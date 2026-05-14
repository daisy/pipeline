package org.daisy.pipeline.nlp.lexing.ruled.impl;

import java.util.List;

import org.daisy.pipeline.nlp.TextCategorizer.CategorizedWord;

public interface ISentenceDetector {

	/**
	 * The result cannot be kept after another call as it might be recycled.
	 * 
	 * @return a list of sentences.
	 */
	List<List<CategorizedWord>> split(List<CategorizedWord> words);

	/**
	 * Can return false before returning true, but not the other way around.
	 */
	boolean threadsafe();
}
