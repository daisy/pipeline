package org.daisy.pipeline.nlp.calabash.impl;

import java.util.List;
import java.util.Locale;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.nlp.lexing.LexService.LexerInitException;

public interface InlineSectionProcessor {

	public class Leaf {
		public XdmNode formatting;
		public int level;
	}

	/**
	 * Called when a new inline section has been detected.
	 * 
	 * Guaranteed to be called in document order.
	 * 
	 * The provided lists are not guaranteed to be different for each call (they
	 * might be recycled).
	 * 
	 * A text element can be null. A formatting element can be null as well
	 * (when punctuation marks have been inserted into the text to help the
	 * lexer)
	 * 
	 * @throws LexerInitException
	 */

	public void onInlineSectionFound(List<Leaf> leaves, List<String> text, Locale lang)
	        throws LexerInitException;

	public void onEmptySectionFound(List<Leaf> leaves);
}
