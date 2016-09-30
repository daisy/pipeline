package org.daisy.dotify.impl.translator.sv_SE;
import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.consumer.hyphenator.HyphenatorFactoryMaker;
import org.junit.Ignore;
import org.junit.Test;

public class SwedishBrailleTranslatorTest {
	private final static String TEST_INPUT_STRING_1 = "Skillnaden mellan arbets- och vilodagar blev mindre skarp; hon kunde tillåta sig vilodagar mitt i veckan.";
	private final BrailleTranslator translator;
	
	public SwedishBrailleTranslatorTest() throws TranslatorConfigurationException {
		this.translator = new SwedishBrailleTranslatorFactory(HyphenatorFactoryMaker.newInstance()).newTranslator("sv-SE",BrailleTranslatorFactory.MODE_UNCONTRACTED);
		// this.translator.setHyphenating(true);
	}

	@Test
	@Ignore
	public void testTranslator() throws TranslationException {
		//Setup
		BrailleTranslatorResult btr = translator.translate(Translatable.text(TEST_INPUT_STRING_1).build());
		//Test
		assertEquals("Assert that the output is translated.", 
				"⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2800⠁⠗⠃⠑⠞⠎\u2824\u2800⠕⠉⠓\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠃⠇⠑⠧\u2800⠍⠊⠝⠙⠗⠑\u2800⠎⠅⠁⠗⠏⠆\u2800⠓⠕⠝\u2800⠅⠥⠝⠙⠑\u2800⠞⠊⠇⠇⠡⠞⠁\u2800⠎⠊⠛\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠍⠊⠞⠞\u2800⠊\u2800⠧⠑⠉⠅⠁⠝⠄",
				btr.getTranslatedRemainder());
	}
	
	@Test
	@Ignore
	public void testTranslator_01firstRow() throws TranslationException {
		//Setup
		BrailleTranslatorResult btr = translator.translate(Translatable.text(TEST_INPUT_STRING_1).build());

		//Test
		assertEquals("Assert that limit is handled correctly.", 
				"⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2800⠁⠗⠃⠑⠞⠎\u2824",
				btr.nextTranslatedRow(26, false));
	}

	@Test
	@Ignore
	public void testTranslatorWithAnotherLanguage() throws TranslationException {
		//Setup
		BrailleTranslatorResult btr = translator.translate(Translatable.text(TEST_INPUT_STRING_1).locale("en").build());
		//Test
		assertEquals("Assert that the output is translated.", 
				"⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2800⠁⠗⠃⠑⠞⠎\u2824\u2800⠕⠉⠓\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠃⠇⠑⠧\u2800⠍⠊⠝⠙⠗⠑\u2800⠎⠅⠁⠗⠏⠆\u2800⠓⠕⠝\u2800⠅⠥⠝⠙⠑\u2800⠞⠊⠇⠇⠡⠞⠁\u2800⠎⠊⠛\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠍⠊⠞⠞\u2800⠊\u2800⠧⠑⠉⠅⠁⠝⠄",
				btr.getTranslatedRemainder());
	}

	@Test
	@Ignore
	public void testTranslatorAttributes_StyleBoldSingleWord() throws TranslationException {
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(new DefaultTextAttribute.Builder("strong").build(10));
		atts.add(new DefaultTextAttribute.Builder().build(95));
		BrailleTranslatorResult btr = translator.translate(Translatable.text(TEST_INPUT_STRING_1).attributes(atts.build(105)).build());
		assertEquals("Assert that the style is translated.", 
				"\u2828⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2800⠁⠗⠃⠑⠞⠎\u2824\u2800⠕⠉⠓\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠃⠇⠑⠧\u2800⠍⠊⠝⠙⠗⠑\u2800⠎⠅⠁⠗⠏⠆\u2800⠓⠕⠝\u2800⠅⠥⠝⠙⠑\u2800⠞⠊⠇⠇⠡⠞⠁\u2800⠎⠊⠛\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠍⠊⠞⠞\u2800⠊\u2800⠧⠑⠉⠅⠁⠝⠄",
				btr.getTranslatedRemainder());
	}

	@Test
	@Ignore
	public void testTranslatorAttributes_StyleBoldMultipleWords() throws TranslationException {
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(new DefaultTextAttribute.Builder("strong").build(17));
		atts.add(new DefaultTextAttribute.Builder().build(88));
		BrailleTranslatorResult btr = translator.translate(Translatable.text(TEST_INPUT_STRING_1).attributes(atts.build(TEST_INPUT_STRING_1.length())).build());
		assertEquals("Assert that the style is translated.", "\u2828\u2828⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝\u2800⠍⠑⠇⠇⠁⠝\u2831\u2800⠁⠗⠃⠑⠞⠎\u2824\u2800⠕⠉⠓\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠃⠇⠑⠧\u2800⠍⠊⠝⠙⠗⠑\u2800⠎⠅⠁⠗⠏⠆\u2800⠓⠕⠝\u2800⠅⠥⠝⠙⠑\u2800⠞⠊⠇⠇⠡⠞⠁\u2800⠎⠊⠛\u2800⠧⠊⠇⠕⠙⠁⠛⠁⠗\u2800⠍⠊⠞⠞\u2800⠊\u2800⠧⠑⠉⠅⠁⠝⠄", btr.getTranslatedRemainder());
	}
	
	@Test
	public void testTranslatorAttributes_Nested() throws TranslationException {
		String text = "page 1";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder("strong");
		atts.add(5);
		atts.add(new DefaultTextAttribute.Builder("em").build(1));
		TextAttribute ta = new DefaultTextAttribute.Builder().add(atts.build(6)).build(6);
		BrailleTranslatorResult btr = translator.translate(Translatable.text(text).attributes(ta).build());
		assertEquals("", "⠨⠨⠏⠁⠛⠑⠀⠠⠄⠼⠁⠱", btr.getTranslatedRemainder());
	}

	@Test
	@Ignore
	public void testTranslatorAttributes_DisallowedStructure() throws TranslationException {
		String numeric = "123";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder("sub");
		atts.add(new DefaultTextAttribute.Builder("sup").build(2));
		atts.add(1);
		BrailleTranslatorResult btr = translator.translate(Translatable.text(numeric).attributes(atts.build(numeric.length())).build());
		System.out.println(btr.getTranslatedRemainder());
	}

}
