package org.daisy.dotify.impl.hyphenator.latex;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;

class LatexHyphenatorConfigurationException extends
		HyphenatorConfigurationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2975917855861633456L;

	LatexHyphenatorConfigurationException() {
		super();
	}

	LatexHyphenatorConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	LatexHyphenatorConfigurationException(String message) {
		super(message);
	}

	LatexHyphenatorConfigurationException(Throwable cause) {
		super(cause);
	}


}
