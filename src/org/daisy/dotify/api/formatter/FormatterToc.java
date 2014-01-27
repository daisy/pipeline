package org.daisy.dotify.api.formatter;

/**
 * Provides TOC entry formatter tasks.
 * 
 * @author Joel Håkansson
 */
public interface FormatterToc extends FormatterCore, TocEntry {

	/**
	 * Inserts an expression to evaluate.
	 * @param exp the expression
	 * @param t the text properties
	 */
	public void insertEvaluate(String exp, TextProperties t);
}
