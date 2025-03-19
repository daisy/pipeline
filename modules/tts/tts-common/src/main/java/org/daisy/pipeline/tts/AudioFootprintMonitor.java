package org.daisy.pipeline.tts;

import java.util.concurrent.Semaphore;

import javax.sound.sampled.AudioInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AudioFootprintMonitor monitors the footprint of the audio data created by the
 * TTS processors. The audio data goes through two virtual memory spaces: a
 * space for the TTS and a space for the encoding. The former is bigger and
 * throws exceptions if there is no space left, rather than possibly deadlocking
 * on blocking requests. The latter can afford to block on the requests, since
 * if the space is full, it will be consumed by the encoding threads.
 */
public class AudioFootprintMonitor {

	public class MemoryException extends Exception {
		public MemoryException(String message) {
			super(message);
		}

		public MemoryException(int bytes) {
			super("tried to allocate " + bytes + " bytes (" + bytes / 1000000.0 + "MB)");
		}
	}

	private Logger ServerLogger = LoggerFactory.getLogger(AudioFootprintMonitor.class);

	public AudioFootprintMonitor() {
		String maxMemProp = System.getProperty("org.daisy.pipeline.tts.maxmem");
		long maxMem;
		if (maxMemProp != null) {
			maxMem = Long.valueOf(maxMemProp) * 1048576;
		} else {
			maxMem = Runtime.getRuntime().maxMemory();
			if (maxMem == Long.MAX_VALUE)
				maxMem = 500 * 1048576; //500 MB
			else
				maxMem /= 2; //50% of the total memory
		}
		if (maxMem > Integer.MAX_VALUE)
			maxMem = Integer.MAX_VALUE;

		mSpaceForTTS = (int) (2 * maxMem / 3);
		mSpaceForEncoding = (int) (maxMem - mSpaceForTTS);

		//the padding prevents big-size acquire() from blocking the small ones
		mPaddedEncodingSpace = mSpaceForEncoding - mSpaceForEncoding / 10;

		mTTSCounter = new Semaphore(mSpaceForTTS, false);
		mEncodingCounter = new Semaphore(mSpaceForEncoding, false);
	}

	public void acquireTTSMemory(AudioInputStream audio) throws MemoryException {
		acquireTTSMemory(getFootprint(audio));
	}

	public void acquireTTSMemory(int size) throws MemoryException {
		ServerLogger.trace("Allocating {} in buffer from tts", size);
		if (!mTTSCounter.tryAcquire(size))
			throw new MemoryException(size);
	}

	/**
	 * Must not be used by the encoders. They should use releaseEncodersMemory()
	 * instead.
	 */
	public void releaseTTSMemory(AudioInputStream audio) {
		releaseTTSMemory(getFootprint(audio));
	}

	public void releaseTTSMemory(int size) {
		ServerLogger.trace("About to release {} of {} in buffer", size, mTTSCounter.availablePermits());
		mTTSCounter.release(size);
	}

	public int getSpaceForTTS() {
		return mSpaceForTTS;
	}

	public int getUnreleasedTTSMem() {
		return mSpaceForTTS - mTTSCounter.availablePermits();
	}

	/**
	 * Transfer memory from the TTS area to the encoding area. It blocks until
	 * there is enough space in the encoding area.
	 */
	public void transferToEncoding(int ttsMemSize, int encodingMemSize)
	        throws InterruptedException {
		acquireEncodersMemory(encodingMemSize);
		ServerLogger.trace("About to transfer (flush) {} of {} in buffer", ttsMemSize, mTTSCounter.availablePermits());
		mTTSCounter.release(ttsMemSize);
	}

	public void acquireEncodersMemory(int size) throws InterruptedException {
		if (size > mPaddedEncodingSpace) {
			ServerLogger.debug("try to acquire " + size + " bytes, exceeding the max size = "
			        + mPaddedEncodingSpace + "MB");
			mEncodingCounter.acquire(mPaddedEncodingSpace);
		} else
			mEncodingCounter.acquire(size);
	}

	public void releaseEncodersMemory(int size) {
		if (size > mPaddedEncodingSpace)
			mEncodingCounter.release(mPaddedEncodingSpace);
		else
			mEncodingCounter.release(size);
	}

	public int getSpaceForEncoding() {
		return mSpaceForEncoding;
	}

	public int getUnreleasedEncondingMem() {
		return mSpaceForEncoding - mEncodingCounter.availablePermits();
	}

	private int mPaddedEncodingSpace;
	private int mSpaceForTTS;
	private int mSpaceForEncoding;
	private Semaphore mTTSCounter;
	private Semaphore mEncodingCounter;

	/**
	 * Get length of AudioInputStream in number of bytes
	 */
	public static int getFootprint(AudioInputStream audio) {
		return Math.multiplyExact(Math.toIntExact(audio.getFrameLength()),
                                  audio.getFormat().getFrameSize());
	}
}
