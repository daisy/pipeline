package org.daisy.pipeline.braille.dotify;

import org.daisy.dotify.api.hyphenator.HyphenatorInterface;

import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.HyphenatorProvider;

public interface DotifyHyphenator extends Hyphenator {
	
	public HyphenatorInterface asHyphenatorInterface();
	
	public interface Provider extends HyphenatorProvider<DotifyHyphenator> {}
	
}
