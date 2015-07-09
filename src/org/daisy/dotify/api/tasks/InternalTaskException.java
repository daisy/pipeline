package org.daisy.dotify.api.tasks;


/**
 * An InternalTaskException is an exception that indicates 
 * conditions in an {@link InternalTask} that a reasonable 
 * application might want to catch.
 * @author Joel Håkansson
 */
public class InternalTaskException extends TaskSystemException {

	static final long serialVersionUID = -3190485874533528098L;

	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public InternalTaskException() { }

	/**
	 * Constructs a new exception with the specified detail message.
	 * @param message the detail message
	 */
	public InternalTaskException(String message) { super(message); }

	/**
	 * Constructs a new exception with the specified cause
	 * @param cause the cause
	 */
	public InternalTaskException(Throwable cause) { super(cause); }

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public InternalTaskException(String message, Throwable cause) { super(message, cause); }

}
