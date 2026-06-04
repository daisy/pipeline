import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.common.HyphenatorRegistry;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.braille.css.TextStyleParser;

import org.daisy.pipeline.junit.AbstractTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TexHyphenatorCoreTest extends AbstractTest {
	
	@Inject
	public HyphenatorRegistry provider;
	
	@Test
	public void testHyphenate() {
		assertEquals(text("foo\u00ADbar"),
		             provider.get(query("(table:'foobar.tex')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform(styledText("foobar", "hyphens: auto")));
		assertEquals(text("foo-\u200Bbar"),
		             provider.get(query("(table:'foobar.tex')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform(styledText("foo-bar", "hyphens: auto")));
		assertEquals(text("foo\u00ADbar"),
		             provider.get(query("(table:'foobar.properties')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform(styledText("foobar", "hyphens: auto")));
		assertEquals(text("foo-\u200Bbar"),
		             provider.get(query("(table:'foobar.properties')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform(styledText("foo-bar", "hyphens: auto")));
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
}
