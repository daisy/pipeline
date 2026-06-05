package org.daisy.pipeline.tts.calabash.impl;

import java.io.File;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;

import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.pipeline.audio.AudioClip;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioFootprintMonitor;
import org.daisy.pipeline.tts.TTSLog;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.TTSLog.ErrorCode;

/**
 * Consumes a shared queue of PCM packets. PCM packets are then provided to
 * audio encoders. The thread stops when it receives an 'EndOfQueue' marker.
 */
public class EncodingThread {

	class EncodingException extends RuntimeException {
		public EncodingException(String message, Throwable cause) {
			super(message, cause);
		}
		public EncodingException(Throwable t) {
			super(t);
		}
		public EncodingException(String message) {
			super(message);
		}
	}
	
	private Thread mThread;
	private Throwable criticalError;

	void start(final AudioFileFormat.Type fileType, final AudioServices encoderRegistry,
	        final BlockingQueue<ContiguousPCM> inputPCM, final AudioFootprintMonitor audioFootprintMonitor,
	        Map<String, String> TTSproperties, final TTSLog ttslog, MessageAppender messageAppender) {

		//max seconds of encoded audio per seconds of encoding
		//it would be more accurate with a byte rate instead, but less intuitive
		float encodingSpeed = 2.0f;
		String speedProp = "org.daisy.pipeline.tts.encoding.speed";
		String speedParam = TTSproperties.get(speedProp);
		if (speedParam != null) {
			try {
				encodingSpeed = Float.valueOf(speedParam);
			} catch (NumberFormatException e) {
				ttslog.addGeneralError(
					ErrorCode.WARNING,
					"wrong format for property " + speedProp + ". A float is expected, not " + speedParam,
					e);
			}
		}

		//Eventually, we should select the encoder using the audio format as criterion, but for now
		//we always employ the same encoder for every chunk of PCM
		AudioEncoder encoder = encoderRegistry.newEncoder(fileType, TTSproperties).orElse(null);
		if (encoder == null) {
			ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, "No audio encoder found");
		}

		final AudioEncoder fencoder = encoder;
		final float fEncodingSpeed = encodingSpeed;
		final TTSTimeout timeout = new TTSTimeout();

		mThread = new Thread() {
			@Override
			public void run() {
				// wrap the messages from this thread in a (empty) block so that there is always an
				// active block for this thread, so that SLF4J log messages always have a destination
				MessageAppender messageThread = messageAppender != null
					? messageAppender.append(new MessageBuilder())
					: null;
				try {
					while (!interrupted()) {
						ContiguousPCM job;
						try {
							job = inputPCM.take();
						} catch (InterruptedException e) {
							ttslog.addGeneralError(
								ErrorCode.CRITICAL_ERROR, "encoding thread has been interrupted");
							break; //warning: encoding bytes are not freed
						}
						if (job.isEndOfQueue()) {
							//nothing to release
							break;
						}
						int jobSize = job.sizeInBytes();
						// FIXME: why do we end up in endless loop when encoder is null??
						if (fencoder != null) {
							AudioInputStream audio = job.getAudio();
							float secs = jobSize / audio.getFormat().getFrameRate();
							int maxTime = (int) (1.0 + secs / fEncodingSpeed);
							timeout.enableForCurrentThread(maxTime);
							try {
								File encodedFile = new File(
									job.getDestinationDirectory(),
									job.getDestinationFilePrefix() + "." + fileType.getExtension());
								AudioClip clip = fencoder.encode(
									audio,
									fileType,
									encodedFile);
								audio.close();
								job.getDestinationFile().set(clip);
							} catch (InterruptedException e) {
								ttslog.addGeneralError(
									ErrorCode.CRITICAL_ERROR,
									"timeout while encoding audio to " + job.getDestinationFilePrefix(),
									e);
								audioFootprintMonitor.releaseEncodersMemory(jobSize);
								throw new EncodingException(e);
							} catch (Throwable t) {
								ttslog.addGeneralError(
									ErrorCode.CRITICAL_ERROR,
									"error while encoding audio to " + job.getDestinationFilePrefix(),
									t);
								audioFootprintMonitor.releaseEncodersMemory(jobSize);
								throw new EncodingException(t);
							} finally {
								timeout.disable();
							}
						}
						audioFootprintMonitor.releaseEncodersMemory(jobSize);
					}
				} finally {
					timeout.close();
					if (messageThread != null)
						messageThread.close();
				}
			}
		};
		mThread.setUncaughtExceptionHandler(
			(thread, throwable) -> { criticalError = throwable; }
		);
		mThread.start();
	}

	void waitToFinish() throws EncodingException {
		try {
			mThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(); // should not happen
		}
		if (criticalError != null) {
			if (criticalError instanceof EncodingException)
				throw (EncodingException)criticalError;
			else
				throw new RuntimeException("coding error", criticalError); // should not happen
		}
	}
}
