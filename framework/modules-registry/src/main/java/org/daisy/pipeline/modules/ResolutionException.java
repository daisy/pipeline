package org.daisy.pipeline.modules;

/**
 * Exception thrown when a resource can not be resolved.
 */
public class ResolutionException extends RuntimeException {

	public ResolutionException() {
		super();
	}

	public ResolutionException(String message) {
		super(message);
	}

	public ResolutionException(Throwable cause) {
		super(cause);
	}

	public ResolutionException(String message, Throwable cause) {
		super(message, cause);
	}
}
