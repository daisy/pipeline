package org.liblouis;

@SuppressWarnings("serial")
public class CompilationException extends Exception {
	
	public CompilationException(String message) {
		super(message);
	}
	
	public CompilationException(Throwable cause) {
		super(cause);
	}
	
	public CompilationException(String message, Throwable cause) {
		super(message, cause);
	}
}
