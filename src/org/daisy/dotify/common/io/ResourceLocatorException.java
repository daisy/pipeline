package org.daisy.dotify.common.io;

import java.io.IOException;

/**
 * A ResourceLocatorException is an exception that indicates 
 * conditions in a {@link ResourceLocator} that a reasonable 
 * application might want to catch.
 * 
 * @author Joel Håkansson
 */
public class ResourceLocatorException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6379009305571540260L;

	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public ResourceLocatorException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 * @param message the detail message
	 */
	public ResourceLocatorException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified cause
	 * @param cause the cause
	 */
	public ResourceLocatorException(Throwable cause) {
		super();
		//Java 1.5 does not support 'super(cause)'
		super.initCause(cause);
	}


	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public ResourceLocatorException(String message, Throwable cause) {
		super(message);
		//Java 1.5 does not support 'super(message, cause)'
		super.initCause(cause);
	}

}
