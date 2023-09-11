package org.daisy.pipeline.tts.calabash.impl;

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

import javax.sound.sampled.AudioFileFormat;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.common.messaging.MessageAppender;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioFootprintMonitor;
import org.daisy.pipeline.tts.SSMLMarkSplitter;
import org.daisy.pipeline.tts.StructuredSSMLSplitter;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSEngine.SynthesisResult;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.TTSTimeout.ThreadFreeInterrupter;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.Voice.MarkSupport;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;
import org.daisy.pipeline.tts.VoiceManager;
import org.daisy.pipeline.tts.calabash.impl.EncodingThread.EncodingException;
import org.daisy.pipeline.tts.calabash.impl.TTSLog.ErrorCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;

import com.google.common.collect.Iterables;

/**
 * SSMLtoAudio splits the input SSML sentences into sections called
 * ContiguousText. Every section contains a list of contiguous sentences that
 * may be eventually stored into different audio files, but it is guaranteed
 * that such audio files won't contain sentences of other sections in such a
 * manner that the list of audio files will mirror the document order. Within
 * a section, every sentence has the same single sample rate to prevent us from
 * resampling the audio data before sending them to encoders.
 * 
 * Once all the sentences have been assigned to TTS voices, they are sorted by
 * size and stored in a shared queue of ContiguousText (actually, only the
 * smallest sentences are sorted that way). SSMLtoAudio creates threads to
 * consume this queue.
 * 
 * The TextToPcmThreads send PCM data to EncodingThreads via a queue of
 * ContiguousPCM. These PCM packets are then processed from the longest (with
 * respect to the number of samples) to the shortest in order to make it likely
 * that the threads will finish at the same time. When all the TextToPcmThreads
 * are joined, the pipeline pushes an EndOfQueue marker to every EncodingThreads
 * to notify that they must stop waiting for more PCM packets than the ones
 * already pushed.
 * 
 * The queue of PCM chunks, along with the queues of other TTS steps, share a
 * global max size so as to make sure that they won't grow too much if the
 * encoding is slower than the synthesizing (see AudioFootprintMonitor).
 * 
 * If an EncodingThread fails to encode samples, it won't set the URI attribute
 * of the audio chunks. In that way, SSMLtoAudio is informed not to include the
 * corresponding text into the list of audio clips.
 * 
 */
public class SSMLtoAudio implements FormatSpecifications {

	/*
	 * The maximum number of sentences that a section (ContiguousText) can contain.
	 */
	private static int MAX_SENTENCES_PER_SECTION = 100;

	private TTSEngine mLastTTS; //used if no TTS is found for the current sentence
	private TTSRegistry mTTSRegistry;
	private Logger mLogger;
	private ContiguousText mCurrentSection;
	private File mAudioDir; //where all the sound files will be stored
	private final AudioFileFormat.Type mAudioFileFormat;
	private int mSentenceCounter = 0;
	private long mTotalTextSize;
	private int mDocumentPosition;
	private Map<TTSEngine, List<ContiguousText>> mOrganizedText;
	private AudioFootprintMonitor mAudioFootprintMonitor;
	private Processor mProc;
	private VoiceManager mVoiceManager;
	private TimedTTSExecutor mExecutor = new TimedTTSExecutor();
	private Map<String, String> mProperties;
	private TTSLog mTTSlog;
	private int mErrorCounter;

	public SSMLtoAudio(File audioDir, AudioFileFormat.Type audioFileFormat,
	        TTSRegistry ttsregistry, Logger logger,
	        AudioFootprintMonitor audioFootprintMonitor, Processor proc,
	        VoiceConfigExtension configExt, TTSLog logs) {
		mTTSRegistry = ttsregistry;
		mLogger = logger;
		mCurrentSection = null;
		mDocumentPosition = 0;
		mOrganizedText = new HashMap<TTSEngine, List<ContiguousText>>();
		mAudioFootprintMonitor = audioFootprintMonitor;
		mProc = proc;
		mAudioDir = audioDir;
		mAudioFileFormat = audioFileFormat;
		mTTSlog = logs;

		/*
		 * Create a piece of SSML that will be used for testing. Useless
		 * attributes and namespaces are inserted on purpose. A <mark> is
		 * included for engines that support marks. It is inserted somewhere
		 * in the middle of the string because the SAPI adapter ignores
		 * marks that appear at the end.
		 */
		DocumentBuilder docBuilder = proc.newDocumentBuilder();
		XdmNode testingSSMLWithoutMark; {
			String ssml = "<s:speak version=\"1.0\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\">"
			        + "<s:s xmlns:tmp=\"http://\" id=\"s1\"><s:token>small</s:token> sentence</s:s></s:speak>";
			try {
				testingSSMLWithoutMark = docBuilder.build(new SAXSource(new InputSource(new StringReader(ssml))));
			} catch (SaxonApiException e) {
				throw new RuntimeException(e); // should not happen
			}
		}
		XdmNode testingSSMLWithMark; {
			String ssml = "<s:speak version=\"1.0\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\">"
			        + "<s:s xmlns:tmp=\"http://\" id=\"s1\"><s:token>small</s:token>"
			        + "<s:mark name=\"mark\"></s:mark> sentence</s:s></s:speak>";
			try {
				testingSSMLWithMark = docBuilder.build(new SAXSource(new InputSource(new StringReader(ssml))));
			} catch (SaxonApiException e) {
				throw new RuntimeException(e); // should not happen
			}
		}

		/*
		 * initialize the TTS engines
		 */

		TTSTimeout timeout = new TTSTimeout();
		mProperties = configExt.getAllProperties();
		List<TTSEngine> workingEngines = new ArrayList<TTSEngine>();
		List<String> engineStatus = new ArrayList<String>();
		for (TTSService service : ttsregistry.getServices()) {
			TTSEngine engine = null;
			try {
				engine = createAndTestEngine(
					service, mProperties, testingSSMLWithoutMark, testingSSMLWithMark, timeout);
				workingEngines.add(engine);
				engineStatus.add("[x] " + service.getName());
			} catch (Throwable e) {
				// Show the full error with stack trace only in the main and TTS log. A short version is included
				// in the engine status summary. An engine that could not be activated is not an error
				// unless no engines could be activated at all. This is to not confuse users because it
				// is normal that only a part of the engines work.
				String msg = service.getName() + " could not be activated";
				mTTSlog.addGeneralError(ErrorCode.WARNING, msg + ": " + e.getMessage(), e);
				engineStatus.add("[ ] " + msg);
			}

		}
		timeout.close();
		String summary = "Number of working TTS engine(s): " + workingEngines.size() + "/"
			+ ttsregistry.getServices().size();
		if (workingEngines.size() == 0) {
			mLogger.error(summary);
			for (String s : engineStatus)
				mLogger.error(" * " + s);
		} else {
			mLogger.info(summary);
			for (String s : engineStatus)
				mLogger.info(" * " + s);
		}

		mVoiceManager = new VoiceManager(workingEngines, configExt.getVoiceDeclarations());
	}

	/**
	 * @throws Throwable if an engine could not be created or when the test failed. The exception is
	 *                   included in the TTS and the message is included in the engine status
	 *                   summary. The stack trace is printed in the detailed log.
	 */
	private TTSEngine createAndTestEngine(final TTSService service, Map<String, String> properties,
	                                      XdmNode testingSSMLWithoutMark, XdmNode testingSSMLWithMark,
	                                      TTSTimeout timeout)
			throws Throwable {

		//create the engine
		TTSEngine engine = null;
		timeout.enableForCurrentThread(2);
		try {
			engine = service.newEngine(properties);
		} finally {
			timeout.disable();
		}

		//get a voice supporting SSML marks (so far as they are supported by the engine)
		Voice firstVoice = null;
		int timeoutSecs = 30;
		timeout.enableForCurrentThread(timeoutSecs);
		try {
			for (Voice v : engine.getAvailableVoices()) {
				if (!engine.handlesMarks() || v.getMarkSupport() != MarkSupport.MARK_NOT_SUPPORTED) {
					firstVoice = v;
					break;
				}
			}
			if (firstVoice == null) {
				throw new Exception("no voices available");
			}
		} catch (InterruptedException e) {
			throw new Exception("timeout while retrieving voices (exceeded "
			                    + timeoutSecs + " seconds)");
		} catch (Exception e) {
			throw new Exception("failed to retreive voices: " + e.getMessage(), e);
		} finally {
			timeout.disable();
		}

		//allocate resources for testing purpose
		final TTSEngine fengine = engine;
		TTSResource resource = null;
		timeout.enableForCurrentThread(2);
		try {
			resource = engine.allocateThreadResources();
		} catch (Exception e) {
			throw new Exception("could not allocate resources: " + e.getMessage(), e);
		} finally {
			timeout.disable();
		}

		//create a custom interrupter in case the engine hangs
		final TTSResource res = resource;
		TTSTimeout.ThreadFreeInterrupter interrupter = new ThreadFreeInterrupter() {
			@Override
			public void threadFreeInterrupt() {
				mTTSlog.addGeneralError(
					ErrorCode.WARNING,
					"Timeout while initializing " + service.getName()
					+ ". Forcing interruption of the current work of " + service.getName() + "...");
				fengine.interruptCurrentWork(res);
			}
		};

		//run the text-to-speech on the testing input
		SynthesisResult result = null;
		try {
			XdmNode ssml = engine.handlesMarks() ? testingSSMLWithMark : testingSSMLWithoutMark;
			result = mExecutor.synthesizeWithTimeout(
				timeout, interrupter, null, ssml, Sentence.computeSize(ssml),
				engine, firstVoice, res);
		} catch (Exception e) {
			throw new Exception("test failed: " + e.getMessage(), e);
		} finally {
			if (res != null)
				timeout.enableForCurrentThread(2);
				try {
					engine.releaseThreadResources(res);
				} catch (Exception e) {
					mTTSlog.addGeneralError(
						ErrorCode.WARNING,
						"Error while releasing resource of " + service.getName() + ": " + e.getMessage(),
						e);
				} finally {
					timeout.disable();
				}
		}

		//check that the output buffer is big enough
		String msg = "";
		if (result.audio.getFrameLength() * result.audio.getFormat().getFrameSize() < 2500) {
			msg = "Audio output is not big enough. ";
		}

		if (engine.handlesMarks()) {
			// test SSML contains one mark
			String details = " voice: "+firstVoice;
			if (result.marks.size() != 1) {
				msg += "One bookmark events expected, but received " + result.marks.size() + " events instead. "+details;
			} else {
				int offset = result.marks.get(0);
				if (offset < 2500) {
					msg += "Expecting mark offset to be bigger, got "
					        + offset + " as offset. "+details;
				}
			}
		}
		if (!msg.isEmpty()) {
			throw new Exception("test failed: " + msg);
		}

		return engine;
	}

	/**
	 * @param node Document node of SSML document
	 */
	public void feedSSML(XdmNode doc) throws SynthesisException {
		XdmSequenceIterator iter = doc.axisIterator(Axis.CHILD);
		if (iter.hasNext()) {
			traverse((XdmNode)iter.next());
		}
		endSection();
	}

	private void traverse(XdmNode node) throws SynthesisException {
		if (SentenceTag.equals(node.getNodeName())) {
			if (!dispatchSSML(node))
				mErrorCounter++;
			if (++mSentenceCounter % MAX_SENTENCES_PER_SECTION == 0)
				endSection();
		} else {
			XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
			while (iter.hasNext())
				traverse((XdmNode)iter.next());
		}
	}

	/**
	 * The SSML is assumed to be pushed in document order.
	 *
	 * @param ssml The input SSML
	 * @return true when the SSML was successfully converted to speech, false when there was an error
	 **/
	// package private for tests
	boolean dispatchSSML(XdmNode ssml) throws SynthesisException {
		String voiceEngine = ssml.getAttributeValue(Sentence_attr_select1);
		String voiceName = ssml.getAttributeValue(Sentence_attr_select2);
		Gender gender; {
			String attr = ssml.getAttributeValue(Sentence_attr_gender);
			String ageAttr = ssml.getAttributeValue(Sentence_attr_age);
			if (attr != null && ageAttr != null) {
				try {
					int age = Integer.parseInt(ageAttr);
					if (age <= 16) {
						gender = Gender.of(attr + "-child");
					} else if (age >= 70) {
						gender = Gender.of(attr + "-eldery");
					} else {
						gender = Gender.of(attr);
					}
				} catch (NumberFormatException e) {
					gender = Gender.of(attr);
				}
			} else {
				gender = Gender.of(attr);
			}
		}
		Locale lang; {
			String attr = ssml.getAttributeValue(Sentence_attr_lang);
			try {
				lang = VoiceInfo.tagToLocale(attr);
			} catch (UnknownLanguage e) {
				lang = null;
			}
		}
		String id = ssml.getAttributeValue(Sentence_attr_id);

		TTSLog.Entry logEntry = mTTSlog.getOrCreateEntry(id);
		logEntry.setSSML(ssml);
		Voice voice = mVoiceManager.findAvailableVoice(voiceEngine, voiceName, lang, gender);
		logEntry.setSelectedVoice(voice);
		if (voice == null) {
			String err = "could not find any installed voice matching with "
			                + new Voice(voiceEngine, voiceName)
			                + " or providing the language '" + lang + "'";
			logEntry.addError(new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING, err));
			endSection();
			return false;
		}

		TTSEngine newSynth = mVoiceManager.getTTS(voice);
		if (newSynth == null) {
			/*
			 * Should not happen since findAvailableVoice() returns only a
			 * non-null voice if a TTSService can provide it
			 */
			String err = "could not find any TTS engine for the voice "
				+ new Voice(voiceEngine, voiceName);
			logEntry.addError(new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING, err));
			endSection();
			return false;
		}

		if (!mVoiceManager.matches(voice, voiceEngine, voiceName, lang, gender)) {
			logEntry.addError(new TTSLog.Error(TTSLog.ErrorCode.UNEXPECTED_VOICE,
			        "no voice matches exactly with the requested characteristics"));
		}

		/*
		 * If a TTS engine has no reserved threads, its sentences are pushed to
		 * a global list whose key is null. Otherwise the TTS Service is used as
		 * key.
		 */
		TTSEngine poolkey = null;
		if (newSynth.reservedThreadNum() > 0) {
			poolkey = newSynth;
		}

		if (newSynth != mLastTTS) {
			if (mLastTTS != null
			        && (poolkey != null || mLastTTS.reservedThreadNum() > 0))
				endSection(); // necessary because the same thread wouldn't be able to
				              // concatenate outputs of different formats
			mLastTTS = newSynth;
		}

		if (mCurrentSection == null) {
			//happen the first time and whenever endSection() is called
			mCurrentSection = new ContiguousText(mDocumentPosition++, mAudioDir);
			List<ContiguousText> listOfSections = mOrganizedText.get(poolkey);
			if (listOfSections == null) {
				listOfSections = new ArrayList<ContiguousText>();
				mOrganizedText.put(poolkey, listOfSections);
			}

			listOfSections.add(mCurrentSection);
		}
		mCurrentSection.sentences.add(new Sentence(newSynth, voice, ssml));
		return true;
	}

	private void endSection() {
		mCurrentSection = null;
	}

	public Iterable<SoundFileLink> blockingRun(AudioServices audioServices)
	        throws SynthesisException, InterruptedException, EncodingException {

		MessageAppender activeBlock = MessageAppender.getActiveBlock(); // px:ssml-to-audio step

		//SSML mark splitter shared by the threads:
		SSMLMarkSplitter ssmlSplitter = new StructuredSSMLSplitter(mProc);

		reorganizeSections();

		//threading layout
		int reservedThreadNum = 0;
		for (TTSEngine tts : mOrganizedText.keySet()) {
			if (tts != null && tts.reservedThreadNum() > 0)
				reservedThreadNum += tts.reservedThreadNum();
		}
		int cores = Runtime.getRuntime().availableProcessors();
		int ttsThreadNum = convertToInt(mProperties, "org.daisy.pipeline.tts.threads.number", cores);
		int encodingThreadNum = convertToInt(mProperties, "org.daisy.pipeline.tts.threads.encoding.number",
		        ttsThreadNum);
		int regularTTSthreadNum = convertToInt(mProperties, "org.daisy.pipeline.tts.threads.speaking.number",
		        ttsThreadNum);
		int totalTTSThreads = regularTTSthreadNum + reservedThreadNum;
		int maxMemPerTTSThread = convertToInt(mProperties, "org.daisy.pipeline.tts.threads.each.memlimit", 20)*1048576; //20MB
		mLogger.info("Number of encoding threads: " + encodingThreadNum);
		mLogger.info("Number of regular text-to-speech threads: " + regularTTSthreadNum);
		mLogger.info("Number of reserved text-to-speech threads: " + reservedThreadNum);
		mLogger.info("Max TTS memory footprint (encoding excluded): "
		        + mAudioFootprintMonitor.getSpaceForTTS() / 1000000 + "MB");
		mLogger.info("Max encoding memory footprint: "
		        + mAudioFootprintMonitor.getSpaceForEncoding() / 1000000 + "MB");

		//input queue common to all the threads
		BlockingQueue<ContiguousPCM> pcmQueue = new PriorityBlockingQueue<ContiguousPCM>();

		//start the TTS threads
		TextToPcmThread[] tpt = new TextToPcmThread[totalTTSThreads];
		List<ContiguousText> text = mOrganizedText.get(null);
		if (text == null) {
			text = Collections.EMPTY_LIST;
		}
		int i = 0;
		ConcurrentLinkedQueue<ContiguousText> stext = new ConcurrentLinkedQueue<ContiguousText>(text);
		if (regularTTSthreadNum > 0) {
			long textSize = stext.stream().mapToLong(ContiguousText::getStringSize).sum();
			BigDecimal portion = textSize == 0
				? BigDecimal.ZERO
				: new BigDecimal(textSize).divide(new BigDecimal(mTotalTextSize), MathContext.DECIMAL128)
				                          .divide(new BigDecimal(regularTTSthreadNum), MathContext.DECIMAL128);
			for (; i < regularTTSthreadNum; ++i) {
				tpt[i] = new TextToPcmThread();
				tpt[i].start(stext, pcmQueue, mExecutor, mTTSRegistry, mVoiceManager, ssmlSplitter,
				             mLogger, mAudioFootprintMonitor, maxMemPerTTSThread, mTTSlog, activeBlock,
				             mTotalTextSize, portion);
			}
		}
		for (Map.Entry<TTSEngine, List<ContiguousText>> e : mOrganizedText.entrySet()) {
			TTSEngine tts = e.getKey();
			if (tts != null) { //tts = null is handled by the previous loop
				stext = new ConcurrentLinkedQueue<ContiguousText>(e.getValue());
				if (tts.reservedThreadNum() > 0) {
					long textSize = stext.stream().mapToLong(ContiguousText::getStringSize).sum();
					BigDecimal portion = textSize == 0
						? BigDecimal.ZERO
						: new BigDecimal(textSize).divide(new BigDecimal(mTotalTextSize), MathContext.DECIMAL128)
						                          .divide(new BigDecimal(tts.reservedThreadNum()), MathContext.DECIMAL128);
					for (int j = 0; j < tts.reservedThreadNum(); ++i, ++j) {
						tpt[i] = new TextToPcmThread();
						tpt[i].start(stext, pcmQueue, mExecutor, mTTSRegistry, mVoiceManager, ssmlSplitter,
						             mLogger, mAudioFootprintMonitor, maxMemPerTTSThread, mTTSlog,
						             activeBlock, mTotalTextSize, portion);
					}
				}
			}
		}
		mLogger.info("Text-to-speech threads started.");

		//start the encoding threads
		EncodingThread[] encodingTh = new EncodingThread[encodingThreadNum];
		for (int j = 0; j < encodingTh.length; ++j) {
			encodingTh[j] = new EncodingThread();
			encodingTh[j].start(mAudioFileFormat, audioServices, pcmQueue,
			                    mAudioFootprintMonitor, mProperties, mTTSlog, activeBlock);
		}
		mLogger.info("Encoding threads started.");

		//collect the sound fragments
		Collection<SoundFileLink>[] fragments = new Collection[tpt.length];
		for (int j = 0; j < tpt.length; ++j) {
			fragments[j] = tpt[j].getSoundFragments();
		}
		for (int j = 0; j < tpt.length; ++j) {
			mErrorCounter += tpt[j].getErrorCount();
		}

		//send END notifications and wait for the encoding threads to finish
		mLogger.info("Text-to-speech finished. Waiting for audio encoding to finish...");
		for (int k = 0; k < encodingTh.length; ++k) {
			pcmQueue.add(ContiguousPCM.EndOfQueue);
		}
		for (int j = 0; j < encodingTh.length; ++j) {
			try {
				encodingTh[j].waitToFinish();
			} catch (EncodingException e) {
				while (++j < encodingTh.length)
					// FIXME: interrupt instead of waiting
					try { encodingTh[j].waitToFinish(); }
					catch (EncodingException _e) {}
				throw e;
			}
		}

		mLogger.info("Audio encoding finished.");

		return Iterables.concat(fragments);
	}

	double getErrorRate() {
		return (double)mErrorCounter/mSentenceCounter;
	}

	private void reorganizeSections() {
		mTotalTextSize = 0;
		int sectionCount = 0;
		for (List<ContiguousText> sections : mOrganizedText.values()) {
			//compute the sections' size: needed for displaying the progress,
			//splitting the sections and sorting them
			for (ContiguousText section : sections)
				section.computeSize();

			for (ContiguousText section : sections) {
				mTotalTextSize += section.getStringSize();
			}
			//split up the sections that are too big
			int maxSize = (int) (mTotalTextSize / 15); //should it depend on the total size or be an absolute max?
			List<ContiguousText> newSections = new ArrayList<ContiguousText>();
			List<ContiguousText> toRemove = new ArrayList<ContiguousText>();
			for (ContiguousText section : sections) {
				if (section.getStringSize() >= maxSize) {
					toRemove.add(section);
					splitSection(section, maxSize, newSections);
				}
			}

			sections.removeAll(toRemove);
			sections.addAll(newSections);

			//sort the sections according to their size in descending-order
			Collections.sort(sections);

			//keep sorted only the smallest sections (50% of total) so the biggest sections won't
			//necessarily be processed at the same time, as that may consume too much memory.
			Collections.shuffle(sections.subList(0, sections.size() / 2));

			sectionCount += sections.size();
		}
		mLogger.info("Number of synthesizable TTS sections: " + sectionCount);
	}

	//we can dispense with this function as soon as we take into consideration the size of the SSML
	//sentences, rather than creating a new section every 10 sentences or so.
	private void splitSection(ContiguousText section, int maxSize,
	        List<ContiguousText> newSections) {
		int left = 0;
		int count = 0;
		ContiguousText currentSection = new ContiguousText(section.getDocumentPosition(),
		        section.getAudioOutputDir());
		currentSection.setStringsize(0);
		for (int right = 0; right < section.sentences.size(); ++right) {
			if (currentSection.getStringSize() > maxSize) {
				currentSection.sentences = section.sentences.subList(left, right);
				currentSection.setDocumentSplitPosition(count);
				newSections.add(currentSection);
				currentSection = new ContiguousText(section.getDocumentPosition(), section
				        .getAudioOutputDir());
				currentSection.setStringsize(0);
				left = right;
				++count;
			}
			currentSection.setStringsize(currentSection.getStringSize()
			        + section.sentences.get(right).getSize());
		}
		currentSection.sentences = section.sentences.subList(left, section.sentences.size());
		currentSection.setDocumentSplitPosition(count);
		newSections.add(currentSection);
	}

	private int convertToInt(Map<String, String> params, String prop, int defaultVal) {
		String str = params.get(prop);
		if (str != null) {
			try {
				defaultVal = Integer.valueOf(str);
			} catch (NumberFormatException e) {
				mTTSlog.addGeneralError(
					ErrorCode.WARNING, str + " is not a valid value for property " + prop, e);
			}
		}
		return defaultVal;
	}
}
