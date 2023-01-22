package org.daisy.common.spi;

/**
 * If this type of exception is thrown during instantiation of a service, it will not generate an
 * ERROR message (only INFO and TRACE messages).
 */
public class ActivationException extends RuntimeException {

	private static final long serialVersionUID = -5017633450958770953L;

	/**
	 * @param message Reason why the service provider could not be instantiated. Doesn't need to
	 *                include the name of the service or provider.
	 * @param cause Can be specified for more details. Stack trace will be logged in a TRACE
	 *              message.
	 */
	public ActivationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActivationException(String message) {
		this(message, null);
	}
}
