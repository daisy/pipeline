package org.daisy.pipeline.tts.synthesize;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.xslt.CompiledStylesheet;
import org.daisy.common.xslt.XslTransformCompiler;
import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioBufferTracker;
import org.daisy.pipeline.tts.SSMLMarkSplitter;
import org.daisy.pipeline.tts.StraightBufferAllocator;
import org.daisy.pipeline.tts.StructuredSSMLSplitter;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.TTSServiceUtil;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.TTSTimeout.ThreadFreeInterrupter;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.Voice.MarkSupport;
import org.daisy.pipeline.tts.VoiceManager;
import org.daisy.pipeline.tts.synthesize.TTSLog.ErrorCode;
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
 * encoding is slower than the synthesizing (see AudioBuffersTracker).
 * 
 * If an EncodingThread fails to encode samples, it won't set the URI attribute
 * of the audio chunks. In that way, SSMLtoAudio is informed not to include the
 * corresponding text into the list of audio clips.
 * 
 */
public class SSMLtoAudio implements IProgressListener, FormatSpecifications {
	private TTSEngine mLastTTS; //used if no TTS is found for the current sentence
	private TTSRegistry mTTSRegistry;
	private IPipelineLogger mLogger;
	private ContiguousText mCurrentSection;
	private File mAudioDir; //where all the sound files will be stored
	private long mTotalTextSize; //used for the progress bar
	private long mPrintedProgress;
	private long mProgress;
	private int mDocumentPosition;
	private Map<TTSEngine, List<ContiguousText>> mOrganizedText;
	private AudioBufferTracker mAudioBufferTracker;
	private Processor mProc;
	private Logger ServerLogger = LoggerFactory.getLogger(TTSRegistry.class);
	private VoiceManager mVoiceManager;
	private Map<TTSService, CompiledStylesheet> mSSMLtransformers;
	private Map<String, String> mProperties;
	private TTSLog mTTSlog;

	public SSMLtoAudio(File audioDir, TTSRegistry ttsregistry, IPipelineLogger logger,
	        AudioBufferTracker audioBufferTracker, Processor proc, URIResolver uriResolver,
	        VoiceConfigExtension configExt, TTSLog logs) {
		mTTSRegistry = ttsregistry;
		mLogger = logger;
		mCurrentSection = null;
		mDocumentPosition = 0;
		mOrganizedText = new HashMap<TTSEngine, List<ContiguousText>>();
		mAudioBufferTracker = audioBufferTracker;
		mProc = proc;
		mAudioDir = audioDir;
		mTTSlog = logs;
		mSSMLtransformers = new HashMap<TTSService, CompiledStylesheet>();

		/*
		 * Create a piece of SSML that will be used for testing. Useless
		 * attributes and namespaces are inserted on purpose.
		 */
		String ssml = "<s:speak version=\"1.0\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\">"
		        + "<s:s xmlns:tmp=\"http://\" id=\"s1\"><s:token>small</s:token> sentence</s:s></s:speak>";
		DocumentBuilder builder = proc.newDocumentBuilder();
		SAXSource source = new SAXSource(new InputSource(new StringReader(ssml)));
		XdmNode testingXML = null;
		try {
			testingXML = builder.build(source);
		} catch (SaxonApiException e1) {
			//that should not happen
			ServerLogger.error("could not compile testing SSML " + ssml);
			return;
		}

		/*
		 * initialize the TTS engines
		 */

		TTSTimeout timeout = new TTSTimeout();
		mProperties = configExt.getAllProperties();
		XslTransformCompiler xslCompiler = new XslTransformCompiler(proc
		        .getUnderlyingConfiguration(), uriResolver);
		List<TTSEngine> workingEngines = new ArrayList<TTSEngine>();
		for (TTSService service : ttsregistry.getServices()) {
			if (service.getSSMLxslTransformerURL() == null) {
				String err = "missing SSML transformer for TTS "
				        + TTSServiceUtil.displayName(service);
				mTTSlog.addGeneralError(ErrorCode.WARNING, err);
				ServerLogger.error(err);
				continue;
			}
			CompiledStylesheet transf = null;
			try {
				transf = xslCompiler.compileStylesheet(service.getSSMLxslTransformerURL()
				        .openStream());
			} catch (SaxonApiException e) {
				String err = "error while compiling XSLT SSML adapter of "
				        + TTSServiceUtil.displayName(service);
				mTTSlog.addGeneralError(ErrorCode.WARNING, err);
				ServerLogger.error(err);
			} catch (IOException e) {
				String err = "error while opening XSLT SSML adapter of "
				        + TTSServiceUtil.displayName(service);
				ServerLogger.error(err);
				mTTSlog.addGeneralError(ErrorCode.WARNING, err);
			}
			if (transf != null) {
				mSSMLtransformers.put(service, transf);
				TTSEngine engine = createAndTestEngine(service, mProperties, testingXML,
				        transf, timeout);
				if (engine != null) {
					workingEngines.add(engine);
				}
			}

		}
		timeout.close();
		mLogger.printInfo("Number of working TTS engine(s): " + workingEngines.size() + "/"
		        + ttsregistry.getServices().size());

		mVoiceManager = new VoiceManager(workingEngines, configExt.getVoiceDeclarations());
	}

	private TTSEngine createAndTestEngine(final TTSService service,
	        Map<String, String> properties, XdmNode testingXML,
	        CompiledStylesheet ssmlTransformer, TTSTimeout timeout) {

		//create the engine
		TTSEngine engine = null;
		try {
			timeout.enableForCurrentThread(2);
			engine = service.newEngine(properties);
		} catch (Throwable e) {
			String err = TTSServiceUtil.displayName(service)
			        + " could not be initialized, cause: " + e.getMessage() + ": "
			        + getStack(e);
			mTTSlog.addGeneralError(ErrorCode.WARNING, err);
			ServerLogger.error(err);
			return null;
		} finally {
			timeout.disable();
		}

		//transform the SSML with the custom SSML adapter
		String ttsInput = null;
		try {
			Map<String, Object> params = new TreeMap<String, Object>();
			if (engine.endingMark() != null)
				params.put("ending-mark", engine.endingMark());
			ttsInput = ssmlTransformer.newTransformer().transformToString(testingXML, params);
		} catch (SaxonApiException e) {
			String err = "error while using the SSML adapter of "
			        + TTSServiceUtil.displayName(service) + " on " + testingXML.toString();
			ServerLogger.error(err);
			mTTSlog.addGeneralError(ErrorCode.WARNING, err);
			return null;
		}

		//get a voice supporting SSML marks (so far as they are supported by the engine)
		Voice firstVoice = null;
		try {
			timeout.enableForCurrentThread(2);
			for (Voice v : engine.getAvailableVoices()) {
				if (engine.endingMark() == null
				        || v.getMarkSupport() != MarkSupport.MARK_NOT_SUPPORTED) {
					firstVoice = v;
				}
			}
			if (firstVoice == null) {
				String err = TTSServiceUtil.displayName(service)
				        + " cannot be tested because no voice seems available.";
				mTTSlog.addGeneralError(ErrorCode.WARNING, err);
				ServerLogger.error(err);
				return null;
			}
		} catch (Exception e) {
			String err = TTSServiceUtil.displayName(service)
			        + " failed to return voices, cause: " + e.getMessage() + ": "
			        + getStack(e);
			mTTSlog.addGeneralError(ErrorCode.WARNING, err);
			ServerLogger.error(err);
			return null;
		} finally {
			timeout.disable();
		}

		//allocate resources for testing purpose
		final TTSEngine fengine = engine;
		TTSResource resource = null;
		try {
			timeout.enableForCurrentThread(2);
			resource = engine.allocateThreadResources();
		} catch (Exception e) {
			String err = "Could not initialize resource for "
			        + TTSServiceUtil.displayName(service) + ", cause: " + e.getMessage()
			        + ": " + getStack(e);
			mTTSlog.addGeneralError(ErrorCode.WARNING, err);
			ServerLogger.error(err);
			return null;
		} finally {
			timeout.disable();
		}

		//create a custom interrupter in case the engine hangs
		final TTSResource res = resource;
		TTSTimeout.ThreadFreeInterrupter interrupter = new ThreadFreeInterrupter() {
			@Override
			public void threadFreeInterrupt() {
				String msg = "Timeout while initializing "
				        + TTSServiceUtil.displayName(service)
				        + ". Forcing interruption of the current work of "
				        + TTSServiceUtil.displayName(service) + "... ";
				ServerLogger.warn(msg);
				mTTSlog.addGeneralError(ErrorCode.WARNING, msg);
				fengine.interruptCurrentWork(res);
			}
		};

		//run the text-to-speech on the testing input
		Collection<AudioBuffer> audioBuffers = null;
		List<TTSService.Mark> marks = new ArrayList<TTSService.Mark>();
		List<String> expectedMarks = new ArrayList<String>();
		if (engine.endingMark() != null)
			expectedMarks.add(engine.endingMark());
		try {
			timeout.enableForCurrentThread(interrupter, 2);
			audioBuffers = engine.synthesize(ttsInput, testingXML, firstVoice, res, marks,
			        expectedMarks, new StraightBufferAllocator(), false);
		} catch (Exception e) {
			String msg = "Error while testing " + TTSServiceUtil.displayName(service) + "; "
			        + e.getMessage() + ": " + getStack(e);
			ServerLogger.warn(msg);
			mTTSlog.addGeneralError(ErrorCode.WARNING, msg);
			return null;
		} finally {
			timeout.disable();
			if (res != null)
				try {
					timeout.enableForCurrentThread(2);
					engine.releaseThreadResources(res);
				} catch (Exception e) {
					String msg = "Error while releasing resource of "
					        + TTSServiceUtil.displayName(service) + "; " + e.getMessage()
					        + ": " + getStack(e);
					ServerLogger.warn(msg);
					mTTSlog.addGeneralError(ErrorCode.WARNING, msg);
				} finally {
					timeout.disable();
				}
		}

		//check that the output buffer is big enough
		String msg = "";
		int size = 0;
		for (AudioBuffer buff : audioBuffers)
			size += buff.size;
		if (size < 2500) {
			msg = "Audio output is not big enough. ";
		}

		//check the ending mark
		if (engine.endingMark() != null) {
			String details = " input: "+ttsInput+", voice: "+firstVoice;
			if (marks.size() != 1) {
				msg += "One bookmark events expected, but received " + marks.size() + " events instead. "+details;
			} else {
				Mark mark = marks.get(0);
				if (!engine.endingMark().equals(mark.name)) {
					msg += "Expecting ending mark " + engine.endingMark() + ", got "
					        + mark.name + " instead. "+details;
				}
				if (mark.offsetInAudio < 2500) {
					msg += "Expecting ending mark offset to be bigger, got "
					        + mark.offsetInAudio + " as offset. "+details;
				}
			}
		}
		if (!msg.isEmpty()) {
			msg = "Errors found while testing, " + TTSServiceUtil.displayName(service) + ": "
			        + msg;
			ServerLogger.warn(msg);
			mTTSlog.addGeneralError(ErrorCode.WARNING, msg);
			return null;
		}

		return engine;
	}

	/**
	 * The SSML is assumed to be pushed in document order.
	 **/
	public void dispatchSSML(XdmNode ssml) throws SynthesisException {
		String voiceEngine = ssml.getAttributeValue(Sentence_attr_select1);
		String voiceName = ssml.getAttributeValue(Sentence_attr_select2);
		String gender = ssml.getAttributeValue(Sentence_attr_gender);
		String age = ssml.getAttributeValue(Sentence_attr_age);
		String id = ssml.getAttributeValue(Sentence_attr_id);
		String lang = ssml.getAttributeValue(Sentence_attr_lang);

		TTSLog.Entry logEntry = mTTSlog.getOrCreateEntry(id);
		logEntry.setSSML(ssml);

		if (age != null) {
			try {
				int age_i = Integer.parseInt(age);
				if (age_i <= 16) {
					gender += "-child";
				} else if (age_i >= 70) {
					gender += "-eldery";
				}
			} catch (NumberFormatException e) {
				//ignore
			}
		}

		boolean[] exactMatch = new boolean[1];
		Voice voice = mVoiceManager.findAvailableVoice(voiceEngine, voiceName, lang, gender,
		        exactMatch);
		logEntry.setSelectedVoice(voice);
		if (voice == null) {
			logEntry.addError(new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING,
			        "could not find any installed voice matching with "
			                + new Voice(voiceEngine, voiceName)
			                + " or providing the language '" + lang + "'"));
			endSection();
			return;
		}

		TTSEngine newSynth = mVoiceManager.getTTS(voice);
		if (newSynth == null) {
			/*
			 * Should not happen since findAvailableVoice() returns only a
			 * non-null voice if a TTSService can provide it
			 */
			logEntry.addError(new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING,
			        "could not find any TTS engine for the voice "
			                + new Voice(voiceEngine, voiceName)));
			endSection();
			return;
		}

		if (!exactMatch[0]) {
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
			        && (poolkey != null || mLastTTS.reservedThreadNum() != 0
			                || mLastTTS.getAudioOutputFormat() == null
			                || newSynth.getAudioOutputFormat() == null || !mLastTTS
			                .getAudioOutputFormat().matches(newSynth.getAudioOutputFormat())))
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
	}

	public void endSection() {
		mCurrentSection = null;
	}

	public Iterable<SoundFileLink> blockingRun(AudioServices audioServices)
	        throws SynthesisException, InterruptedException {

		//SSML mark splitter shared by the threads:
		SSMLMarkSplitter ssmlSplitter = new StructuredSSMLSplitter(mProc);

		reorganizeSections();
		mProgress = 0;
		mPrintedProgress = 0;

		//threading layout
		int reservedThreadNum = 0;
		for (TTSEngine tts : mOrganizedText.keySet()) {
			if (tts != null && tts.reservedThreadNum() > 0)
				reservedThreadNum += tts.reservedThreadNum();
		}
		int cores = Runtime.getRuntime().availableProcessors();
		int ttsThreadNum = convertToInt(mProperties, "threads.number", cores);
		int encodingThreadNum = convertToInt(mProperties, "threads.encoding.number",
		        ttsThreadNum);
		int regularTTSthreadNum = convertToInt(mProperties, "threads.speaking.number",
		        ttsThreadNum);
		int totalTTSThreads = regularTTSthreadNum + reservedThreadNum;
		int maxMemPerTTSThread = convertToInt(mProperties, "threads.each.memlimit", 20)*1048576; //20MB
		mLogger.printInfo("Number of encoding threads: " + encodingThreadNum);
		mLogger.printInfo("Number of regular text-to-speech threads: " + regularTTSthreadNum);
		mLogger.printInfo("Number of reserved text-to-speech threads: " + reservedThreadNum);
		mLogger.printInfo("Max TTS memory footprint (encoding excluded): "
		        + mAudioBufferTracker.getSpaceForTTS() / 1000000 + "MB");
		mLogger.printInfo("Max encoding memory footprint: "
		        + mAudioBufferTracker.getSpaceForEncoding() / 1000000 + "MB");

		//input queue common to all the threads
		BlockingQueue<ContiguousPCM> pcmQueue = new PriorityBlockingQueue<ContiguousPCM>();

		//start the TTS threads
		TextToPcmThread[] tpt = new TextToPcmThread[totalTTSThreads];
		List<ContiguousText> text = mOrganizedText.get(null);
		if (text == null) {
			text = Collections.EMPTY_LIST;
		}
		ConcurrentLinkedQueue<ContiguousText> stext = new ConcurrentLinkedQueue<ContiguousText>(
		        text);
		int i = 0;
		for (; i < regularTTSthreadNum; ++i) {
			tpt[i] = new TextToPcmThread();
			tpt[i].start(stext, pcmQueue, mTTSRegistry, mVoiceManager, ssmlSplitter, this,
			        mLogger, mAudioBufferTracker, maxMemPerTTSThread, mSSMLtransformers,
			        mTTSlog);
		}
		for (Map.Entry<TTSEngine, List<ContiguousText>> e : mOrganizedText.entrySet()) {
			TTSEngine tts = e.getKey();
			if (tts != null) { //tts = null is handled by the previous loop
				stext = new ConcurrentLinkedQueue<ContiguousText>(e.getValue());
				for (int j = 0; j < tts.reservedThreadNum(); ++i, ++j) {
					tpt[i] = new TextToPcmThread();
					tpt[i].start(stext, pcmQueue, mTTSRegistry, mVoiceManager, ssmlSplitter,
					        this, mLogger, mAudioBufferTracker, maxMemPerTTSThread,
					        mSSMLtransformers, mTTSlog);
				}
			}
		}
		mLogger.printInfo("Text-to-speech threads started.");

		//start the encoding threads
		EncodingThread[] encodingTh = new EncodingThread[encodingThreadNum];
		for (int j = 0; j < encodingTh.length; ++j) {
			encodingTh[j] = new EncodingThread();
			encodingTh[j].start(audioServices, pcmQueue, mLogger, mAudioBufferTracker,
			        mProperties, mTTSlog);
		}
		mLogger.printInfo("Encoding threads started.");

		//collect the sound fragments
		Collection<SoundFileLink>[] fragments = new Collection[tpt.length];
		for (int j = 0; j < tpt.length; ++j) {
			fragments[j] = tpt[j].getSoundFragments();
		}

		//send END notifications and wait for the encoding threads to finish
		mLogger.printInfo("Text-to-speech finished. Waiting for audio encoding to finish...");
		for (int k = 0; k < encodingTh.length; ++k) {
			pcmQueue.add(ContiguousPCM.EndOfQueue);
		}
		for (int j = 0; j < encodingTh.length; ++j)
			encodingTh[j].waitToFinish();

		mLogger.printInfo("Audio encoding finished.");

		return Iterables.concat(fragments);
	}

	@Override
	synchronized public void notifyFinished(ContiguousText section) {
		mProgress += section.getStringSize();
		if (mProgress - mPrintedProgress > mTotalTextSize / 15) {
			int TTSMem = mAudioBufferTracker.getUnreleasedTTSMem() / 1000000;
			int EncodeMem = mAudioBufferTracker.getUnreleasedEncondingMem() / 1000000;
			mLogger.printInfo("progress: " + 100 * mProgress / mTotalTextSize + "%  [TTS: "
			        + TTSMem + "MB encoding: " + EncodeMem + "MB]");
			mPrintedProgress = mProgress;
		}
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
		mLogger.printInfo("Number of synthesizable TTS sections: " + sectionCount);
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

	private static String getStack(Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		t.printStackTrace(printWriter);
		printWriter.flush();
		return writer.toString();
	}

	private int convertToInt(Map<String, String> params, String prop, int defaultVal) {
		String str = params.get(prop);
		if (str != null) {
			try {
				defaultVal = Integer.valueOf(str);
			} catch (NumberFormatException e) {
				String msg = str + " is not a valid value for property " + prop;
				ServerLogger.warn(msg);
				mTTSlog.addGeneralError(ErrorCode.WARNING, msg);
			}
		}
		return defaultVal;
	}
}
