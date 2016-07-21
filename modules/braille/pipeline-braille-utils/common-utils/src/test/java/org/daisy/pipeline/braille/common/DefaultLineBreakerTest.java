package org.daisy.pipeline.braille.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.Hyphenator;

public class DefaultLineBreakerTest {
	
	@Test
	public void testLineBreaking() {
		TestHyphenator hyphenator = new TestHyphenator();
		TestTranslator translator = new TestTranslator(hyphenator);
		assertEquals(
			"BUSS\n" +
			"TOPP",
			fillLines(translator.lineBreakingFromStyledText().transform(text("busstopp")), 4));
		assertEquals(
			"BUSS-\n" +
			"STOPP",
			fillLines(translator.lineBreakingFromStyledText().transform(text("busstopp")), 5));
		assertEquals(
			"BUSS-\n" +
			"STOPP",
			fillLines(translator.lineBreakingFromStyledText().transform(text("busstopp")), 6));
		assertEquals(
			"BUSS-\n" +
			"STOPP",
			fillLines(translator.lineBreakingFromStyledText().transform(text("busstopp")), 7));
	}
	
	private static class TestHyphenator extends AbstractHyphenator {
		
		@Override
		public FullHyphenator asFullHyphenator() {
			return fullHyphenator;
		}
		
		private static final FullHyphenator fullHyphenator = new FullHyphenator() {
			public String transform(String text) {
				if (text.contains("busstopp"))
					throw new RuntimeException("text contains non-standard break points");
				else
					return text;
			}
			public String[] transform(String[] text) {
				String[] r = new String[text.length];
				for (int i = 0; i < r.length; i++)
					r[i] = transform(text[i]);
				return r;
			}
		};
		
		@Override
		public LineBreaker asLineBreaker() {
			return lineBreaker;
		}
		
		private final LineBreaker lineBreaker = new AbstractHyphenator.util.DefaultLineBreaker() {
			protected Break breakWord(String word, int limit, boolean force) {
				if (limit >= 4 && word.equals("busstopp"))
					return new Break("bussstopp", 4, true);
				else if (limit >= word.length())
					return new Break(word, word.length(), false);
				else if (force)
					return new Break(word, limit, false);
				else
					return new Break(word, 0, false);
			}
		};
	}
	
	private static class TestTranslator extends AbstractBrailleTranslator {
		
		private final Hyphenator hyphenator;
		
		private TestTranslator(Hyphenator hyphenator) {
			this.hyphenator = hyphenator;
		}
		
		private final static Pattern WORD_SPLITTER = Pattern.compile("[\\x20\t\\n\\r\\u2800\\xA0]+");
		
		private final LineBreakingFromStyledText lineBreaker = new AbstractBrailleTranslator.util.DefaultLineBreaker(' ', '-', null) {
			protected BrailleStream translateAndHyphenate(final Iterable<CSSStyledText> styledText) {
				return new BrailleStream() {
					int pos = 0;
					String text; {
						text = "";
						for (CSSStyledText t : styledText)
							text += t.getText(); }
					public boolean hasNext() {
						return pos < text.length();
					}
					public String next(int limit, boolean force) {
						String next = "";
						int start = pos;
						int end = text.length();
						int available = limit;
						if (end - start <= available) {
							next = text.substring(start);
							pos = end; }
						else {
							try {
								next = hyphenator.asFullHyphenator().transform(text.substring(pos));
								pos = end; }
							catch (Exception e) {
								Matcher m = WORD_SPLITTER.matcher(text.substring(pos));
								boolean foundSpace;
								while ((foundSpace = m.find()) || pos < end) {
									int wordStart = pos;
									int wordEnd = foundSpace ? start + m.start() : end;
									if (wordEnd > wordStart) {
										String word = text.substring(wordStart, wordEnd);
										if (word.length() <= available) {
											next += word;
											available -= word.length();
											pos += word.length(); }
										else if (available <= 0)
											break;
										else {
											Hyphenator.LineIterator lines = hyphenator.asLineBreaker().transform(word);
											String line = lines.nextLine(available, force);
											if (line.length() == available && lines.lineHasHyphen()) {
												lines.reset();
												line = lines.nextLine(available - 1, force); }
											if (line.length() > 0) {
												next += line;
												if (lines.lineHasHyphen())
													next += "-\u200b";
												pos += line.length();
												text = text.substring(0, pos) + lines.remainder(); }
											break; }}
									if (foundSpace) {
										String space = text.substring(pos, start + m.end());
										next += space;
										available -= space.length();
										pos = space.length(); }}}}
						return translate(next);
					}
					public Character peek() {
						return text.charAt(pos);
					}
					public String remainder() {
						return translate(text.substring(pos));
					}
					int markPos;
					String markText; {
						mark(); }
					public void mark() {
						markPos = pos;
						markText = text;
					}
					public void reset() {
						pos = markPos;
						text = markText;
					}
					private String translate(String s) {
						return s.toUpperCase();
					}
				};
			}
		};
		
		@Override
		public LineBreakingFromStyledText lineBreakingFromStyledText() {
			return lineBreaker;
		}
	}
	
	private Iterable<CSSStyledText> text(String... text) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		for (String t : text)
			styledText.add(new CSSStyledText(t, ""));
		return styledText;
	}
	
	private static String fillLines(BrailleTranslator.LineIterator lines, int width) {
		StringBuilder sb = new StringBuilder();
		while (lines.hasNext()) {
			sb.append(lines.nextTranslatedRow(width, true));
			if (lines.hasNext())
				sb.append('\n'); }
		return sb.toString();
	}
}
