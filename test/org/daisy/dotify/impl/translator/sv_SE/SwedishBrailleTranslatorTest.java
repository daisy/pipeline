package org.daisy.dotify.impl.translator.sv_SE;
import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.junit.Ignore;
import org.junit.Test;

public class SwedishBrailleTranslatorTest {
	private final static String TEST_INPUT_STRING_1 = "Skillnaden mellan arbets- och vilodagar blev mindre skarp; hon kunde tillåta sig vilodagar mitt i veckan.";
	private final BrailleTranslator translator;
	
	public SwedishBrailleTranslatorTest() throws TranslatorConfigurationException {
		this.translator = null;// new
								// SwedishBrailleTranslatorFactory(HyphenatorFactoryMaker.newInstance()).newTranslator("sv-SE",
								// BrailleTranslatorFactory.MODE_UNCONTRACTED);
		// this.translator.setHyphenating(true);
	}

	@Test
	@Ignore
	public void testTranslator() {
		//Setup
		BrailleTranslatorResult btr = translator.translate(TEST_INPUT_STRING_1);
		//Test
		assertEquals("Assert that the output is translated.", 
				"⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2800⠁⠗⠃⠑⠞⠎\u2824\u2800⠕⠉⠓\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠃⠇⠑⠧\u2800⠍⠊⠝⠙⠗⠑\u2800⠎⠅⠁⠗⠏⠆\u2800⠓⠕⠝\u2800⠅⠥⠝⠙⠑\u2800⠞⠊⠇⠇⠡⠞⠁\u2800⠎⠊⠛\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠍⠊⠞⠞\u2800⠊\u2800⠧⠑⠉⠅⠁⠝⠄",
				btr.getTranslatedRemainder());
	}
	
	@Test
	@Ignore
	public void testTranslator_01firstRow() {
		//Setup
		BrailleTranslatorResult btr = translator.translate(TEST_INPUT_STRING_1);

		//Test
		assertEquals("Assert that limit is handled correctly.", 
				"⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2800⠁⠗⠃⠑⠞⠎\u2824",
				btr.nextTranslatedRow(26, false));
	}

	@Test
	@Ignore
	public void testTranslatorWithAnotherLanguage() throws TranslationException {
		//Setup
		BrailleTranslatorResult btr = translator.translate(TEST_INPUT_STRING_1, "en");
		//Test
		assertEquals("Assert that the output is translated.", 
				"⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2800⠁⠗⠃⠑⠞⠎\u2824\u2800⠕⠉⠓\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠃⠇⠑⠧\u2800⠍⠊⠝⠙⠗⠑\u2800⠎⠅⠁⠗⠏⠆\u2800⠓⠕⠝\u2800⠅⠥⠝⠙⠑\u2800⠞⠊⠇⠇⠡⠞⠁\u2800⠎⠊⠛\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠍⠊⠞⠞\u2800⠊\u2800⠧⠑⠉⠅⠁⠝⠄",
				btr.getTranslatedRemainder());
	}

	@Test
	@Ignore
	public void testTranslatorAttributes_StyleBoldSingleWord() {
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(new DefaultTextAttribute.Builder("strong").build(10));
		atts.add(new DefaultTextAttribute.Builder().build(95));
		BrailleTranslatorResult btr = translator.translate(TEST_INPUT_STRING_1, atts.build(105));
		assertEquals("Assert that the style is translated.", 
				"\u2828⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2800⠁⠗⠃⠑⠞⠎\u2824\u2800⠕⠉⠓\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠃⠇⠑⠧\u2800⠍⠊⠝⠙⠗⠑\u2800⠎⠅⠁⠗⠏⠆\u2800⠓⠕⠝\u2800⠅⠥⠝⠙⠑\u2800⠞⠊⠇⠇⠡⠞⠁\u2800⠎⠊⠛\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠍⠊⠞⠞\u2800⠊\u2800⠧⠑⠉⠅⠁⠝⠄",
				btr.getTranslatedRemainder());
	}

	@Test
	@Ignore
	public void testTranslatorAttributes_StyleBoldMultipleWords() {
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(new DefaultTextAttribute.Builder("strong").build(17));
		atts.add(new DefaultTextAttribute.Builder().build(88));
		BrailleTranslatorResult btr = translator.translate(TEST_INPUT_STRING_1, atts.build(TEST_INPUT_STRING_1.length()));
		assertEquals("Assert that the style is translated.", "\u2828\u2828⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2831\u2800⠁⠗⠃⠑⠞⠎\u2824\u2800⠕⠉⠓\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠃⠇⠑⠧\u2800⠍⠊⠝⠙⠗⠑\u2800⠎⠅⠁⠗⠏⠆\u2800⠓⠕⠝\u2800⠅⠥⠝⠙⠑\u2800⠞⠊⠇⠇⠡⠞⠁\u2800⠎⠊⠛\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠍⠊⠞⠞\u2800⠊\u2800⠧⠑⠉⠅⠁⠝⠄", btr.getTranslatedRemainder());
	}

	@Test
	@Ignore
	public void testTranslatorAttributes_DisallowedStructure() {
		String numeric = "123";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder("sub");
		atts.add(new DefaultTextAttribute.Builder("sup").build(2));
		atts.add(1);
		BrailleTranslatorResult btr = translator.translate(numeric, atts.build(numeric.length()));
		System.out.println(btr.getTranslatedRemainder());
	}

}
