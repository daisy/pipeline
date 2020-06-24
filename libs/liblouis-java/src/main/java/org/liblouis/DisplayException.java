package org.liblouis;

@SuppressWarnings("serial")
public class DisplayException extends Exception {

	public DisplayException(String message) {
		super(message);
	}

	public DisplayException(Throwable throwable) {
		super(throwable);
	}

	public DisplayException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
