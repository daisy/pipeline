import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import org.daisy.braille.css.SimpleInlineStyle;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.dotify.DotifyTranslator;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.braille.css.TextStyleParser;
import org.daisy.pipeline.junit.AbstractTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DotifyCoreTest extends AbstractTest {
	
	@Inject
	public DotifyTranslator.Provider provider;
	
	@Test
	public void testSelect() {
		provider.get(query("(locale:sv-SE)")).iterator().next();
	}
	
	@Test
	public void testFuzzySelect() {
		provider.get(query("(document-locale:sv_SE_blaah)")).iterator().next();
	}
	
	@Test
	public void testTranslate() {
		assertEquals(styledText("⠋⠕⠕⠃⠁⠗", ""),
		             provider.get(query("(locale:sv-SE)")).iterator().next()
		                     .fromStyledTextToBraille()
		                     .transform(styledText("foobar","")));
	}
	
	@Test
	public void testTranslateAndHyphenate() {
		assertEquals(styledText("⠋⠕⠕\u00AD⠃⠁⠗", ""),
		             provider.get(query("(locale:sv-SE)(document-locale:sv-SE)")).iterator().next()
		                     .fromStyledTextToBraille()
		                     .transform(styledText("foobar","hyphens:auto")));

	}
	
	@Test(expected=RuntimeException.class)
	public void testTranslateAndNotHyphenate() {
		assertEquals(styledText("⠋⠕⠕\u00AD⠃⠁⠗", ""),
		             provider.get(query("(locale:sv-SE)")).iterator().next()
		                     .fromStyledTextToBraille()
		                     .transform(styledText("foobar","hyphens:auto")));
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
}
