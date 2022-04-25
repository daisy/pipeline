package org.daisy.pipeline.tts.calabash.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.pipeline.tts.AudioFootprintMonitor;
import org.daisy.pipeline.tts.AudioFootprintMonitor.MemoryException;
import org.daisy.pipeline.tts.SSMLMarkSplitter;
import org.daisy.pipeline.tts.SSMLMarkSplitter.Chunk;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSEngine.SynthesisResult;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.TTSTimeout.ThreadFreeInterrupter;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.Voice.MarkSupport;
import org.daisy.pipeline.tts.VoiceManager;
import org.daisy.pipeline.tts.calabash.impl.TimedTTSExecutor.TimeoutException;
import org.daisy.pipeline.tts.calabash.impl.TTSLog.ErrorCode;

import org.slf4j.Logger;

import com.google.common.collect.Iterables;

/**
 * TextToPcmThread consumes text from a shared queue. It produces PCM data as
 * output, which in turn are pushed to another shared queue consumed by the
 * EncodingThreads. PCM is produced by calling TTS processors.
 *
 * TTS processors may fail for some reasons, e.g. after a timeout, or if marks
 * are missing. In such cases, TextToPcmThread will clean up the resources and
 * attempt to synthesize the current sentence with another TTS processor chosen
 * by the TTSRegistry, unless the error is a MemoryException, in which case the
 * thread gives up on the guilty sentence.
 *
 * The resources of the TTS processors (e.g. sockets) are allocated on-the-fly
 * and are all released at the end of the thread execution.
 *
 */
public class TextToPcmThread implements FormatSpecifications {
	private Logger mLogger;
	private Map<TTSEngine, TTSResource> mResources = new HashMap<TTSEngine, TTSResource>();
	private int mFileNrInSection; //usually = 0, but incremented when a flush occurs within a section
	private List<SoundFileLink> mSoundFileLinks; //result provided back to the SynthesizeStep caller
	private List<SoundFileLink> mLinksOfCurrentFile; //links under construction
	private Iterable<AudioInputStream> mAudioOfCurrentFile; // audio file under construction
	private int mOffsetInFile; // reset after every flush
	private int mMemFootprint; //reset after every flush
	private Thread mThread;
	private TimedTTSExecutor mExecutor;
	private TTSRegistry mTTSRegistry;
	private AudioFormat mLastFormat; //used for knowing if a flush is necessary
	private AudioFootprintMonitor mAudioFootprintMonitor;
	private SSMLMarkSplitter mSSMLSplitter;
	private VoiceManager mVoiceManager;
	private TTSLog mTTSLog;
	private int mErrorCounter;

	/**
	 * Java counterpart of SSML's marks
	 */
	private class Mark {
		public Mark(String name, int offset) {
			this.offsetInAudio = offset;
			this.name = name;
		}
		/**
		 * Name
		 */
		public String name;
		/**
		 * Offset in bytes
		 */
		public int offsetInAudio;
	}

	/**
	 * @param totalTextSize Total size of all text (not only the text contained in <code>input</code>)
	 * @param portion       Estimated portion of the text that this thread will process.
	 */
	void start(final ConcurrentLinkedQueue<ContiguousText> input,
	           final BlockingQueue<ContiguousPCM> pcmOutput, TimedTTSExecutor executor,
	           TTSRegistry ttsregistry, VoiceManager voiceManager, SSMLMarkSplitter ssmlSplitter,
	           Logger logger, AudioFootprintMonitor audioFootprintMonitor, final int maxQueueEltSize,
	           TTSLog ttsLog, MessageAppender messageAppender,
	           long totalTextSize, BigDecimal portion) {
		mSSMLSplitter = ssmlSplitter;
		mSoundFileLinks = new ArrayList<SoundFileLink>();
		mExecutor = executor;
		mTTSRegistry = ttsregistry;
		mLogger = logger;
		mAudioFootprintMonitor = audioFootprintMonitor;
		mVoiceManager = voiceManager;
		mTTSLog = ttsLog;
		mErrorCounter = 0;
		flush(null, pcmOutput);

		mThread = new Thread() {
			@Override
			public void run() {
				// wrap the messages from this thread in a (empty) block so that there is always an
				// active block for this thread, so that SLF4J log messages always have a destination
				MessageAppender messageThread = messageAppender != null
					? messageAppender.append(new MessageBuilder().withProgress(portion))
					: null;
				TTSTimeout timeout = new TTSTimeout();
				try {

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
								mErrorCounter++;
								mTTSLog.getWritableEntry(sentence.getID()).addError(
									new TTSLog.Error(
										TTSLog.ErrorCode.CRITICAL_ERROR, "the current thread is stopping because of an error", t));
								breakloop = true;
							}
						}
						flush(section, pcmOutput);

						// update progress
						if (messageThread != null && portion.compareTo(BigDecimal.ZERO) > 0) {
							MessageBuilder m = new MessageBuilder()
								.withProgress(
									new BigDecimal(section.getStringSize()).divide(new BigDecimal(totalTextSize), MathContext.DECIMAL128)
									                                       .divide(portion, MathContext.DECIMAL128)
									                                       .min(BigDecimal.ONE));
							messageThread.append(m).close();
						}
					}

					//release the TTS resources
					for (Map.Entry<TTSEngine, TTSResource> e : mResources.entrySet()) {
						timeout.enableForCurrentThread(2);
						try {
							releaseResource(e.getKey(), e.getValue());
						} catch (Exception ex) {
							mTTSLog.addGeneralError(
								ErrorCode.WARNING,
								"Error while releasing resource of " + e.getKey().getProvider().getName() + ex.getMessage(),
								ex);
						} finally {
							timeout.disable();
						}
					}
				} finally {
					timeout.close();
					if (messageThread != null)
						messageThread.close(); // sets progress to 100% if not already 100%
				}
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
				mTTSLog.addGeneralError(
					ErrorCode.WARNING, "error while releasing resources of " + tts.getProvider().getName(), t);
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

				ContiguousPCM pcm = new ContiguousPCM(concat(mAudioOfCurrentFile),
				        section.getAudioOutputDir(), filePrefix);
				for (SoundFileLink clip : mLinksOfCurrentFile) {
					clip.soundFileURIHolder = pcm.getURIholder();
				}
				try {
					mAudioFootprintMonitor.transferToEncoding(mMemFootprint, pcm.sizeInBytes());
				} catch (InterruptedException e) {
					// Should never happen since interruptions only occur during calls to TTS processors.
					mLogger.warn("interruption of memory transfer");
				}
				pcmOutput.add(pcm);
				pcm = null;
				mSoundFileLinks.addAll(mLinksOfCurrentFile);
				++mFileNrInSection;
			}
		}
		mLinksOfCurrentFile = new ArrayList<SoundFileLink>();
		mAudioOfCurrentFile = new ArrayList<AudioInputStream>();
		mOffsetInFile = 0;
		mMemFootprint = 0;
		mLastFormat = null;
	}

	/**
	 * Wrapper around {@link TimedTTSExecutor#synthesizeWithTimeout()} to handle
	 * marks. Returns a sequence of {@link AudioInputStream} with the same audio
	 * format.
	 */
	private Iterable<AudioInputStream> synthesize(
			TTSTimeout timeout, TTSTimeout.ThreadFreeInterrupter interrupter, TTSLog.Entry logEntry,
			Sentence sentence, TTSEngine tts, Voice voice, TTSResource threadResources,
			List<Mark> marks, List<String> markNames
	) throws SynthesisException, TimeoutException, MemoryException {
		if (tts.handlesMarks()
		        && voice.getMarkSupport() != MarkSupport.MARK_NOT_SUPPORTED){
			logEntry.setActualVoice(voice);
			SynthesisResult result = mExecutor.synthesizeWithTimeout(
				timeout, interrupter, logEntry, sentence.getText(), sentence.getSize(), tts, voice,
				threadResources);
			List<Integer> markOffsets = result.marks;
			if (markNames.size() != markOffsets.size()) {
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(ErrorCode.WARNING, "wrong number of marks with "
				                + tts.getProvider().getName()
				                + ". Number of marks received: " + markOffsets.size()
				                + ", expected number: " + markNames.size()));
				return null;
			}
			for (int i = 0; i < markNames.size(); i++) {
				marks.add(new Mark(markNames.get(i), markOffsets.get(i)));
			}
			mAudioFootprintMonitor.acquireTTSMemory(result.audio);
			return Collections.singletonList(result.audio);
		} else {
			Collection<Chunk> chunks = mSSMLSplitter.split(sentence.getText());
			List<AudioInputStream> result = new ArrayList<>();
			int offset = 0;
			for (Chunk chunk : chunks) {
				logEntry.setActualVoice(voice);
				try {
					AudioInputStream stream = mExecutor.synthesizeWithTimeout(
						timeout, interrupter, logEntry, chunk.ssml(), Sentence.computeSize(chunk.ssml()),
						tts, voice, threadResources).audio;
					if (chunk.leftMark() != null) {
						marks.add(new Mark(chunk.leftMark(), offset));
					}
					int size = AudioFootprintMonitor.getFootprint(stream);
					offset += size;
					mAudioFootprintMonitor.acquireTTSMemory(size);
					result.add(stream);
				} catch (MemoryException | SynthesisException | TimeoutException e) {
					// TODO: flush here
					for (AudioInputStream s : result)
						mAudioFootprintMonitor.releaseTTSMemory(s);
					throw e;
				} catch (Throwable t) {
					// TODO: flush here
					for (AudioInputStream s : result)
						mAudioFootprintMonitor.releaseTTSMemory(s);
					throw new SynthesisException(t);
				}
			}
			if (markNames.size() != marks.size()) {
				throw new RuntimeException(); // should not happen
			}
			return result;
		}
	}

	/**
	 * @return null if something went wrong
	 */
	private Iterable<AudioInputStream> speakWithVoice(final Sentence sentence, Voice v,
	        final TTSEngine tts, List<Mark> marks, List<String> markNames, TTSTimeout timeout)
	        		throws MemoryException {
		//allocate a TTS resource if necessary
		TTSResource resource = mResources.get(tts);
		if (resource == null) {
			timeout.enableForCurrentThread(3);
			try {
				resource = mTTSRegistry.allocateResourceFor(tts);
			} catch (SynthesisException e) {
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(ErrorCode.WARNING,
				                "Error while allocating resources for "
				                        + tts.getProvider().getName() + ": "
				                        + e));

				return null;
			} catch (InterruptedException e) {
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(ErrorCode.WARNING,
				                "Timeout while trying to allocate resources for "
				                        + tts.getProvider().getName()));
				return null;
			} finally {
				timeout.disable();
			}
			if (resource == null) {
				//TTS not working anymore?
				mTTSLog.getWritableEntry(sentence.getID()).addError(
				        new TTSLog.Error(ErrorCode.WARNING, "Could not allocate resource for "
				                + tts.getProvider().getName()
				                + " (it has probably been stopped)."));
				return null; //it will try with another TTS
			}
			mResources.put(tts, resource);
		}

		//convert the input sentence into PCM using the TTS processor
		final TTSResource fresource = resource;
		TTSTimeout.ThreadFreeInterrupter interrupter = new ThreadFreeInterrupter() {
			@Override
			public void threadFreeInterrupt() {
				mTTSLog.getWritableEntry(sentence.getID()).addError(
					new TTSLog.Error(
						ErrorCode.WARNING,
						"Forcing interruption of the current work of " + tts.getProvider().getName() + "..."));
				tts.interruptCurrentWork(fresource);
			}
		};
		TTSLog.Entry logEntry = mTTSLog.getWritableEntry(sentence.getID());
		try {
			synchronized (resource) {
				if (resource.invalid) {
					mTTSLog.getWritableEntry(sentence.getID()).addError(
						new TTSLog.Error(
							ErrorCode.WARNING,
							"Resource of " + tts.getProvider().getName()
							+ " is no longer valid. The corresponding service has probably been stopped."));
					return null;
				}
				return synthesize(timeout, interrupter, logEntry,
				                 sentence, tts, v, resource, marks, markNames);
			}
		} catch (TimeoutException e) {
			logEntry.addError(
			        new TTSLog.Error(ErrorCode.WARNING, "timeout (" + e.getSeconds()
			                + " seconds) fired while speaking with "
			                + tts.getProvider().getName()));
			return null;
		} catch (SynthesisException e) {
			logEntry.addError(
				new TTSLog.Error(
					ErrorCode.WARNING, "error while speaking with " + tts.getProvider().getName() + ": " + e, e));

			return null;
		}
	}
	
	static List<String> getMarkNames(XdmNode ssml) {
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
		
		List<String> markNames = getMarkNames(sentence.getText());
		
		TTSEngine tts = sentence.getTTSproc();
		Voice originalVoice = sentence.getVoice();
		List<Mark> marks = new ArrayList<Mark>();
		Iterable<AudioInputStream> pcm;
		try {
			pcm = speakWithVoice(sentence, originalVoice, tts, marks, markNames, timeout);
		} catch (MemoryException e) {
			flush(section, pcmOutput);
			printMemError(sentence, e);
			return false;
		}
		if (pcm == null) {
			//release the resource to make it more likely for the next try to succeed
			releaseResource(tts, mResources.get(tts));
			mResources.remove(tts);

			//Find another voice for this sentence
			Voice newVoice = mVoiceManager.findSecondaryVoice(sentence.getVoice());
			if (newVoice == null) {
				mTTSLog.getWritableEntry(sentence.getID()).addError(
					new TTSLog.Error(
						TTSLog.ErrorCode.AUDIO_MISSING,
						"something went wrong but no fallback voice can be found for " + originalVoice));
				return false;
			}
			tts = mVoiceManager.getTTS(newVoice); //cannot return null in this case

			//Try with the new engine
			marks.clear();
			try {
				pcm = speakWithVoice(sentence, newVoice, tts, marks, markNames, timeout);
			} catch (MemoryException e) {
				flush(section, pcmOutput);
				printMemError(sentence, e);
				return false;
			}
			if (pcm == null) {
				mTTSLog.getWritableEntry(sentence.getID()).addError(
					new TTSLog.Error(
						TTSLog.ErrorCode.AUDIO_MISSING,
						"something went wrong with " + originalVoice
						+ " and fallback voice " + newVoice + " didn't work either"));
				return false;
			}

			mLogger.info("something went wrong with " + originalVoice + ". Voice " + newVoice
			        + " used instead to synthesize sentence");

			if (mLastFormat != null && !pcm.iterator().next().getFormat().matches(mLastFormat))
				flush(section, pcmOutput);
		}
		mLastFormat = pcm.iterator().next().getFormat();

		int begin = mOffsetInFile;
		addAudio(pcm);

		// keep track of where the sound begins and where it ends within the audio file
		if (marks.size() == 0) {
			SoundFileLink sf = new SoundFileLink();
			sf.xmlid = sentence.getID();
			sf.clipBegin = convertBytesToSecond(mLastFormat, begin);
			sf.clipEnd = convertBytesToSecond(mLastFormat, mOffsetInFile);
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
					sf.clipBegin = convertBytesToSecond(mLastFormat, begin + starts.get(id));
				else
					sf.clipBegin = convertBytesToSecond(mLastFormat, begin);
				if (ends.containsKey(id))
					sf.clipEnd = convertBytesToSecond(mLastFormat, begin + ends.get(id));
				else
					sf.clipEnd = convertBytesToSecond(mLastFormat, mOffsetInFile);
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
			flush(section, pcmOutput);
		}
		return true;
	}

	private void printMemError(Sentence sentence, MemoryException e) {
		String msg = "out of memory";
		mTTSLog.getWritableEntry(sentence.getID()).addError(
			new TTSLog.Error(ErrorCode.AUDIO_MISSING, msg));
	}

	private void addAudio(Iterable<AudioInputStream> toadd) {
		for (AudioInputStream b : toadd) {
			int size = AudioFootprintMonitor.getFootprint(b);
			mOffsetInFile += size;
			mMemFootprint += size;
		}
		mAudioOfCurrentFile = Iterables.concat(mAudioOfCurrentFile, toadd);
	}

	/**
	 * Concatenate a sequence of {@link AudioInputStream} into a single {@link AudioInputStream}.
	 */
	private static AudioInputStream concat(Iterable<AudioInputStream> streams) {
		int count = 0;
		long _totalLength = 0;
		AudioFormat format = null;
		for (AudioInputStream s : streams) {
			count++;
			_totalLength += s.getFrameLength();
			if (format == null)
				format = s.getFormat();
			else if (!format.matches(s.getFormat()))
				throw new IllegalArgumentException("Can not concatenate AudioInputStream with different audio formats");
		}
		if (count == 0)
			throw new IllegalArgumentException("At least one AudioInputStream expected");
		if (count == 1)
			return streams.iterator().next();
		long totalLength = _totalLength;
		int frameSize = format.getFrameSize();
		return new AudioInputStream(
			new InputStream() {
				Iterator<AudioInputStream> nextStreams = streams.iterator();
				AudioInputStream stream = null;
				byte[] frame = new byte[frameSize];
				long availableFrames = totalLength;
				int availableInFrame = 0;
				public int read() throws IOException {
					if (availableFrames == 0 && availableInFrame == 0)
						return -1;
					if (availableInFrame > 0)
						return frame[frameSize - (availableInFrame--)] & 0xFF;
					if (stream != null) {
						availableInFrame = stream.read(frame);
						if (availableInFrame > 0) {
							availableFrames--;
							return frame[frameSize - (availableInFrame--)] & 0xFF;
						}
					}
					try {
						if (stream != null)
							stream.close();
						stream = nextStreams.next();
					} catch (NoSuchElementException e) {
						return -1;
					}
					return read();
				}
				public int available() {
					try {
						return Math.toIntExact(Math.multiplyExact(availableFrames, frameSize)) + availableInFrame;
					} catch (ArithmeticException e) {
						return Integer.MAX_VALUE;
					}
				}
				public void close() throws IOException {
					if (stream != null)
						stream.close();
					while (nextStreams.hasNext())
						nextStreams.next().close();
				}
			},
			format,
			totalLength);
	}

	private static double convertBytesToSecond(AudioFormat format, int bytes) {
		return (bytes / (format.getFrameRate() * format.getFrameSize()));
	}
}
