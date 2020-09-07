package org.daisy.pipeline.tts.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.daisy.pipeline.tts.scheduler.FatalError;
import org.daisy.pipeline.tts.scheduler.RecoverableError;
import org.daisy.pipeline.tts.scheduler.Schedulable;
import org.daisy.pipeline.tts.scheduler.Scheduler;

/**
 * Scheduler that increases the delay between actions launch, using a number of seconds equals to
 * apower of 2 times retries and a random number of milliseconds.
 *
 * <p>This scheduler implementation is based on <a
 * href="https://cloud.google.com/storage/docs/exponential-backoff">Google</a> and <a
 * href="https://docs.aws.amazon.com/general/latest/gr/api-retries.html">Amazon</a> recommendations.</p>
 *
 * @author Louis Caille @ braillenet.org
 *
 * @param <Action> class of request objects to handle
 */
public class ExponentialBackoffScheduler<Action extends Schedulable> implements Scheduler<Action> {

	/**
	 *  maximum waiting time in millisecond
	 */
	private static final int MAXIMUM_BACKOFF = 64000;

	/**
	 * Waiting time computed following <a
	 * href="https://cloud.google.com/storage/docs/exponential-backoff">Google</a> and <a
	 * href="https://docs.aws.amazon.com/general/latest/gr/api-retries.html">Amazon</a>
	 * recommendations.
	 *
	 * @return the waiting time in milliseconds
	 */
	private long waitingTime(int attemptCounter){
		long random_number_milliseconds = (long)(Math.random() * 1000);
		long waitingTime = (long)Math.min(Math.pow(2, attemptCounter) * 1000L + random_number_milliseconds, MAXIMUM_BACKOFF);
		return waitingTime;
	}

	/**
	 * When a recoverable error is thrown, the scheduler keep the action in its queue and delays all
	 * launches by sleeping the thread while in locked state
	 */
	public void launch(Action action) throws InterruptedException, FatalError {
		int attempt = 0;
		boolean retry = true;
		while (retry) {
			try {
				action.launch();
				retry = false;
			} catch (RecoverableError e){
				Thread.sleep(waitingTime(attempt));
				attempt++;
			}
		}
	}
}
