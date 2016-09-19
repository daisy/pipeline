package org.daisy.pipeline.tts.synthesize;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioBufferTracker;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.synthesize.TTSLog.ErrorCode;

import com.google.common.base.Optional;

/**
 * Consumes a shared queue of PCM packets. PCM packets are then provided to
 * audio encoders. The thread stops when it receives an 'EndOfQueue' marker.
 */
public class EncodingThread {

	private Thread mThread;

	void start(final AudioServices encoderRegistry,
	        final BlockingQueue<ContiguousPCM> inputPCM, final IPipelineLogger logger,
	        final AudioBufferTracker audioBufferTracker, Map<String, String> TTSproperties,
	        final TTSLog ttslog) {

		//max seconds of encoded audio per seconds of encoding
		//it would be more accurate with a byte rate instead, but less intuitive
		float encodingSpeed = 2.0f;
		String speedProp = "encoding.speed";
		String speedParam = TTSproperties.get(speedProp);
		if (speedParam != null) {
			try {
				encodingSpeed = Float.valueOf(speedParam);
			} catch (NumberFormatException e) {
				String msg = "wrong format for property " + speedProp
				        + ". A float is expected, not " + speedParam;
				logger.printInfo(msg);
				ttslog.addGeneralError(ErrorCode.WARNING, msg);
			}
		}

		//Eventually, we should select the encoder using the audio format as criterion, but for now
		//we always employ the same encoder for every chunk of PCM
		AudioEncoder encoder = encoderRegistry.getEncoder();
		AudioEncoder.EncodingOptions encodingOptions = null;
		if (encoder == null) {
			String msg = "No audio encoder found";
			logger.printInfo(msg);
			ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
		} else {
			encodingOptions = encoder.parseEncodingOptions(TTSproperties);
			try {
				encoder.test(encodingOptions);
			} catch (Exception e) {
				String msg = "audio encoder does not work: " + getStack(e);
				logger.printInfo(msg);
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
				while (!interrupted()) {
					ContiguousPCM job;
					try {
						job = inputPCM.take();
					} catch (InterruptedException e) {
						String msg = "encoding thread has been interrupted";
						logger.printInfo(msg);
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
							timeout.enableForCurrentThread(maxTime);
							Optional<String> destURI = fencoder.encode(job.getBuffers(), job
							        .getAudioFormat(), job.getDestinationDirectory(), job
							        .getDestinationFilePrefix(), options);
							if (destURI.isPresent()) {
								job.getURIholder().append(destURI.get());
							} else {
								String msg = "Audio encoder failed to encode to "
								        + job.getDestinationFilePrefix();
								ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
							}
						} catch (InterruptedException e) {
							String msg = "timeout while encoding audio to "
							        + job.getDestinationFilePrefix() + ": " + getStack(e);
							ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
						} catch (Throwable t) {
							String msg = "error while encoding audio to "
							        + job.getDestinationFilePrefix() + ": " + getStack(t);
							ttslog.addGeneralError(ErrorCode.CRITICAL_ERROR, msg);
						} finally {
							timeout.disable();
						}
					}
					job = null;
					audioBufferTracker.releaseEncodersMemory(jobSize);
				}

				timeout.close();
			}
		};
		mThread.start();
	}

	void waitToFinish() {
		try {
			mThread.join();
		} catch (InterruptedException e) {
			//should not happen
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
