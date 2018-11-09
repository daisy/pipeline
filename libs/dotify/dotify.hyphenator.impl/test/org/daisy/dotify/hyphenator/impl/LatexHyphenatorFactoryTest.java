package org.daisy.dotify.hyphenator.impl;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class LatexHyphenatorFactoryTest {

	@Test
	public void testEnglishHyphenator() throws HyphenatorConfigurationException {
		HyphenatorInterface h = new LatexHyphenatorFactory(LatexHyphenatorCore.getInstance()).newHyphenator("en");
		
		//Test
		assertEquals("test­ing", h.hyphenate("testing"));
	}
	
}
