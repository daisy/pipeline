package org.daisy.pipeline.braille.liblouis;

import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.HyphenatorProvider;

public interface LiblouisHyphenator extends Hyphenator {
	
	public LiblouisTable asLiblouisTable();
	
	public interface Provider extends HyphenatorProvider<LiblouisHyphenator> {}
	
}
