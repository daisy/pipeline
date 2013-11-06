package org.daisy.dotify.api.writer;


/**
 * A PagedMediaWriterException is an exception that indicates 
 * conditions in a {@link PagedMediaWriter} that a reasonable 
 * application might want to catch.
 * @author Joel Håkansson
 */
public class PagedMediaWriterException extends Exception {

	static final long serialVersionUID = 4712270390572769650L;

	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public PagedMediaWriterException() {}

	/**
	 * Constructs a new exception with the specified detail message.
	 * @param message the detail message
	 */
	public PagedMediaWriterException(String message) { super(message); }

	/**
	 * Constructs a new exception with the specified cause
	 * @param cause the cause
	 */
	public PagedMediaWriterException(Throwable cause) { super(cause); }

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public PagedMediaWriterException(String message, Throwable cause) { super(message, cause); }

}