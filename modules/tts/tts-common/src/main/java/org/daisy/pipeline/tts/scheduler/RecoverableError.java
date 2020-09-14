package org.daisy.pipeline.tts.scheduler;

/**
 * Exception to be raised by Schedulable object when an error occurred but that object can be
 * rescheduled.
 *
 * @author Nicolas Pavie @ braillenet.org
 */
public class RecoverableError extends Exception {

	private static final long serialVersionUID = 1L;

	public RecoverableError(String message) {
		super(message);
	}

	public RecoverableError(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RecoverableError(Throwable cause) {
		super(cause);
	}
}
