package org.daisy.pipeline.tts.scheduler;

/**
 * Schedulable action interface, used by Scheduler implementation.
 *
 * <p>A schedulable action must be rescheduled if a RecoverableError is raised.</p>
 */
public interface Schedulable {
	/**
	 * Execute the action
	 *
	 * @throws InterruptedException if the action thread is interrupted
	 * @throws RecoverableError if the error is recoverable, so that the action can be rescheduled
	 * @throws FatalError if the action cannot be rescheduled
	 */
	public void launch() throws InterruptedException, RecoverableError, FatalError;
}
