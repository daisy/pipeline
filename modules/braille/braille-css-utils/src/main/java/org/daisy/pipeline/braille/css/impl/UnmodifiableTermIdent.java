package org.daisy.pipeline.braille.css.impl;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;

class UnmodifiableTermIdent extends UnmodifiableTerm<String> implements TermIdent {

	private final Term<String> ident;

	UnmodifiableTermIdent(Term<String> ident) {
		this.ident = ident;
	}

	@Override
	public String getValue() {
		return ident.getValue();
	}
}
