package org.daisy.pipeline.word_to_dtbook.impl;

public class ValidationResult {

	public final String ErrorMessage;

	public ValidationResult() {
		this(null);
	}

	public ValidationResult(String errorMessage) {
		ErrorMessage = errorMessage;
	}

	public boolean IsValid() {
		return ErrorMessage == null || "".equals(ErrorMessage);
	}
}
