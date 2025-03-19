package org.daisy.pipeline.tts.synthesize.calabash.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFormat;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.common.xslt.CompiledStylesheet;
import org.daisy.common.xslt.ThreadUnsafeXslTransformer;
import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.AudioBufferTracker;
import org.daisy.pipeline.tts.SSMLMarkSplitter;
import org.daisy.pipeline.tts.SSMLMarkSplitter.Chunk;
import org.daisy.pipeline.tts.SoundUtil;
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
import org.daisy.pipeline.tts.synthesize.calabash.impl.TimedTTSExecutor.TimeoutException;
import org.daisy.pipeline.tts.synthesize.calabash.impl.TTSLog.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * TextToPcmThread consumes text from a shared queue. It produces PCM data as
 * output, which in turn are pushed to another shared queue consumed by the
 * EncodingThreads. PCM is produced by calling TTS processors.
 *
 * TTS processors may fail for some reasons, e.g. after a timeout or because the
 * ending SSML mark is missing. In such cases, TextToPcmThread will clean up the
 * resources and attempt to synthesize the current sentence with another TTS
 * processor chosen by the TTSRegistry, unless the error is a MemoryException,
 * in which case the thread gives up on the guilty sentence.
 *
 * The resources of the TTS processors (e.g. sockets) are allocated on-the-fly
 * and are all released at the end of the thread execution.
 *
 */
public class TextToPcmThread implements FormatSpecifications {
	private Logger mLogger;
	private Map<TTSEngine, TTSResource> mResources = new HashMap<TTSEngine, TTSResource>();
	private Map<TTSService, ThreadUnsafeXslTransformer> mTransforms = new HashMap<TTSService, ThreadUnsafeXslTransformer>();
	private int mFileNrInSection; //usually = 0, but incremented when a flush occurs within a section
	private List<SoundFileLink> mSoundFileLinks; //result provided back to the SynthesizeStep caller
	private List<SoundFileLink> mLinksOfCurrentFile; //links under construction
	private Iterable<AudioBuffer> mBuffersOfCurrentFile; //buffers under construction
	private int mOffsetInFile; // reset after every flush
	private int mMemFootprint; //reset after every flush
	private Thread mThread;
	private TimedTTSExecutor mExecutor;
	private TTSRegistry mTTSRegistry;
	private AudioFormat mLastFormat; //used for knowing if a flush is necessary
	private AudioBufferTracker mAudioBufferTracker;
	private SSMLMarkSplitter mSSMLSplitter;
	private Map<String, Object> mTransformParams = new TreeMap<String, Object>();
	private Map<TTSService, CompiledStylesheet> mSSMLTransformers;
	private VoiceManager mVoiceManager;
	private TTSLog mTTSLog;
	private int mErrorCounter;

	void start(final ConcurrentLinkedQueue<ContiguousText> input,
	        final BlockingQueue<ContiguousPCM> pcmOutput, TimedTTSExecutor executor,
	        TTSRegistry ttsregistry, VoiceManager voiceManager, SSMLMarkSplitter ssmlSplitter,
	        final IProgressListener progressListener, Logger logger,
	        AudioBufferTracker AudioBufferTracker, final int maxQueueEltSize,
	        Map<TTSService, CompiledStylesheet> ssmlTransformers, TTSLog ttsLog) {
		mSSMLTransformers = ssmlTransformers;
		mSSMLSplitter = ssmlSplitter;
		mSoundFileLinks = new ArrayList<SoundFileLink>();
		mExecutor = executor;
		mTTSRegistry = ttsregistry;
		mLogger = logger;
		mAudioBufferTracker = AudioBufferTracker;
		mVoiceManager = voiceManager;
		mTTSLog = ttsLog;
		mErrorCounter = 0;
		flush(null, pcmOutput);

		mThread = new Thread() {
			@Override
			public void run() {
				TTSTimeout timeout = new TTSTimeout();

				/* Main loop */
				while (true) {
					ContiguousText section = input.poll();
					if (section == null) { //queue is empty
						break;
					}
					mFileNrInSection = 0;
					boolean breakloop = false;
					for (Sentence sentence : section.sentences) {
						if (breakloop) {
							mErrorCounter++;
							continue;
						}
						try {
							if (!speak(section, sentence, pcmOutput, timeout, maxQueueEltSize)) mErrorCounter++;
						} catch (Throwable t) {
							StringWriter sw = new StringWriter();
							t.printStackTrace(new PrintWriter(sw));
							mErrorCounter++;
							mTTSLog.getWritableEntry(sentence.getID()).addError(
							        new TTSLog.Error(TTSLog.ErrorCode.CRITICAL_ERROR,
							                "the current thread is stopping because of error: "
							                        + sw.toString()));
							breakloop = true;
						}
					}
					System.err.println("Flusing at end of thread " + Thread.currentThread().getId());
					flush(section, pcmOutput);
					progressListener.notifyFinished(section);
				}

				//release the TTS resources
				for (Map.Entry<TTSEngine, TTSResource> e : mResources.entrySet()) {
					try {
						timeout.enableForCurrentThread(2);
						releaseResource(e.getKey(), e.getValue());
					} catch (Exception ex) {
						String msg = "Error while releasing resource of "
						        + TTSServiceUtil.displayName(e.getKey().getProvider()) + "; "
						        + ex.getMessage();
						mLogger.warn(msg);
						mTTSLog.addGeneralError(ErrorCode.WARNING, msg);
					} finally {
						timeout.disable();
					}
				}

				timeout.close();
			}
		};
		mThread.start();
	}

	Collection<SoundFileLink> getSoundFragments() {
		if (mThread != null) {
			try {
				mThread.join();
			} catch (InterruptedException e) {
				//should not happen
				mLogger.warn("TextToPCMThread interruption");
			}
			mThread = null;
		}

		return mSoundFileLinks;
	}

	int getErrorCount() {
		return mErrorCounter;
	}

	private void releaseResource(TTSEngine tts, TTSResource r) {
		if (r == null) {
			return;
		}
		synchronized (r) {
			try {
				tts.releaseThreadResources(r);
			} catch (Throwable t) {
				StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				String msg = "error while releasing resources of "
				        + TTSServiceUtil.displayName(tts.getProvider()) + ": " + sw.toString();
				mTTSLog.addGeneralError(ErrorCode.WARNING, msg);
			}
		}
	}

	private void flush(ContiguousText section, BlockingQueue<ContiguousPCM> pcmOutput) {
		if (section != null && mLinksOfCurrentFile.size() > 0) {
			if (mLastFormat == null) {
				throw new RuntimeException("coding error"); // should not happen
			} else {
				String filePrefix = String.format("part%04d_%02d_%03d", section
				        .getDocumentPosition(), section.getDocumentSplitPosition(),
				        mFileNrInSection);

				ContiguousPCM pcm = new ContiguousPCM(mLastFormat, mBuffersOfCurrentFile,
				        section.getAudioOutputDir(), filePrefix);
				for (SoundFileLink clip : mLinksOfCurrentFile) {
					clip.soundFileURIHolder = pcm.getURIholder();
				}
				try {
					mAudioBufferTracker.transferToEncoding(mMemFootprint, pcm.sizeInBytes());
				} catch (InterruptedException e) {
					// Should never happen since interruptions only occur during calls to TTS processors.
					mLogger.warn("interruption of memory transfer");
				} catch (Error e) {
					// Set here for debugging; improve or remove after finishing debugging
					System.err.println("Other error when transferring to encoding: " + e.getClass().getName() + " for " + mMemFootprint + " with pcm of size " + pcm.sizeInBytes());
					e.printStackTrace();
					throw e;
				}
				pcmOutput.add(pcm);
				pcm = null;
				mSoundFileLinks.addAll(mLinksOfCurrentFile);
				++mFileNrInSection;
			}
		}
		mLinksOfCurrentFile = new ArrayList<SoundFileLink>();
		mBuffersOfCurrentFile = new ArrayList<AudioBuffer>();
		mOffsetInFile = 0;
		mMemFootprint = 0;
		mLastFormat = null;
	}

	/**
	 * Wrapper around TTSService.synthesize() to transform the SSML into string
	 */
	private Collection<AudioBuffer> synthesizeSSML(
			TTSTimeout timeout, TTSTimeout.ThreadFreeInterrupter interrupter, TTSLog.Entry logEntry,
			XdmNode ssml, String sentenceId, int sentenceSize, TTSEngine tts, Voice voice,
			TTSResource threadResources, List<Mark> marks, List<String> expectedMarks
	) throws SaxonApiException, SynthesisException, TimeoutException, MemoryException {
		String transformed = transformSSML(ssml, tts, voice);
		if (transformed != null)
			logEntry.addTTSinput(transformed);
		logEntry.setActualVoice(voice);
		return mExecutor.synthesizeWithTimeout(
			timeout, interrupter, logEntry, transformed, ssml, sentenceSize, tts, voice,
			threadResources, marks, expectedMarks, mAudioBufferTracker, false);
	}

	/**
	 * Wrapper around synthesizeSSML() to handle marks
	 */
	private Iterable<AudioBuffer> synthesize(
			TTSTimeout timeout, TTSTimeout.ThreadFreeInterrupter interrupter, TTSLog.Entry logEntry,
			Sentence sentence, TTSEngine tts, Voice voice, TTSResource threadResources,
			List<Mark> marks, List<String> expectedMarks
	) throws SaxonApiException, SynthesisException, TimeoutException, MemoryException {
		logEntry.resetTTSinput();
		if (tts.endingMark() != null
		        && voice.getMarkSupport() != MarkSupport.MARK_NOT_SUPPORTED){
			//can handle mark
			expectedMarks.set(expectedMarks.size()-1, tts.endingMark());
			return synthesizeSSML(timeout, interrupter, logEntry, sentence.getText(),
			                      sentence.getID(), sentence.getSize(), tts, voice,
			                      threadResources, marks, expectedMarks);
		}
		else {
			Collection<Chunk> chunks = mSSMLSplitter.split(sentence.getText());
			Iterable<AudioBuffer> result = new ArrayList<AudioBuffer>();
			int offset = 0;
			for (Chunk chunk : chunks) {
				Collection<AudioBuffer> buffers = null;
				try {
					buffers = synthesizeSSML(timeout, interrupter, logEntry, chunk.ssml(),
					                         sentence.getID(), Sentence.computeSize(chunk.ssml()),
					                         tts, voice, threadResources, new ArrayList<Mark>(),
					                         expectedMarks);
					mLogger.info("**** ya tengo los buffers");
				} catch (MemoryException | SaxonApiException | SynthesisException
				        | TimeoutException e) {
					//TODO: flush the buffers here
					SoundUtil.cancelFootPrint(result, mAudioBufferTracker);
					throw e;
				} catch (Throwable t) {

					//TODO: flush the buffers here
					SoundUtil.cancelFootPrint(result, mAudioBufferTracker);
					throw new SynthesisException(t);
				}

				if (chunk.leftMark() != null) {
					marks.add(new Mark(chunk.leftMark(), offset));
				}
				mLogger.info("**** ya tengo los buffers - recorriendo");
				for (AudioBuffer b : buffers) {
					offset += b.size;
				}
				mLogger.info("**** ya tengo los buffers - recorridos {}", offset);
				result = Iterables.concat(result, buffers);
			}
			
			//add an empty ending-mark
			marks.add(new Mark(tts.endingMark(), 0));

			return result;
		}
	}

	/**
	 * @return null if something went wrong
	 */
	private Iterable<AudioBuffer> speakWithVoice(final Sentence sentence, Voice v,
	        final TTSEngine tts, List<Mark> marks, List<String> expectedMarks, TTSTimeout timeout)
	        		throws MemoryException {
		//allocate a TTS resource if necessary
		TTSResource resource = mResources.get(tts);
		if (resource == null) {
			try {
				timeout.enableForCurrentThread(3); //3 seconds
				resource = mTTSRegistry.allocateResourceFor(tts);
			} catch (SynthesisException e) {
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(ErrorCode.WARNING,
				                "Error while allocating resources for "
				                        + TTSServiceUtil.displayName(tts.getProvider()) + ": "
				                        + e));

				return null;
			} catch (InterruptedException e) {
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(ErrorCode.WARNING,
				                "Timeout while trying to allocate resources for "
				                        + TTSServiceUtil.displayName(tts.getProvider())));
				return null;
			} finally {
				timeout.disable();
			}
			if (resource == null) {
				//TTS not working anymore?
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(ErrorCode.WARNING, "Could not allocate resource for "
				                + TTSServiceUtil.displayName(tts.getProvider())
				                + " (it has probably been stopped)."));
				return null; //it will try with another TTS
			}
			mResources.put(tts, resource);

			TTSService service = tts.getProvider();
			if (!mTransforms.containsKey(service)) {
				CompiledStylesheet transformer = mSSMLTransformers.get(service);
				mTransforms.put(service, transformer != null ? transformer.newTransformer() : null);
			}
		}

		//convert the input sentence into PCM using the TTS processor
		Iterable<AudioBuffer> pcm = null;
		final TTSResource fresource = resource;
		TTSTimeout.ThreadFreeInterrupter interrupter = new ThreadFreeInterrupter() {
			@Override
			public void threadFreeInterrupt() {
				String msg = "Forcing interruption of the current work of "
				        + TTSServiceUtil.displayName(tts.getProvider()) + "...";
				mLogger.warn(msg);
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(ErrorCode.WARNING, msg));
				tts.interruptCurrentWork(fresource);
			}
		};
		TTSLog.Entry logEntry = mTTSLog.getWritableEntry(sentence.getID());
		try {
			synchronized (resource) {
				if (resource.invalid) {
					String msg = "Resource of "
					        + TTSServiceUtil.displayName(tts.getProvider())
					        + " is no longer valid. The corresponding service has probably been stopped.";
					mLogger.info(msg);
					mTTSLog.getWritableEntry(sentence.getID()).addError(
					        new TTSLog.Error(ErrorCode.WARNING, msg));
					return null;
				}
				pcm = synthesize(timeout, interrupter, logEntry,
				                 sentence, tts, v, resource, marks, expectedMarks);
				mLogger.info("**** ya tengo el pcm");
			}
		} catch (TimeoutException e) {
			logEntry.addError(
			        new TTSLog.Error(ErrorCode.WARNING, "timeout (" + e.getSeconds()
			                + " seconds) fired while speaking with "
			                + TTSServiceUtil.displayName(tts.getProvider())));
			// reset the interrupted state for the next iteration
			Thread.interrupted();
			return null;
		} catch (SynthesisException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logEntry.addError(
			        new TTSLog.Error(ErrorCode.WARNING, "error while speaking with "
			                + TTSServiceUtil.displayName(tts.getProvider()) + " : " + e + ":"
			                + sw.toString()));

			return null;
		} catch (MemoryException e) {
			throw e;
		} catch (SaxonApiException e) {
			logEntry.addError(
			        new TTSLog.Error(ErrorCode.WARNING,
			                "error while transforming SSML with the XSLT of "
			                        + TTSServiceUtil.displayName(tts.getProvider()) + " : "
			                        + e));
			return null;
		}

		//check validity of the result by using the ending mark
		if (marks.size() == 0 || (tts.endingMark() != null && !tts.endingMark().equals(
		                marks.get(marks.size() - 1).name))) {
			SoundUtil.cancelFootPrint(pcm, mAudioBufferTracker);
			logEntry.addError(
			        new TTSLog.Error(ErrorCode.WARNING, "missing ending mark with "
			                + TTSServiceUtil.displayName(tts.getProvider())
			                + ". Number of marks received: " + marks.size()));
			return null;
		}
		
		int marksReceived = marks.size();
		if (expectedMarks.size() != marksReceived){
			SoundUtil.cancelFootPrint(pcm, mAudioBufferTracker);
			mTTSLog.getWritableEntry(sentence.getID()).addError(
			        new TTSLog.Error(ErrorCode.WARNING, "wrong number of marks with "
			                + TTSServiceUtil.displayName(tts.getProvider())
			                + ". Number of marks received: " + marksReceived +", expected number: "+expectedMarks.size()));
			return null;
		}
		for (int i = 0; i < expectedMarks.size(); ++i){
			String expectedMark = expectedMarks.get(i);
			String actualMark = marks.get(i).name;
			if ((actualMark == null && expectedMark != null) || (actualMark != null && !actualMark.equals(expectedMark))){
				SoundUtil.cancelFootPrint(pcm, mAudioBufferTracker);
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(ErrorCode.WARNING, "mark name mismatch with "
				                + TTSServiceUtil.displayName(tts.getProvider())
				                + " actual: " + actualMark+", expected: "+expectedMark));
				return null;
			}
		}
		
		return pcm;
	}
	
	private List<String> getMarkNames(XdmNode ssml){
		XdmSequenceIterator iter = ssml.axisIterator(Axis.DESCENDANT);
		ArrayList<String> markNames = new ArrayList<String>();
		while (iter.hasNext()){
			XdmNode elt = (XdmNode) iter.next();
			if (elt.getNodeKind() == XdmNodeKind.ELEMENT && "mark".equals(elt.getNodeName().getLocalName())){
				markNames.add(elt.getAttributeValue(new QName("name")));
			}
		}
		return markNames;
	}

	/**
	 * @return true when the sentence was successfully converted to speech, false when there was an error
	 */
	private boolean speak(ContiguousText section, Sentence sentence,
	        BlockingQueue<ContiguousPCM> pcmOutput, TTSTimeout timeout, int maxQueueEltSize) {
		
		List<String> expectedMarks = getMarkNames(sentence.getText());
		expectedMarks.add(null); //makes room for the ending-mark
		
		TTSEngine tts = sentence.getTTSproc();
		Voice originalVoice = sentence.getVoice();
		List<Mark> marks = new ArrayList<Mark>();
		Iterable<AudioBuffer> pcm;
		try {
			pcm = speakWithVoice(sentence, originalVoice, tts, marks, expectedMarks, timeout);
		} catch (MemoryException e) {
			flush(section, pcmOutput);
			printMemError(sentence, e);
			return false;
		}
		if (pcm == null) {
			System.err.println("Warning: no pcm generated");
			//release the resource to make it more likely for the next try to succeed
			releaseResource(tts, mResources.get(tts));
			mResources.remove(tts);

			//Find another voice for this sentence
			Voice newVoice = mVoiceManager.findSecondaryVoice(sentence.getVoice());
			if (newVoice == null) {
				String msg = "something went wrong but no fallback voice can be found for "
					+ originalVoice;
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING, msg));
				mLogger.warn(msg + " (see TTS log for more info)");
				return false;
			}
			tts = mVoiceManager.getTTS(newVoice); //cannot return null in this case

			//Try with the new engine
			marks.clear();
			try {
				pcm = speakWithVoice(sentence, newVoice, tts, marks, expectedMarks, timeout);
			} catch (MemoryException e) {
				flush(section, pcmOutput);
				printMemError(sentence, e);
				return false;
			}
			if (pcm == null) {
				String msg = "something went wrong with " + originalVoice
					+ " and fallback voice " + newVoice
					+ " didn't work either";
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING, msg));
				mLogger.warn(msg + " (see TTS log for more info)");
				return false;
			}

			mLogger.info("something went wrong with " + originalVoice + ". Voice " + newVoice
			        + " used instead to synthesize sentence");

			if (mLastFormat != null && !tts.getAudioOutputFormat().matches(mLastFormat))
				flush(section, pcmOutput);
		}
		mLastFormat = tts.getAudioOutputFormat();

		int begin = mOffsetInFile;
		try {
			addBuffers(pcm);
		} catch (InterruptedException e) {
			// Should never happen since interruptions only occur during calls to TTS processors.
			mLogger.warn("interruption exception while queuing the PCM buffers");
		}

		if (marks.size() > 0) {
			//remove the ending mark
			marks = marks.subList(0, marks.size() - 1);
		}

		// keep track of where the sound begins and where it ends within the audio buffers
		if (marks.size() == 0) {
			SoundFileLink sf = new SoundFileLink();
			sf.xmlid = sentence.getID();
			sf.clipBegin = convertBytesToSecond(mLastFormat, begin, mLogger);
			sf.clipEnd = convertBytesToSecond(mLastFormat, mOffsetInFile, mLogger);
			mLinksOfCurrentFile.add(sf);
		} else {
			Map<String, Integer> starts = new HashMap<String, Integer>();
			Map<String, Integer> ends = new HashMap<String, Integer>();
			Set<String> all = new HashSet<String>();

			for (Mark m : marks) {
				String[] mark = m.name.split(FormatSpecifications.MarkDelimiter, -1);
				if (!mark[0].isEmpty()) {
					ends.put(mark[0], m.offsetInAudio);
					all.add(mark[0]);
				}
				if (!mark[1].isEmpty()) {
					starts.put(mark[1], m.offsetInAudio);
					all.add(mark[1]);
				}
			}
			for (String id : all) {
				SoundFileLink sf = new SoundFileLink();
				sf.xmlid = id;
				if (starts.containsKey(id))
					sf.clipBegin = convertBytesToSecond(mLastFormat, begin + starts.get(id), mLogger);
				else
					sf.clipBegin = convertBytesToSecond(mLastFormat, begin, mLogger);
				if (ends.containsKey(id))
					sf.clipEnd = convertBytesToSecond(mLastFormat, begin + ends.get(id), mLogger);
				else
					sf.clipEnd = convertBytesToSecond(mLastFormat, mOffsetInFile, mLogger);
				mLinksOfCurrentFile.add(sf);
			}
			/*
			 * note: if marks.size() > 0 but all.size() == 0, it means that no
			 * marks refer to no ID. It should imply that the sentence contains
			 * skippable elements but no text. In such a case, it is important
			 * to let the script NOT add any fragment, not even the sentence's
			 * parent.
			 */
		}

		if (mMemFootprint > maxQueueEltSize) {
			/*
			 * This flush prevents the TTS processors from raising too many
			 * out-of-memory errors and smoothes the transfers of PCM data to
			 * the encoders.
			 */
			System.err.println("Flusing at speak " + Thread.currentThread().getId());
			flush(section, pcmOutput);
		}
		return true;
	}

	private void printMemError(Sentence sentence, MemoryException e) {
		String msg = "out of memory when processing sentence";
		mLogger.error(msg + " with @id=" + sentence.getID());
		mTTSLog.getWritableEntry(sentence.getID()).addError(
		        new TTSLog.Error(ErrorCode.AUDIO_MISSING, msg));
	}

	private void addBuffers(Iterable<AudioBuffer> toadd) throws InterruptedException {
		for (AudioBuffer b : toadd) {
			mOffsetInFile += b.size;
			mMemFootprint += mAudioBufferTracker.getFootPrint(b);
		}
		mBuffersOfCurrentFile = Iterables.concat(mBuffersOfCurrentFile, toadd);
	}

	private static double convertBytesToSecond(AudioFormat format, int bytes, Logger mLogger) {
		mLogger.info("****convertBytesToSecond: {}, {}, {} ---- {}", bytes, format.getFrameRate(), format.getFrameSize(), bytes / (format.getFrameRate() * format.getFrameSize()));
		return (bytes / (format.getFrameRate() * format.getFrameSize()));
	}

	private String transformSSML(XdmNode ssml, TTSEngine engine, Voice v)
	        throws SaxonApiException {
		ThreadUnsafeXslTransformer transformer = mTransforms.get(engine.getProvider());
		if (transformer == null)
			return null;
		mTransformParams.put("voice", v.name);
		if (engine.endingMark() != null)
			mTransformParams.put("ending-mark", engine.endingMark());
		return transformer.transformToString(ssml, mTransformParams);
	}
}
