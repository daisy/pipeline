package org.liblouis;

import java.util.List;

@SuppressWarnings("serial")
public class TranslationException extends Exception {
	
	public TranslationException(String message) {
		super(message);
	}

	public TranslationException(String message, List<String> errors) {
		this(addErrorsToMessage(message, errors));
	}
	
	public TranslationException(Throwable throwable) {
		super(throwable);
	}
	
	public TranslationException(String message, Throwable throwable) {
		super(message, throwable);
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
