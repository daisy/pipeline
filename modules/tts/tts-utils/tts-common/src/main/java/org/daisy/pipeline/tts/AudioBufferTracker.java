package org.daisy.pipeline.tts;

import java.util.concurrent.Semaphore;

import org.daisy.pipeline.audio.AudioBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AudioBufferTracker manages the footprint of the audio buffers requested by
 * the TTS processors. These buffers go through two virtual memory spaces: a
 * space for the TTS and a space for the encoding. The former is bigger and
 * throws exceptions if there is no space left, rather than possibly deadlocking
 * on blocking requests. The latter can afford to block on the requests, since
 * if the space is full, it will be consumed by the encoding threads.
 * 
 */
public class AudioBufferTracker implements AudioBufferAllocator {
	private Logger ServerLogger = LoggerFactory.getLogger(AudioBufferTracker.class);

	public AudioBufferTracker() {
		String maxMemProp = System.getProperty("tts.maxmem");
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

	@Override
	public AudioBuffer allocateBuffer(int size) throws MemoryException {
		if (!mTTSCounter.tryAcquire(size))
			throw new MemoryException(size);

		AllocatableBuffer res = new AllocatableBuffer();
		res.data = new byte[size];
		res.size = size;

		return res;
	}

	/**
	 * Must not be used by the encoders. They should use releaseEncodersMemory()
	 * instead.
	 */
	@Override
	public void releaseBuffer(AudioBuffer b) {
		releaseTTSMemory(getFootPrint(b));
	}

	/**
	 * Transfer memory from the TTS area to the encoding area. It blocks until
	 * there is enough space in the encoding area.
	 */
	public void transferToEncoding(int ttsMemSize, int encodingMemSize)
	        throws InterruptedException {
		acquireEncodersMemory(encodingMemSize);
		mTTSCounter.release(ttsMemSize);
	}

	public int getSpaceForTTS() {
		return mSpaceForTTS;
	}

	public int getSpaceForEncoding() {
		return mSpaceForEncoding;
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

	public void releaseTTSMemory(int size) {
		mTTSCounter.release(size);
	}

	public int getFootPrint(AudioBuffer buff) {
		return buff.data.length;
	}

	public int getUnreleasedEncondingMem() {
		return (mSpaceForEncoding - mEncodingCounter.availablePermits());
	}

	public int getUnreleasedTTSMem() {
		return (mSpaceForTTS - mTTSCounter.availablePermits());
	}

	private static class AllocatableBuffer extends AudioBuffer {
		public AllocatableBuffer() {
		}
	}

	private int mPaddedEncodingSpace;
	private int mSpaceForTTS;
	private int mSpaceForEncoding;
	private Semaphore mTTSCounter;
	private Semaphore mEncodingCounter;

}
