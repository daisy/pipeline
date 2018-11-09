package org.daisy.dotify.tasks.impl.input.xml;

/**
 * Provides a task creation exception.
 * @author Joel Håkansson
 */
public class TaskCreationException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7077703562914529672L;

	@SuppressWarnings("javadoc")
	public TaskCreationException() {
		super();
	}

	@SuppressWarnings("javadoc")
	public TaskCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@SuppressWarnings("javadoc")
	public TaskCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	@SuppressWarnings("javadoc")
	public TaskCreationException(String message) {
		super(message);
	}

	@SuppressWarnings("javadoc")
	public TaskCreationException(Throwable cause) {
		super(cause);
	}

}
