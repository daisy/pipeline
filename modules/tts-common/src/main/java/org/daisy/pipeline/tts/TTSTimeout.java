package org.daisy.pipeline.tts;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TTSTimeout keeps watch over a given thread (which can be the current thread).
 * When the time is up, it first attempts to call interrupt() on the thread. If
 * this is not working, it will try by calling a custom function (e.g. something
 * that brutally kills a process).
 * 
 * As long as it is used sequentially, TTSTimeout can be called again multiple
 * times : no extra thread creation will be performed.
 * 
 */
public class TTSTimeout extends Thread {

	public interface ThreadFreeInterrupter {
		void threadFreeInterrupt();
	}

	private Thread mJob;
	private int mSeconds;
	private AtomicBoolean mClosed;
	private boolean mEnable;
	private ThreadFreeInterrupter mInterrupter;

	public TTSTimeout() {
		mClosed = new AtomicBoolean(false);
		mEnable = false;
		mInterrupter = null;
		start();
	}

	/**
	 * Must always be called even if enable() is never called, otherwise the
	 * underlying thread will run forever.
	 */
	public void close() {
		mClosed.set(true);
		this.interrupt(); //interrupt the wait at the beginning of the loop
	}

	public void enable(Thread job, ThreadFreeInterrupter interruptable, int seconds) {
		if (seconds < 0)
			throw new IllegalArgumentException("Expecting a positive number of seconds");
		mInterrupter = interruptable;
		mJob = job;
		mEnable = true;
		mSeconds = seconds;
		synchronized (mClosed) {
			mClosed.notify(); //unblock the wait at the beginning of the loop
		}
	}

	public void enable(Thread job, int seconds) {
		enable(job, null, seconds);
	}

	public void enableForCurrentThread(ThreadFreeInterrupter interruptable, int seconds) {
		enable(Thread.currentThread(), interruptable, seconds);
	}

	public void enableForCurrentThread(int seconds) {
		enableForCurrentThread(null, seconds);
	}

	/**
	 * Must always be called after enable().
	 */
	public void disable() {
		this.interrupt();

		//wait for the timeout to finish its current loop
		synchronized (mClosed) {
			while (mEnable) {
				try {
					mClosed.wait();
				} catch (InterruptedException e) {
					//not possible: two disable()'s or close() cannot be called simultaneously
				}
			}
		}
	}

	@Override
	public void run() {
		while (!mClosed.get()) {
			synchronized (mClosed) {
				while (!mEnable) {
					try {
						mClosed.wait();
					} catch (InterruptedException e) {
						//happen when close() is called
						return;
					}
				}
			}
			try {
				sleep(Math.multiplyExact(mSeconds, 1000));
			} catch (InterruptedException e) {
				//interrupted by the TTS thread with disable() because the watched job is finished
				notifyFinished();
				continue;
			}
			//time's up! first attempt: the job is gracefully interrupted
			mJob.interrupt();

			//wait for the job to exit and to cancel this timeout
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				//interrupted by the TTS thread with disable()
				notifyFinished();
				continue;
			}
			if (interrupted()) {
				//can happen if the thread is interrupted between calls
				notifyFinished();
				continue;
			}

			//it didn't work: we attempt the aggressive way
			if (mInterrupter != null)
				mInterrupter.threadFreeInterrupt();

			//wait forever until the TTS cancels this timeout
			try {
				sleep(Long.MAX_VALUE);
			} catch (InterruptedException e) {
				//interrupted by the TTS thread with disable()
				notifyFinished();
				continue;
			}
		}
	}

	private void notifyFinished() {
		mEnable = false;
		synchronized (mClosed) {
			mClosed.notify();
		}
	}

}
