package org.daisy.pipeline.braille.css.impl;

import cz.vutbr.web.css.TermURI;

class UnmodifiableTermURI extends UnmodifiableTerm<String> implements TermURI {

	private final TermURI uri;

	UnmodifiableTermURI(TermURI uri) {
		this.uri = uri;
	}

	@Override
	public String getValue() {
		return uri.getValue();
	}

	public java.net.URL getBase() {
		return uri.getBase();
	}

	public TermURI setBase(java.net.URL base) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
}