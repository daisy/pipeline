package org.daisy.pipeline.tts.calabash.impl;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;

import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioFootprintMonitor;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.calabash.impl.TTSLog.ErrorCode;

import org.slf4j.Logger;

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
	        final BlockingQueue<ContiguousPCM> inputPCM, final Logger logger,
	        final AudioFootprintMonitor audioFootprintMonitor, Map<String, String> TTSproperties,
	        final TTSLog ttslog) {

		//max seconds of encoded audio per seconds of encoding
		//it would be more accurate with a byte rate instead, but less intuitive
		float encodingSpeed = 2.0f;
		String speedProp = "org.daisy.pipeline.tts.encoding.speed";
		String speedParam = TTSproperties.get(speedProp);
		if (speedParam != null) {
			try {
				encodingSpeed = Float.valueOf(speedParam);
			} catch (NumberFormatException e) {
				String msg = "wrong format for property " + speedProp
				        + ". A float is expected, not " + speedParam;
				logger.info(msg);
				ttslog.addGeneralError(ErrorCode.WARNING, msg);
			}
		}

		//Eventually, we should select the encoder using the audio format as criterion, but for now
		//we always employ the same encoder for every chunk of PCM
		AudioEncoder encoder = encoderRegistry.newEncoder(fileType, TTSproperties).orElse(null);
		if (encoder == null) {
			String msg = "No audio encoder found";
			logger.info(msg);
			ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
		}

		final AudioEncoder fencoder = encoder;
		final float fEncodingSpeed = encodingSpeed;
		final TTSTimeout timeout = new TTSTimeout();

		mThread = new Thread() {
			@Override
			public void run() {
				try {
					while (!interrupted()) {
						ContiguousPCM job;
						try {
							job = inputPCM.take();
						} catch (InterruptedException e) {
							String msg = "encoding thread has been interrupted";
							logger.info(msg);
							ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
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
								fencoder.encode(
									audio,
									fileType,
									encodedFile);
								audio.close();
								job.getURIholder().append(encodedFile.toURI().toString());
							} catch (InterruptedException e) {
								String msg = "timeout while encoding audio to "
									+ job.getDestinationFilePrefix() + ": " + getStack(e);
								ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
								audioFootprintMonitor.releaseEncodersMemory(jobSize);
								throw new EncodingException(e);
							} catch (Throwable t) {
								String msg = "error while encoding audio to "
									+ job.getDestinationFilePrefix() + ": " + getStack(t);
								ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
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
				}
			}
		};
		mThread.setUncaughtExceptionHandler(
			(thread, throwable) -> { criticalError = throwable; }
		);
		mThread.start();
	}

	void waitToFinish() throws EncodingException {
		if (criticalError != null) {
			if (criticalError instanceof EncodingException)
				throw (EncodingException)criticalError;
			else
				throw new RuntimeException("coding error");
		}
		try {
			mThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(); // should not happen
		}
	}

	//TODO: move this method in some kind of utils/helpers
	private static String getStack(Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		t.printStackTrace(printWriter);
		printWriter.flush();
		return writer.toString();
	}
}
