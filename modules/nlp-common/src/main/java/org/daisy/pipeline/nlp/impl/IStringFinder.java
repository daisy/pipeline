package org.daisy.pipeline.nlp.impl;

import java.util.Collection;

/**
 * Find a string in a provided collection. The implementations define the
 * matching strategy (full, prefix, suffix...)
 */
public interface IStringFinder {

	void compile(Collection<String> matchable);

	/**
	 * @param input the string to find
	 * @return null if not found, the matched substring of @param input
	 *         otherwise
	 */
	String find(String input);

	boolean threadsafe();
}
