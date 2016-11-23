package org.daisy.dotify.api.translator;

/**
 * Provides a runtime exception for unsupported metrics.
 * 
 * @author Joel Håkansson
 */
public class UnsupportedMetricException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4173548731907414149L;

	/**
     * Constructs an <code>UnsupportedMetricException</code> with no
     * detail message.
     */
	public UnsupportedMetricException() {
		super();
	}

	/**
     * Constructs an <code>UnsupportedMetricException</code> with the
     * specified detail message.
     *
     * @param   message   the detail message.
     */
	public UnsupportedMetricException(String message) {
		super(message);
	}

	/**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * 
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link Throwable#getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
	public UnsupportedMetricException(Throwable cause) {
		super(cause);
	}

	 /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * <p>Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link Throwable#getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link Throwable#getCause()} method).  (A <tt>null</tt> value
     *         is permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
	public UnsupportedMetricException(String message, Throwable cause) {
		super(message, cause);
	}

}
