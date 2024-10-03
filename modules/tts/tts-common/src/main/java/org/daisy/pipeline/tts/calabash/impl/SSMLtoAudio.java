package org.daisy.pipeline.tts.calabash.impl;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioFootprintMonitor;
import org.daisy.pipeline.tts.calabash.impl.EncodingThread.EncodingException;
import org.daisy.pipeline.tts.config.VoiceConfigExtension;
import org.daisy.pipeline.tts.DefaultSSMLMarkSplitter;
import org.daisy.pipeline.tts.SSMLMarkSplitter;
import org.daisy.pipeline.tts.Sentence;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSLog;
import org.daisy.pipeline.tts.TTSLog.ErrorCode;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.TimedTTSExecutor;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Property THREADS_NUMBER = Properties.getProperty("org.daisy.pipeline.tts.threads.number",
	                                                                      false,
	                                                                      "Number of threads for speech synthesis and audio encoding",
	                                                                      false,
	                                                                      ""+Runtime.getRuntime().availableProcessors());
	private static final Property ENCODING_THREADS_NUMBER = Properties.getProperty("org.daisy.pipeline.tts.threads.encoding.number",
	                                                                               false,
	                                                                               "Number of threads for audio encoding",
	                                                                               false,
	                                                                               null);
	private static final Property SPEAKING_THREADS_NUMBER = Properties.getProperty("org.daisy.pipeline.tts.threads.speaking.number",
	                                                                               false,
	                                                                               "Number of threads for speech synthesis",
	                                                                               false,
	                                                                               null);
	private static final Property THREADS_MEMLIMIT = Properties.getProperty("org.daisy.pipeline.tts.threads.each.memlimit",
	                                                                        false,
	                                                                        "Maximum amount of memory consumed by each speech synthesis thread (in Mb)",
	                                                                        false,
	                                                                        "20"); // 20 Mb

	private TTSEngine mLastTTS; //used if no TTS is found for the current sentence
	private TTSRegistry mTTSRegistry;
	private Logger mLogger;
	private ContiguousText mCurrentSection;
	private File mAudioDir; //where all the sound files will be stored
	private final AudioFileFormat.Type mAudioFileFormat;
	/*
	 * The maximum number of sentences that a section (ContiguousText) can contain.
	 */
	private final int mMaxSentencesPerSection;
	private int mSentenceCounter = 0;
	private long mTotalTextSize;
	private int mDocumentPosition;
	private Map<TTSEngine, List<ContiguousText>> mOrganizedText;
	private AudioFootprintMonitor mAudioFootprintMonitor;
	private Processor mProc;
	private VoiceManager mVoiceManager;
	private TimedTTSExecutor mExecutor = new TimedTTSExecutor();
	private final Map<String,String> mProperties;
	private TTSLog mTTSlog;
	private int mErrorCounter;

	int numberOfCores = getPropertyAsInt(THREADS_NUMBER).get(); // for unit tests to override

	public SSMLtoAudio(File audioDir, AudioFileFormat.Type audioFileFormat,
	        int maxSentencesPerSection, TTSRegistry ttsregistry, Logger logger,
	        AudioFootprintMonitor audioFootprintMonitor, Processor proc,
	        Map<String,String> properties, VoiceConfigExtension configExt, TTSLog logs) {
		mTTSRegistry = ttsregistry;
		mLogger = logger;
		mCurrentSection = null;
		mDocumentPosition = 0;
		mOrganizedText = new HashMap<TTSEngine, List<ContiguousText>>();
		mAudioFootprintMonitor = audioFootprintMonitor;
		mProc = proc;
		mAudioDir = audioDir;
		mAudioFileFormat = audioFileFormat;
		mMaxSentencesPerSection = maxSentencesPerSection;
		mTTSlog = logs;
		/*
		 * initialize the TTS engines
		 */
		mProperties = properties;
		mVoiceManager = new VoiceManager(
			ttsregistry.getWorkingEngines(properties, mTTSlog, mLogger),
			configExt.getVoiceDeclarations());
	}

	/**
	 * @param node Document node of SSML document
	 */
	public void feedSSML(XdmNode doc) throws SynthesisException {
		XdmSequenceIterator iter = doc.axisIterator(Axis.CHILD);
		if (iter.hasNext()) {
			traverse((XdmNode)iter.next(), null);
		}
		endSection();
	}

	private void traverse(XdmNode node, Locale lang) throws SynthesisException {
		if (SentenceTag.equals(node.getNodeName())) {
			if (!dispatchSSML(node, lang))
				mErrorCounter++;
			if (mMaxSentencesPerSection > 0 && ++mSentenceCounter % mMaxSentencesPerSection == 0)
				endSection();
		} else {
			String langAttr = node.getAttributeValue(Sentence_attr_lang);
			if (langAttr != null) {
				try {
					lang = (new Locale.Builder()).setLanguageTag(langAttr).build();
				} catch (IllformedLocaleException e) {
					lang = null;
				}
			}
			XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
			while (iter.hasNext())
				traverse((XdmNode)iter.next(), lang);
		}
	}

	/**
	 * The SSML is assumed to be pushed in document order.
	 *
	 * @param ssml The input SSML
	 * @param lang The parent language
	 * @return true when the SSML was successfully converted to speech, false when there was an error
	 **/
	// package private for tests
	boolean dispatchSSML(XdmNode ssml, Locale lang) throws SynthesisException {
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
		{
			String langAttr = ssml.getAttributeValue(Sentence_attr_lang);
			if (langAttr != null) {
				try {
					lang = (new Locale.Builder()).setLanguageTag(langAttr).build();
				} catch (IllformedLocaleException e) {
					lang = null;
				}
			}
		}
		String id = ssml.getAttributeValue(Sentence_attr_id);

		TTSLog.Entry logEntry = mTTSlog.getOrCreateEntry(id);
		logEntry.setSSML(ssml);
		Iterable<Voice> voices = mVoiceManager.findAvailableVoices(voiceEngine, voiceName, lang, gender);
		Voice preferredVoice = Iterables.getFirst(voices, null);
		logEntry.setSelectedVoice(preferredVoice);
		if (preferredVoice == null) {
			String err = "could not find any installed voice matching with "
			                + "{" + (voiceEngine != null ? ("engine: " + voiceEngine) : "")
			                +       (voiceName != null ? ((voiceEngine != null ? ", " : "") + "name: " + voiceName): "")
			                + "}"
			                + " or providing the language '" + lang + "'";
			logEntry.addError(new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING, err));
			return false;
		}

		TTSEngine newSynth = mVoiceManager.getTTS(preferredVoice);
		if (newSynth == null) {
			/*
			 * Should not happen since findAvailableVoice() returns only a
			 * non-null voice if a TTSService can provide it
			 */
			String err = "could not find any TTS engine for the voice "
				+ new Voice(voiceEngine, voiceName);
			logEntry.addError(new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING, err));
			return false;
		}

		if (!mVoiceManager.matches(preferredVoice, voiceEngine, voiceName, lang, gender)) {
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
		mCurrentSection.sentences.add(new Sentence(newSynth, voices, ssml));
		return true;
	}

	private void endSection() {
		mCurrentSection = null;
	}

	public Iterable<SoundFileLink> blockingRun(AudioServices audioServices)
	        throws SynthesisException, InterruptedException, EncodingException {

		MessageAppender activeBlock = MessageAppender.getActiveBlock(); // px:ssml-to-audio step

		//SSML mark splitter shared by the threads:
		SSMLMarkSplitter ssmlSplitter = new DefaultSSMLMarkSplitter(mProc); // StructuredSSMLSplitter makes assumptions
		                                                                    // that are not always met, e.g. marks may
		                                                                    // be contained within a a prosody element

		reorganizeSections();

		//threading layout
		int reservedThreadNum = 0;
		for (TTSEngine tts : mOrganizedText.keySet()) {
			if (tts != null && tts.reservedThreadNum() > 0)
				reservedThreadNum += tts.reservedThreadNum();
		}
		int ttsThreadNum = numberOfCores;
		int encodingThreadNum = getPropertyAsInt(ENCODING_THREADS_NUMBER).orElse(ttsThreadNum);
		int regularTTSthreadNum = getPropertyAsInt(SPEAKING_THREADS_NUMBER).orElse(ttsThreadNum);
		int totalTTSThreads = regularTTSthreadNum + reservedThreadNum;
		int maxMemPerTTSThread = getPropertyAsInt(THREADS_MEMLIMIT).get() * 1048576;
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

			// compute the sections' size: needed for displaying the progress,
			// splitting the sections and sorting them
			for (ContiguousText section : sections) {
				section.computeSize();
				mTotalTextSize += section.getStringSize();
			}

			// split up the sections that are too big
			// FIXME: Explain what is "too big".
			// FIXME: Code was disabled for now because it does not make sense to let the maximum
			// size depend on the total size (and mTotalTextSize isn't even constant).
			/*int maxSize = (int) (mTotalTextSize / 15);
			List<ContiguousText> newSections = new ArrayList<ContiguousText>();
			List<ContiguousText> toRemove = new ArrayList<ContiguousText>();
			for (ContiguousText section : sections) {
				if (section.getStringSize() >= maxSize) {
					toRemove.add(section);
					splitSection(section, maxSize, newSections);
				}
			}
			sections.removeAll(toRemove);
			sections.addAll(newSections);*/

			// sort the sections according to their size in descending order
			// FIXME: Explain why this is done.
			Collections.sort(sections);

			// keep sorted only the smallest sections (50% of total) so the biggest sections won't
			// necessarily be processed at the same time, as that may consume too much memory.
			Collections.shuffle(sections.subList(0, sections.size() / 2));

			sectionCount += sections.size();
		}
		mLogger.debug("Number of synthesizable TTS sections: " + sectionCount);
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

	private Optional<Integer> getPropertyAsInt(Property prop) {
		String str = prop.getValue(mProperties);
		if (str != null) {
			try {
				return Optional.of(Integer.valueOf(str));
			} catch (NumberFormatException e) {
				mTTSlog.addGeneralError(
					ErrorCode.WARNING, str + " is not a valid value for property " + prop.getName(), e);
			}
		}
		return Optional.empty();
	}
}
