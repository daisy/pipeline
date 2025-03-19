package org.daisy.pipeline.tts.synthesize.calabash.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioBufferTracker;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.synthesize.calabash.impl.TTSLog.ErrorCode;

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

	void start(final AudioServices encoderRegistry,
	        final BlockingQueue<ContiguousPCM> inputPCM, final Logger logger,
	        final AudioBufferTracker audioBufferTracker, Map<String, String> TTSproperties,
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
		AudioEncoder encoder = encoderRegistry.getEncoder();
		AudioEncoder.EncodingOptions encodingOptions = null;
		if (encoder == null) {
			String msg = "No audio encoder found";
			logger.info(msg);
			ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
		} else {
			encodingOptions = encoder.parseEncodingOptions(TTSproperties);
			try {
				encoder.test(encodingOptions);
			} catch (Exception e) {
				String msg = "audio encoder does not work: " + getStack(e);
				logger.info(msg);
				ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
				encoder = null;
			}
		}

		final AudioEncoder.EncodingOptions options = encodingOptions;
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
							UUID uuid = UUID.randomUUID();
							logger.info("{}*****inputPCM.take() INI", uuid);
							job = inputPCM.take();
							logger.info("{}*****inputPCM.take() FIN", uuid);
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
						if (fencoder != null) {
							float secs = jobSize / (job.getAudioFormat().getFrameRate());
							int maxTime = (int) (1.0 + secs / fEncodingSpeed);
							try {
								logger.info("*****fencoder.encode INI");
								timeout.enableForCurrentThread(maxTime);
								Optional<String> destURI = fencoder.encode(job.getBuffers(), job
								                                           .getAudioFormat(), job.getDestinationDirectory(), job
								                                           .getDestinationFilePrefix(), options);
								
//								File encodedFile = new File(job.getDestinationDirectory(), job.getDestinationFilePrefix() + ".mp3");
//								logger.info("*****escribiendo MP3 en el fichero {}", encodedFile.toURI().toString());
//								// voy a guardar los buffers a mano a ver que hace
//								try (FileOutputStream out = new FileOutputStream(encodedFile)) {
//									for (AudioBuffer b : job.getBuffers()) {
//										out.write(b.data, 0, b.size);
//										out.flush();
//									}
//								}
//								Optional<String> destURI = Optional.of(encodedFile.toURI().toString());
								logger.info("*****fencoder.encode FIN");
								if (destURI.isPresent()) {
									job.getURIholder().append(destURI.get());
								} else {
									String msg = "Audio encoder failed to encode to "
										+ job.getDestinationFilePrefix();
									ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
									logger.info(msg);
								}
							} catch (InterruptedException e) {
								String msg = "timeout while encoding audio to "
									+ job.getDestinationFilePrefix() + ": " + getStack(e);
								ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
								audioBufferTracker.releaseEncodersMemory(jobSize);
								logger.error(msg, e);
								throw new EncodingException(e);
							} catch (Throwable t) {
								String msg = "error while encoding audio to "
									+ job.getDestinationFilePrefix() + ": " + getStack(t);
								ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
								audioBufferTracker.releaseEncodersMemory(jobSize);
								logger.error(msg, t);
								throw new EncodingException(t);
							} finally {
								timeout.disable();
							}
						}
						audioBufferTracker.releaseEncodersMemory(jobSize);
					}
				} finally {
					timeout.close();
				}
			}
		};
		mThread.setUncaughtExceptionHandler(
			(thread, throwable) -> {
				logger.error("***** critical error not null", throwable); criticalError = throwable; }
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
