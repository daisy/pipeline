package org.daisy.pipeline.braille.common;

@SuppressWarnings("serial")
public class TransformationException extends RuntimeException {
	
	public TransformationException(String message) {
		super(message);
	}
	
	public TransformationException(Throwable throwable) {
		super(throwable);
	}
	
	public TransformationException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
