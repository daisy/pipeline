package org.daisy.pipeline.client;

/**
 * A generic error thrown by the Pipeline 2 Client Library.
 */
public class Pipeline2Exception extends Exception {
	private static final long serialVersionUID = 1L;
	
	public Pipeline2Exception(String message, Throwable cause) {
		super(message, cause);
	}

	public Pipeline2Exception(String message) {
		super(message);
	}

	public Pipeline2Exception(Throwable cause) {
		super(cause);
	}
	
}
