package org.daisy.pipeline.braille.common;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.daisy.pipeline.braille.common.Hyphenator.LineIterator;
import org.daisy.pipeline.braille.common.util.Strings;
import org.daisy.pipeline.braille.css.CSSStyledText;

import org.junit.Assert;
import org.junit.Test;

public class HyphenatorWithExceptionsTest {

	@Test
	public void testHyphenate() {
		Hyphenator hyphenator = new TestHyphenator();
		Assert.assertEquals(text("foobar"),
		                    hyphenator.asFullHyphenator().transform(text("foobar")));
		Assert.assertEquals(text("foo-​bar"),
		                    hyphenator.asFullHyphenator().transform(text("foo-bar")));
		Assert.assertEquals(text("foo­bar"),
		                    hyphenator.asFullHyphenator().transform(text("foo­bar")));
		Assert.assertEquals(text("foo­bar"),
		                    hyphenator.asFullHyphenator().transform(styledText("foobar", "hyphens: auto")));
		Assert.assertEquals(
			"buss-\n" +
			"stopp",
			fillLines(hyphenator.asLineBreaker().transform("busstopp", null), 5, '-'));
	}

	@Test
	public void testHyphenateWithExceptions() throws IOException {
		Hyphenator hyphenator = new HyphenatorWithExceptions(
			new TestHyphenator(),
			new StringReader("foob-ar\n"));
		Assert.assertEquals(text("foobar"),
		                    hyphenator.asFullHyphenator().transform(text("foobar")));
		Assert.assertEquals(text("foo-​bar"),
		                    hyphenator.asFullHyphenator().transform(text("foo-bar")));
		Assert.assertEquals(text("foo­bar"),
		                    hyphenator.asFullHyphenator().transform(text("foo­bar")));
		Assert.assertEquals(text("foob­ar"),
		                    hyphenator.asFullHyphenator().transform(styledText("foobar", "hyphens: auto")));
		Assert.assertEquals(
			"foo-\n" +
			"bar",
			fillLines(hyphenator.asLineBreaker().transform("foo-bar", null), 5, '-'));
		Assert.assertEquals(
			"foo-\n" +
			"bar",
			fillLines(hyphenator.asLineBreaker().transform("foo­bar", null), 5, '-'));
		Assert.assertEquals(
			"foob-\n" +
			"ar",
			fillLines(hyphenator.asLineBreaker().transform("foobar", null), 5, '-'));
		Assert.assertEquals(
			"buss-\n" +
			"stopp",
			fillLines(hyphenator.asLineBreaker().transform("busstopp", null), 5, '-'));
		Assert.assertEquals(
			"buss-\n" +
			"stopp\n" +
			"foo-\n" +
			"bar",
			fillLines(hyphenator.asLineBreaker().transform("busstopp foo-bar", null), 5, '-'));
		Assert.assertEquals(
			"buss-\n" +
			"stopp\n" +
			"foo-\n" +
			"bar",
			fillLines(hyphenator.asLineBreaker().transform("busstopp foo­bar", null), 5, '-'));
		Assert.assertEquals(
			"buss-\n" +
			"stopp\n" +
			"foob-\n" +
			"ar",
			fillLines(hyphenator.asLineBreaker().transform("busstopp foobar", null), 5, '-'));
	}

	private static class TestHyphenator extends AbstractHyphenator {

		@Override
		public FullHyphenator asFullHyphenator() {
			return fullHyphenator;
		}

		private static final FullHyphenator fullHyphenator = new AbstractHyphenator.util.DefaultFullHyphenator() {

			private final static char SHY = '\u00AD';

			protected boolean isCodePointAware() { return false; }
			protected boolean isLanguageAdaptive() { return false; }

			protected byte[] getHyphenationOpportunities(String textWithoutHyphens, Locale _language) throws RuntimeException {
				if (textWithoutHyphens.contains("busstopp"))
					throw new NonStandardHyphenationException();
				else
					return Strings.extractHyphens(textWithoutHyphens.replace("foobar", "foo­bar"),
					                              isCodePointAware(),
					                              SHY)._2;
			}
		};

		@Override
		public LineBreaker asLineBreaker() {
			return lineBreaker;
		}

		private final LineBreaker lineBreaker = new AbstractHyphenator.util.DefaultLineBreaker() {
			protected Break breakWord(String word, Locale _language, int limit, boolean force) {
				if (limit >= 4 && word.equals("busstopp"))
					return new Break("bussstopp", 4, true);
				else
					return super.breakWord(word, _language, limit, force);
			}
		};
	}

	private Iterable<CSSStyledText> text(String... text) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		for (String t : text)
			styledText.add(new CSSStyledText(t, ""));
		return styledText;
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
	
	private static String fillLines(LineIterator lines, int width, char hyphen) {
		String s = "";
		while (lines.hasNext()) {
			lines.mark();
			String line = lines.nextLine(width, true);
			if (lines.lineHasHyphen())
				line += hyphen;
			if (line.length() > width) {
				lines.reset();
				line = lines.nextLine(width - 1, true);
				if (lines.lineHasHyphen())
					line += hyphen; }
			s += line;
			if (lines.hasNext()) {
				s += '\n';
				// do some primitive white space processing
				while (lines.hasNext() && lines.remainder().matches("^\\s.*")) {
					lines.mark();
					String space = lines.nextLine(1, true);
					if (space.length() != 1 || !space.matches("^\\s$")) {
						lines.reset();
						break;
					}
				}
			}
		}
		return s;
	}
}
