package org.daisy.dotify.formatter.impl;

public class RestartPaginationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2017654555446022589L;

	public RestartPaginationException() {
	}

	public RestartPaginationException(String message) {
		super(message);
	}

	public RestartPaginationException(Throwable cause) {
		super(cause);
	}

	public RestartPaginationException(String message, Throwable cause) {
		super(message, cause);
	}

	public RestartPaginationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
