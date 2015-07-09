package org.daisy.dotify.api.tasks;


/**
 * A TaskSystemException is an exception that indicates 
 * conditions in a {@link TaskSystem} that a reasonable 
 * application might want to catch.
 * @author Joel Håkansson
 */
public class TaskSystemException extends Exception {

	static final long serialVersionUID = 45773873175031980L;

	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public TaskSystemException() { super(); }

	/**
	 * Constructs a new exception with the specified detail message.
	 * @param message the detail message
	 */
	public TaskSystemException(String message) { super(message); }

	/**
	 * Constructs a new exception with the specified cause
	 * @param cause the cause
	 */
	public TaskSystemException(Throwable cause) { super(cause); }

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public TaskSystemException(String message, Throwable cause) { super(message, cause); }

}
