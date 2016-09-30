package org.daisy.dotify.impl.translator;
import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.junit.Ignore;
import org.junit.Test;

public class DefaultBypassTranslatorTest {
	private final BrailleTranslator bypass;
	
	public DefaultBypassTranslatorTest() throws TranslatorConfigurationException {
		this.bypass = null;// new
							// DefaultBypassTranslatorFactory(HyphenatorFactoryMaker.newInstance()).newTranslator("sv-SE",
							// BrailleTranslatorFactory.MODE_BYPASS);
	}
	
	@Test
	@Ignore
	public void testBypass() throws TranslationException {
		//Setup
		String text = "This is a test to see if the bypass feature works as intended";
		BrailleTranslatorResult btr = bypass.translate(Translatable.text(text).build());
		//Test
		assertEquals("Assert that the output is equal to the input.", text, btr.nextTranslatedRow(100, false));
	}
	
	@Test
	@Ignore
	public void testBypassZeroWidthSpace_01() throws TranslationException {	
		BrailleTranslatorResult btr = bypass.translate(Translatable.text("CD-versionen").build());
		assertEquals("CD-versionen", btr.getTranslatedRemainder());
	}
	
	@Test
	@Ignore
	public void testBypassZeroWidthSpace_02() throws TranslationException {	
		BrailleTranslatorResult btr = bypass.translate(Translatable.text("CD-versionen").build());
		assertEquals("CD-", btr.nextTranslatedRow(4, false));
	}
	
	@Test
	@Ignore
	public void testBypassZeroWidthSpace_03() throws TranslationException {
		//Setup
		BrailleTranslatorResult btr = bypass.translate(Translatable.text("CD-versionen").build());
		btr.nextTranslatedRow(3, false);
		
		//Test
		assertEquals("versionen", btr.getTranslatedRemainder());
	}

}
