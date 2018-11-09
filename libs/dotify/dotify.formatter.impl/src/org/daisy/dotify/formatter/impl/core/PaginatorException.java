package org.daisy.dotify.formatter.impl.core;

/**
 * Indicates a pagination problem.
 * 
 * @author Joel Håkansson
 */
public class PaginatorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8015133306865945283L;

	PaginatorException() {
	}

	public PaginatorException(String message) {
		super(message);
	}

	PaginatorException(Throwable cause) {
		super(cause);
	}

	public PaginatorException(String message, Throwable cause) {
		super(message, cause);
	}

}
