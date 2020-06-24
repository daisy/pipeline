package org.liblouis;

@SuppressWarnings("serial")
public class TranslationException extends Exception {
	
	public TranslationException(String message) {
		super(message);
	}
	
	public TranslationException(Throwable throwable) {
		super(throwable);
	}
	
	public TranslationException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
