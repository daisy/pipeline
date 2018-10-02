package org.daisy.pipeline.braille.liblouis;

import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;

public interface LiblouisTranslator extends BrailleTranslator {
	
	public LiblouisTable asLiblouisTable();
	
	public FromTypeformedTextToBraille fromTypeformedTextToBraille();
	
	public static abstract class Typeform {
		public static final short PLAIN = 0;
		public static final short ITALIC = 1;
		public static final short BOLD = 4;
		public static final short UNDERLINE = 2;
		public static final short COMPUTER = 1024;
	}
	
	public interface FromTypeformedTextToBraille {
		
		/**
		 * @param text The text segments to be translated.
		 * @param typeform The typeform. Array must have the same length as <code>text</code>.
		 * @return The translated text segments. Has the same length as <code>text</code>.
		 */
		public String[] transform(String[] text, short[] typeform);
		
	}
	
	public interface Provider extends BrailleTranslatorProvider<LiblouisTranslator> {}
	
}
