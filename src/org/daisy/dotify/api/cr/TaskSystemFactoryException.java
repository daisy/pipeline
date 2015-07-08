package org.daisy.dotify.api.cr;



/**
 * A TaskSystemFactoryException is an exception that indicates 
 * conditions in a {@link TaskSystemFactory} that a reasonable 
 * application might want to catch.
 * @author Joel Håkansson
 *
 */
public class TaskSystemFactoryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7053028902035757516L;

	public TaskSystemFactoryException() { }

	public TaskSystemFactoryException(String message) { super(message); }

	public TaskSystemFactoryException(Throwable cause) { super(cause); }

	public TaskSystemFactoryException(String message, Throwable cause) { super(message, cause); }

}
