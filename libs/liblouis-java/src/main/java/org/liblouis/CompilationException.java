package org.liblouis;

import java.util.List;

@SuppressWarnings("serial")
public class CompilationException extends Exception {
	
	public CompilationException(String message) {
		super(message);
	}
	
	public CompilationException(String message, List<String> errors) {
		this(addErrorsToMessage(message, errors));
	}
	
	public CompilationException(Throwable cause) {
		super(cause);
	}
	
	public CompilationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	private static String addErrorsToMessage(String message, List<String> errors) {
		if (errors != null && !errors.isEmpty()) {
			message += "\nErrors:";
			for (String e : errors)
				message += ("\n" + e);
		}
		return message;
	}
}
