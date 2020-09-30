package org.daisy.pipeline.tts.scheduler;

/**
 * Scheduler interface to launch schedulable actions.
 *
 * <p>A schedulable action must be reschedule when a RecoverableError is raised.</p>
 *
 * <p>An example of implementation is provided in the ExponentialBackoffScheduler</p>
 *
 * @author Louis Caille @ braillenet
 *
 * @param <Action> the schedulable action class to handle
 */
public interface Scheduler<Action extends Schedulable> {
	
	/**
	 * Launch an action from the queue.
	 *
	 * @param scheduled the action to launch
	 * @throws InterruptedException if the scheduler thread is interrupted
	 * @throws FatalError if the action could not be executed or rescheduled after a RecoverableError
	 */
	public void launch(Action scheduled) throws InterruptedException, FatalError;

}
