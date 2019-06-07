package org.daisy.pipeline.braille.liblouis;

import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;

public interface LiblouisTranslator extends BrailleTranslator {
	
	public LiblouisTable asLiblouisTable();
	
	public FromTypeformedTextToBraille fromTypeformedTextToBraille();
	
	public interface FromTypeformedTextToBraille {
		
		/**
		 * @param text The text segments to be translated.
		 * @param typeform The typeform. Array must have the same length as <code>text</code>.
		 * @return The translated text segments. Has the same length as <code>text</code>.
		 */
		public String[] transform(String[] text, String[] typeform);
		
	}
	
	public interface Provider extends BrailleTranslatorProvider<LiblouisTranslator> {}
	
}
