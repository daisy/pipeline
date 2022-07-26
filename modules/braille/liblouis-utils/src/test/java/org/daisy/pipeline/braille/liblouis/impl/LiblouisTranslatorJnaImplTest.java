package org.daisy.pipeline.braille.liblouis.impl;

import java.io.File;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import cz.vutbr.web.css.TermList;
import cz.vutbr.web.csskit.TermIdentImpl;
import cz.vutbr.web.csskit.TermListImpl;

import org.daisy.braille.css.SimpleInlineStyle;

import static org.daisy.common.file.URLs.asURL;

import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.NativePath;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import org.daisy.pipeline.braille.liblouis.impl.LiblouisTranslatorJnaImplProvider.LiblouisTranslatorImpl;
import org.daisy.pipeline.junit.OSGiLessRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.liblouis.CompilationException;
import org.liblouis.DisplayTable.StandardDisplayTables;
import org.liblouis.Louis;
import org.liblouis.Translator;

@RunWith(OSGiLessRunner.class)
public class LiblouisTranslatorJnaImplTest {
	
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
		
		// FIXME: fix in liblouis-java
		Louis.setTableResolver(new org.liblouis.TableResolver() {
				public URL resolve(String table, URL base) {
					if (base != null && base.toString().startsWith("file:")) {
						File f = base.toString().endsWith("/")
							? new File(asFile(base), table)
							: new File(asFile(base).getParentFile(), table);
						if (f.exists())
							return asURL(f);
					} else if (base == null) {
						File f = new File(table);
						if (f.exists())
							return asURL(f);
					}
					return null;
				}
				public java.util.Set<String> list() {
					return new java.util.HashSet<String>();
				}
			}
		);
		Translator liblouisTranslator = new Translator(table.getAbsolutePath());
		LiblouisTranslatorImpl.LineBreaker.BrailleStreamImpl stream
		= new LiblouisTranslatorImpl.LineBreaker.BrailleStreamImpl(
			liblouisTranslator,
			StandardDisplayTables.DEFAULT,
			null,
			hyphenator,
			null,
			null,
			styledText("volleyballederen volleyballederen", "hyphens:auto"),
			0, -1);
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
