package org.daisy.pipeline.braille.liblouis.impl;

import java.io.File;
import java.net.URL;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.TermList;
import cz.vutbr.web.csskit.TermIdentImpl;
import cz.vutbr.web.csskit.TermListImpl;

import static com.google.common.io.Files.createTempDir;

import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.AbstractResourcePath;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.NativePath;
import org.daisy.pipeline.braille.common.ResourcePath;
import org.daisy.pipeline.braille.common.StandardNativePath;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import static org.daisy.pipeline.braille.common.util.URLs.asURL;
import org.daisy.pipeline.braille.liblouis.impl.LiblouisNativePathForLinux;
import org.daisy.pipeline.braille.liblouis.impl.LiblouisTranslatorJnaImplProvider.LiblouisTranslatorImpl;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator.Typeform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;

import org.liblouis.CompilationException;
import org.liblouis.Louis;
import org.liblouis.Translator;

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
		
		NativePath liblouisNativePath = new LiblouisNativePath();
		Louis.setLibraryPath(asFile(liblouisNativePath.resolve(liblouisNativePath.get("liblouis").iterator().next())));
		File table = new File(LiblouisTranslatorJnaImplTest.class.getResource("/table_paths/table_path_1/foobar.ctb").toURI());
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
	
	private static class LiblouisNativePath extends StandardNativePath {
		ResourcePath delegate = new AbstractResourcePath() {
			URI identifier = asURI("http://www.liblouis.org/native/");
			URL basePath = asURL("jar:"
			                     + LiblouisNativePathForLinux.class.getProtectionDomain().getCodeSource().getLocation()
			                     + "!/native/");
			public URI getIdentifier() {
				return identifier;
			}
			protected URL getBasePath() {
				return basePath;
			}
			protected boolean isUnpacking() {
				return true;
			}
			protected File makeUnpackDir() {
				return createTempDir();
			}
			protected boolean isExecutable(URI resource) {
				return true;
			}
			protected boolean containsResource(URI resource) {
				return (LiblouisNativePathForLinux.class.getResource("/native/" + resource) != null);
			}
		};
		protected ResourcePath delegate() {
			return delegate;
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
