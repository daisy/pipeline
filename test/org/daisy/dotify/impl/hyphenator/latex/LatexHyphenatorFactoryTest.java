package org.daisy.dotify.impl.hyphenator.latex;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.junit.Test;

public class LatexHyphenatorFactoryTest {

	@Test
	public void testEnglishHyphenator() throws HyphenatorConfigurationException {
		HyphenatorInterface h = new LatexHyphenatorFactory(LatexHyphenatorCore.getInstance()).newHyphenator("en");
		
		//Test
		assertEquals("test­ing", h.hyphenate("testing"));
	}
	
}
