package org.daisy.pipeline.nlp.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Try to match given fixed-length strings with a list of strings provided at
 * initialization.
 */
public class FullMatchStringFinder implements IStringFinder {

	private Set<String> mSet = new HashSet<String>();

	@Override
	public void compile(Collection<String> matchable) {
		mSet.addAll(matchable);
	}

	@Override
	public String find(String input) {
		if (mSet.contains(input))
			return input;
		return null;
	}

	@Override
	public boolean threadsafe() {
		return true;
	}

}
