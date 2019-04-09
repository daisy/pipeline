package org.daisy.pipeline.braille.liblouis.impl;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import cz.vutbr.web.css.TermList;
import cz.vutbr.web.csskit.TermIdentImpl;
import cz.vutbr.web.csskit.TermListImpl;

import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.NativePath;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import org.daisy.pipeline.braille.liblouis.impl.LiblouisTranslatorJnaImplProvider.LiblouisTranslatorImpl;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator.Typeform;
import org.daisy.pipeline.junit.OSGiLessRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.liblouis.CompilationException;
import org.liblouis.Louis;
import org.liblouis.Translator;

@RunWith(OSGiLessRunner.class)
public class LiblouisTranslatorJnaImplTest {
	
	private static short typeformFromInlineCSS(String style) {
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
	
	private static short typeformFromTextTransform(String... textTransform) {
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
	
	@Test
	public void testLineBreaker() throws URISyntaxException, CompilationException {
		
		Hyphenator.LineBreaker hyphenator = new MockHyphenator();
		Hyphenator.LineIterator lines = hyphenator.transform("volleyballederen");
		assertEquals("volleyballederen", lines.nextLine(26, false));
		assertFalse(lines.hasNext());
		lines = hyphenator.transform("volleyballederen");
		assertEquals("", lines.nextLine(9, false));
		assertEquals("volleyballederen", lines.remainder());
		lines.reset();
		assertEquals("volleyball", lines.nextLine(14, false));
		assertEquals("lederen", lines.remainder());
		assertTrue(lines.lineHasHyphen());
		lines.reset();
		assertEquals("volleyballederen", lines.nextLine(26, false));
		assertFalse(lines.hasNext());
		
		Louis.setLibraryPath(asFile(liblouisNativePath.resolve(liblouisNativePath.get("liblouis").iterator().next())));
		File table = new File(LiblouisTranslatorJnaImplTest.class.getResource("/tables/foobar.ctb").toURI());
		Translator liblouisTranslator = new Translator(table.getAbsolutePath());
		LiblouisTranslatorImpl.LineBreaker.BrailleStreamImpl stream
		= new LiblouisTranslatorImpl.LineBreaker.BrailleStreamImpl(
			liblouisTranslator,
			hyphenator,
			null,
			styledText("volleyballederen volleyballederen", "hyphens:auto"));
		assertEquals("volleyballederen ", stream.next(26, false, true));
		assertEquals("volleyballederen", stream.next(26, false, true));
		assertFalse(stream.hasNext());
	}
	
	@Inject
	public NativePath liblouisNativePath;
	
	private static class MockHyphenator extends AbstractHyphenator.util.DefaultLineBreaker {
		protected Break breakWord(String word, int limit, boolean force) {
			if (limit >= 10 && word.equals("volleyballederen"))
				return new Break("volleyballlederen", 10, true);
			else if (limit >= word.length())
				return new Break(word, word.length(), false);
			else if (force)
				return new Break(word, limit, false);
			else
				return new Break(word, 0, false);
		}
	}
	
	private Iterable<CSSStyledText> styledText(String... textAndStyle) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		String text = null;
		boolean textSet = false;
		for (String s : textAndStyle) {
			if (textSet)
				styledText.add(new CSSStyledText(text, s));
			else
				text = s;
			textSet = !textSet; }
		if (textSet)
			throw new RuntimeException();
		return styledText;
	}
}
