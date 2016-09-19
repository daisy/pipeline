package org.daisy.pipeline.tts;

import junit.framework.Assert;

import org.daisy.pipeline.tts.TTSTimeout.ThreadFreeInterrupter;
import org.junit.BeforeClass;
import org.junit.Test;

public class TimeoutTest {

	private static class Job extends Thread implements ThreadFreeInterrupter {

		public boolean hardInterrupted = false;
		public boolean softInterrupted = false;
		private long mFirstWait;
		private long mSecondWait;

		public Job(long firstWait, long secondWait) {
			mFirstWait = firstWait;
			mSecondWait = secondWait;
		}

		@Override
		public void threadFreeInterrupt() {
			hardInterrupted = true;
			this.interrupt();
		}

		@Override
		public void run() {
			try {
				Thread.sleep(mFirstWait);
			} catch (InterruptedException e) {
				try {
					Thread.sleep(mSecondWait);
				} catch (InterruptedException e2) {
					return;
				}
				softInterrupted = true;
			}
			return;
		}
	}

	private static boolean run(Thread t) {
		t.start();
		boolean interrupted = false;
		try {
			t.join();
		} catch (InterruptedException e) {
			interrupted = true;
		}
		return interrupted;
	}

	@BeforeClass
	public static void printWarning() {
		System.out.println("Warning: the following tests last about 40 seconds.");
	}

	@Test
	public void noInterruptNeeded() {
		TTSTimeout timeout = new TTSTimeout();
		for (int k = 0; k < 10; ++k) {
			Job job = new Job(100, 0);
			timeout.enable(job, job, 3);
			boolean interrupted = run(job);
			timeout.disable();
			Assert.assertFalse(interrupted);
			Assert.assertFalse(job.hardInterrupted);
			Assert.assertFalse(job.softInterrupted);
		}
		timeout.close();
	}

	@Test
	public void softInterruption() {
		TTSTimeout timeout = new TTSTimeout();
		for (int k = 0; k < 8; ++k) {
			Job job = new Job(Long.MAX_VALUE, 0);
			timeout.enable(job, job, 1);
			boolean interrupted = run(job);
			timeout.disable();
			Assert.assertFalse(job.hardInterrupted);
			Assert.assertTrue(interrupted || job.softInterrupted);
		}
		timeout.close();
	}

	@Test
	public void hardInterruption() {
		TTSTimeout timeout = new TTSTimeout();
		for (int k = 0; k < 8; ++k) {
			Job job = new Job(Long.MAX_VALUE, Long.MAX_VALUE);
			timeout.enable(job, job, 1);
			run(job);
			timeout.disable();
			Assert.assertTrue(job.hardInterrupted);
		}
		timeout.close();
	}
}
