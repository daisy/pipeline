package org.daisy.pipeline.braille.common;

import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.pipeline.braille.css.CSSStyledText;

public interface BrailleTranslator extends Transform {

	/**
	 * Return a new {@link BrailleTranslator} that uses the given {@link Hyphenator} to perform
	 * hyphenation.
	 *
	 * The returned object should be selectable based on its identifier from the same {@link
	 * BrailleTranslatorProvider} that provided this {@link BrailleTranslator}.
	 */
	public BrailleTranslator withHyphenator(Hyphenator hyphenator) throws UnsupportedOperationException;
	
	public FromStyledTextToBraille fromStyledTextToBraille() throws UnsupportedOperationException;
	
	public LineBreakingFromStyledText lineBreakingFromStyledText() throws UnsupportedOperationException;
	
	/* ------------------------- */
	/* fromStyledTextToBraille() */
	/* ------------------------- */
	
	public interface FromStyledTextToBraille {
		
		default public Iterable<String> transform(Iterable<CSSStyledText> styledText) throws TransformationException {
			return transform(styledText, 0, -1);
		}
		
		public Iterable<String> transform(Iterable<CSSStyledText> styledText, int from, int to) throws TransformationException;
	}
	
	/* ---------------------------- */
	/* lineBreakingFromStyledText() */
	/* ---------------------------- */
	
	public interface LineBreakingFromStyledText {
		
		/**
		 * Transform a sequence of {@link CSSStyledText} to braille, in the form of a {@link
		 * LineIterator}, which is an interface for laying out text into lines.
		 */
		default public LineIterator transform(Iterable<CSSStyledText> styledText) throws TransformationException {
			return transform(styledText, 0, -1);
		}
		
		/**
		 * Transform the part of the input sequence between index <code>from</code> (included) and
		 * index <code>to</code> (not included). If <code>to</code> is a negative number it means
		 * transform up until the last segment.
		 */
		public LineIterator transform(Iterable<CSSStyledText> styledTextWithContext, int from, int to) throws TransformationException;
	}
	
	/* ------------ */
	/* LineIterator */
	/* ------------ */
	
	public interface LineIterator extends BrailleTranslatorResult {
	}
	
}
