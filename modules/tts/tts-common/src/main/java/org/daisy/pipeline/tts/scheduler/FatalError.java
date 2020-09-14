package org.daisy.pipeline.tts.scheduler;

/**
 * Exception to be raised by Schedulable object when an error occurred and the object cannot be
 * rescheduled.
 *
 * @author Nicolas Pavie @ braillenet.org
 */
public class FatalError extends Exception {

	private static final long serialVersionUID = 1L;

	public FatalError(String message) {
		super(message);
	}

	public FatalError(String message, Throwable cause) {
		super(message, cause);
	}

	public FatalError(Throwable cause) {
		super(cause);
	}
}
