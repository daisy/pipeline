import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.common.CSSStyledText;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.dotify.DotifyHyphenator;
import org.daisy.pipeline.braille.dotify.DotifyTranslator;
import org.daisy.pipeline.junit.AbstractTest;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class DotifyCoreTest extends AbstractTest {
	
	@Inject
	public DotifyTranslator.Provider provider;
	
	@Inject
	public DotifyHyphenator.Provider hyphenatorProvider;
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("css-utils"),
			pipelineModule("css-utils"),
			"org.daisy.pipeline:calabash-adapter:?",
			// because the exclusion of com.fasterxml.woodstox:woodstox-core from the dotify.library
			// dependencies causes stax2-api to be excluded too
			"org.codehaus.woodstox:stax2-api:jar:?",
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			// apparently the liblouis-java exclusion defined in modules-bom does not have an effect
			mavenBundle("org.daisy.dotify:dotify.library:?"),
			composite(super.config()));
	}
	
	@Test
	public void testSelect() {
		provider.get(query("(locale:sv-SE)")).iterator().next();
	}
	
	@Test
	public void testFuzzySelect() {
		provider.get(query("(locale:sv_SE_blaah)")).iterator().next();
	}
	
	@Test
	public void testTranslate() {
		assertEquals(braille("⠋⠕⠕⠃⠁⠗"),
		             provider.get(query("(locale:sv-SE)")).iterator().next()
		                     .fromStyledTextToBraille()
		                     .transform(styledText("foobar","")));
	}
	
	@Test
	public void testHyphenate() {
		assertEquals("foo\u00ADbar",
		             hyphenatorProvider.get(query("(locale:sv-SE)")).iterator().next()
		                               .asFullHyphenator()
		                               .transform(new String[]{"foobar"})[0]);
	}
	
	@Test
	public void testTranslateAndHyphenate() {
		assertEquals(braille("⠋⠕⠕\u00AD⠃⠁⠗"),
		             provider.get(query("(locale:sv-SE)")).iterator().next()
		                     .fromStyledTextToBraille()
		                     .transform(styledText("foobar","hyphens:auto")));

	}
	
	@Test(expected=RuntimeException.class)
	public void testTranslateAndNotHyphenate() {
		assertEquals(braille("⠋⠕⠕\u00AD⠃⠁⠗"),
		             provider.get(query("(locale:sv-SE)(hyphenator:none)")).iterator().next()
		                     .fromStyledTextToBraille()
		                     .transform(styledText("foobar","hyphens:auto")));
	}
	
	private Iterable<CSSStyledText> styledText(String... textAndStyle) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		String text = null;
		boolean textSet = false;
		for (String s : textAndStyle) {
			if (textSet)
				styledText.add(new CSSStyledText(text, new SimpleInlineStyle(s)));
			else
				text = s;
			textSet = !textSet; }
		if (textSet)
			throw new RuntimeException();
		return styledText;
	}
	
	private Iterable<String> braille(String... text) {
		return Arrays.asList(text);
	}
}
