package org.daisy.pipeline.braille.css.impl;

import cz.vutbr.web.css.Term;

abstract class UnmodifiableTerm<T> implements Term<T> {

	@Override
	public T getValue() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public Operator getOperator() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UnmodifiableTerm<T> shallowClone() {
		try {
			return (UnmodifiableTerm<T>)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("coding error");
		}
	}

	@Override
	public UnmodifiableTerm<T> clone() {
		return shallowClone();
	}

	@Override
	public UnmodifiableTerm<T> setValue(T value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public UnmodifiableTerm<T> setOperator(Operator operator) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
}
