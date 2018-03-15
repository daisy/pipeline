package org.daisy.pipeline.braille.common;

import org.daisy.dotify.api.translator.BrailleTranslatorResult;

public interface BrailleTranslator extends Transform {
	
	public FromStyledTextToBraille fromStyledTextToBraille() throws UnsupportedOperationException;
	
	public LineBreakingFromStyledText lineBreakingFromStyledText() throws UnsupportedOperationException;
	
	/* ------------------------- */
	/* fromStyledTextToBraille() */
	/* ------------------------- */
	
	public interface FromStyledTextToBraille {
		
		public Iterable<String> transform(Iterable<CSSStyledText> styledText) throws TransformationException;
		
	}
	
	/* ---------------------------- */
	/* lineBreakingFromStyledText() */
	/* ---------------------------- */
	
	public interface LineBreakingFromStyledText {
		
		public LineIterator transform(Iterable<CSSStyledText> styledText) throws TransformationException;
		
	}
	
	/* ------------ */
	/* LineIterator */
	/* ------------ */
	
	public interface LineIterator extends BrailleTranslatorResult {
	}
	
}
