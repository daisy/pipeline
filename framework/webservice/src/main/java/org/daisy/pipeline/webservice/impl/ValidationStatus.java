package org.daisy.pipeline.webservice.impl;

public final class ValidationStatus {

	private final boolean valid;
	private final String message;

	/**
	 * @param valid
	 * @param message
	 */
	public ValidationStatus(boolean valid, String message) {
		if (message == null)
			throw new NullPointerException();
		this.valid = valid;
		this.message = message;
	}

	/**
	 * @param valid
	 * @param message
	 */
	public ValidationStatus(boolean valid) {
		this(valid, "");
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

}
