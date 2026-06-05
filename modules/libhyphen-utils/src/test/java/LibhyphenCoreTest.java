import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Hyphenator.FullHyphenator;
import static org.daisy.pipeline.braille.common.Hyphenator.LineBreaker;
import static org.daisy.pipeline.braille.common.Hyphenator.LineIterator;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.braille.css.TextStyleParser;

import org.daisy.pipeline.braille.libhyphen.LibhyphenHyphenator;

import org.daisy.pipeline.junit.AbstractTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibhyphenCoreTest extends AbstractTest {
	
	@Inject
	public LibhyphenHyphenator.Provider provider;
	
	private static final Logger messageBus = LoggerFactory.getLogger("JOB_MESSAGES");
	
	@Test
	public void testStandardHyphenation() {
		FullHyphenator hyphenator= provider.withContext(messageBus)
		                                   .get(query("(table:'standard.dic')"))
		                                   .iterator().next()
		                                   .asFullHyphenator();
		assertEquals(text("foo\u00ADbar"), hyphenator.transform(styledText("foobar", "hyphens: auto")));
		assertEquals(text("foo-\u200Bbar"), hyphenator.transform(styledText("foo-bar", "hyphens: auto")));
		assertEquals(text("foo-\u200Bbar"), hyphenator.transform(styledText("foo-bar", "hyphens: none")));
		assertEquals(text("foo\u00ADbar foob\u00ADar"), hyphenator.transform(styledText("foobar foob\u00ADar", "hyphens: auto")));
	}
	
	@Test
	public void testSurrogatePairs() {
		FullHyphenator hyphenator= provider.withContext(messageBus)
		                                   .get(query("(table:'standard.dic')"))
		                                   .iterator().next()
		                                   .asFullHyphenator();
		assertEquals(text("\uD83D\uDE00\u00AD\uD83D\uDE00"), // \uD83D\uDE00 = U+1F600 grinning face
		             hyphenator.transform(styledText("\uD83D\uDE00\u00AD\uD83D\uDE00", "hyphens: auto")));
	}
	
	@Test(expected=RuntimeException.class)
	public void testStandardHyphenationException() {
		FullHyphenator hyphenator= provider.withContext(messageBus)
		                                   .get(query("(table:'non-standard.dic')"))
		                                   .iterator().next()
		                                   .asFullHyphenator();
		hyphenator.transform(styledText("foobar", "hyphens: auto"));
	}
	
	@Test
	public void testNonStandardHyphenation() {
		LineBreaker hyphenator= provider.withContext(messageBus)
		                                .get(query("(table:'non-standard.dic')"))
		                                .iterator().next()
		                                .asLineBreaker();
		assertEquals("f\n" +
		             "oo\n" +
		             "ba\n" +
		             "r",
		             fillLines(hyphenator.transform("foobar", null), 2, '-'));
		assertEquals("fu-\n" +
		             "bar",
		             fillLines(hyphenator.transform("foobar", null), 3, '-'));
		assertEquals("foo-\n" +
		             "bar",
		             fillLines(hyphenator.transform("foo-bar", null), 4, '-'));
		assertEquals("foo-\n" +
		             "bar",
		             fillLines(hyphenator.transform("foo-bar", null), 5, '-'));
		assertEquals("foo-\n" +
		             "bar",
		             fillLines(hyphenator.transform("foo-bar", null), 6, '-'));
	}

	private final static TextStyleParser cssParser = TextStyleParser.getInstance();
	
	private Iterable<CSSStyledText> styledText(String... textAndStyle) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		String text = null;
		boolean textSet = false;
		for (String s : textAndStyle) {
			if (textSet)
				styledText.add(new CSSStyledText(text, cssParser.parse(s)));
			else
				text = s;
			textSet = !textSet; }
		if (textSet)
			throw new RuntimeException();
		return styledText;
	}
	
	private Iterable<CSSStyledText> text(String... text) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		for (String t : text)
			styledText.add(new CSSStyledText(t, cssParser.parse("")));
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
			if (lines.hasNext())
				s += '\n'; }
		return s;
	}
}
