package org.daisy.pipeline.braille.dotify;

import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;

public interface DotifyTranslator extends BrailleTranslator {
	
	public BrailleFilter asBrailleFilter();
	
	public interface Provider extends BrailleTranslatorProvider<DotifyTranslator> {}
	
}
