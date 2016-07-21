package org.daisy.pipeline.braille.liblouis.impl;

import cz.vutbr.web.css.TermList;
import cz.vutbr.web.csskit.TermIdentImpl;
import cz.vutbr.web.csskit.TermListImpl;

import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.liblouis.LiblouisTranslator.Typeform;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LiblouisTranslatorJnaImplTest {
	
	private static byte typeformFromInlineCSS(String style) {
		return LiblouisTranslatorJnaImplProvider.typeformFromInlineCSS(new SimpleInlineStyle(style));
	}
	
	@Ignore // this doesn't work anymore because the print properties
			// text-decoration, font-weight and color are not supported by
			// org.daisy.braille.css.SimpleInlineStyle
	@Test
	public void testTypeformFromInlineCSS() {
		assertEquals(Typeform.BOLD + Typeform.UNDERLINE,
		             typeformFromInlineCSS(
			             " text-decoration: underline ;font-weight: bold  ; hyphens:auto; color: #FF00FF "));
	}

	private static String textFromTextTransform(String text, String... textTransform) {
		TermList list = new TermListImpl() {};
		for (String t : textTransform)
			list.add((new TermIdentImpl() {}).setValue(t));
		return LiblouisTranslatorJnaImplProvider.textFromTextTransform(text, list);
	}
	
	@Test
	public void testTextFromTextTransform() {
		assertEquals("IK BEN MOOS",
			textFromTextTransform("Ik ben Moos", "uppercase"));
		assertEquals("ik ben moos",
			textFromTextTransform("Ik ben Moos", "lowercase"));
		assertEquals("ik ben moos",
			textFromTextTransform("Ik ben Moos", "uppercase", "lowercase"));
		assertEquals("Ik ben Moos",
			textFromTextTransform("Ik ben Moos", "foo", "bar"));
	}
	
	private static byte typeformFromTextTransform(String... textTransform) {
		TermList list = new TermListImpl() {};
		for (String t : textTransform)
			list.add((new TermIdentImpl() {}).setValue(t));
		return LiblouisTranslatorJnaImplProvider.typeformFromTextTransform(list);
	}
	
	@Test
	public void testTypeformFromTextTransform() {
		assertEquals(Typeform.BOLD + Typeform.UNDERLINE,
			typeformFromTextTransform("louis-bold", "ital", "louis-under", "foo"));
	}
}
