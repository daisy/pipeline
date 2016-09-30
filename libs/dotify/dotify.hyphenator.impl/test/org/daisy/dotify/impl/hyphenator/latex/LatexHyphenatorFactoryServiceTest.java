package org.daisy.dotify.impl.hyphenator.latex;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LatexHyphenatorFactoryServiceTest {

	@Test
	public void testSupportedLocales(){
		boolean supports = true;
		//Test
		supports &= new LatexHyphenatorFactoryService().supportsLocale("en");
		supports &= new LatexHyphenatorFactoryService().supportsLocale("en-US");
		supports &= new LatexHyphenatorFactoryService().supportsLocale("en-GB");
		
		supports &= new LatexHyphenatorFactoryService().supportsLocale("sv");
		supports &= new LatexHyphenatorFactoryService().supportsLocale("sv-SE");
		
		supports &= new LatexHyphenatorFactoryService().supportsLocale("no");
		supports &= new LatexHyphenatorFactoryService().supportsLocale("no-NO");
		
		supports &= new LatexHyphenatorFactoryService().supportsLocale("de");
		supports &= new LatexHyphenatorFactoryService().supportsLocale("de-DE");
		
		supports &= new LatexHyphenatorFactoryService().supportsLocale("fr");
		supports &= new LatexHyphenatorFactoryService().supportsLocale("fr-FR");
		
		supports &= new LatexHyphenatorFactoryService().supportsLocale("fi");
		supports &= new LatexHyphenatorFactoryService().supportsLocale("fi-FI");
		
		assertTrue(supports);
	}

	@Test
	public void testUnsupportedLocale() {
		//Test
		assertTrue(!new LatexHyphenatorFactoryService().supportsLocale("sv-SE-dummy"));
	}
	
}
