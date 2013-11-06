package org.daisy.dotify.api.formatter;

/**
 * A LayoutException is an exception that indicates 
 * conditions in the layout process that a reasonable 
 * application might want to catch.
 * @author Joel Håkansson
 */
public class FormatterException extends Exception {

	static final long serialVersionUID = -2908554164728732775L;

	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public FormatterException() { super(); }

	/**
	 * Constructs a new exception with the specified detail message.
	 * @param message the detail message
	 */
	public FormatterException(String message) { super(message); }

	/**
	 * Constructs a new exception with the specified cause
	 * @param cause the cause
	 */
	public FormatterException(Throwable cause) { super(cause); }

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public FormatterException(String message, Throwable cause) { super(message, cause); }

}
