package org.daisy.dotify.formatter.impl;

public class PaginatorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8015133306865945283L;

	PaginatorException() {
	}

	PaginatorException(String message) {
		super(message);
	}

	PaginatorException(Throwable cause) {
		super(cause);
	}

	PaginatorException(String message, Throwable cause) {
		super(message, cause);
	}

}
