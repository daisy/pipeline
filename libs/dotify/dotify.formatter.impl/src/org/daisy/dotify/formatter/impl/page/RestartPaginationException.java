package org.daisy.dotify.formatter.impl.page;

/**
 * <p>Indicates that the pagination should be restarted from the beginning (the start of the current
 * iteration). This may for example occur when a change in the page layout is triggered, such as
 * changing from footnotes (at the bottom of the page) to end notes (at the end of volumes). If this
 * exception is thrown, then all previously successful sequences could be compromised.</p>
 *
 * <p><strong>Use with care.</strong> This exception should never be thrown
 * in situations where the state that triggered it might return at some
 * point in the future. Misuse may drastically reduce the speed and probability
 * of convergence.</p>
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
