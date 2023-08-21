package org.daisy.pipeline.braille.css.impl;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermString;

class UnmodifiableTermString extends UnmodifiableTerm<String> implements TermString {

	private final Term<String> string;

	UnmodifiableTermString(Term<String> string) {
		this.string = string;
	}

	@Override
	public String getValue() {
		return string.getValue();
	}
}