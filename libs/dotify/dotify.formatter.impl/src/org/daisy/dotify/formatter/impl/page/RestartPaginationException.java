package org.daisy.dotify.formatter.impl.page;

/**
 * Indicates that the pagination failed and should be restarted
 * from the beginning. If this exception is thrown, then all 
 * previously successful sequences may be compromised.
 * 
 * @author Joel Håkansson
 */
public class RestartPaginationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2017654555446022589L;

	RestartPaginationException() {
	}

	RestartPaginationException(String message) {
		super(message);
	}

	RestartPaginationException(Throwable cause) {
		super(cause);
	}

	RestartPaginationException(String message, Throwable cause) {
		super(message, cause);
	}

	RestartPaginationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
