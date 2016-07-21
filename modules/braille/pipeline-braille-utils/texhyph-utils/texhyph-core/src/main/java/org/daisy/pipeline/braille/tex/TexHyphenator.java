package org.daisy.pipeline.braille.tex;

import java.net.URI;

import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.HyphenatorProvider;

public interface TexHyphenator extends Hyphenator {
	
	public URI asTexHyphenatorTable();
	
	public interface Provider extends HyphenatorProvider<TexHyphenator> {}
	
}
