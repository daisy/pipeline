import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.common.CSSStyledText;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.dotify.DotifyHyphenator;
import org.daisy.pipeline.braille.dotify.DotifyTranslator;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class DotifyCoreTest {
	
	@Inject
	DotifyTranslator.Provider provider;
	
	@Inject
	DotifyHyphenator.Provider hyphenatorProvider;
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			domTraversalPackage(),
			felixDeclarativeServices(),
			thisBundle(),
			junitBundles(),
			mavenBundlesWithDependencies(
				brailleModule("common-utils"),
				brailleModule("css-core"),
				mavenBundle("org.daisy.dotify:dotify.api:?"),
				mavenBundle("org.daisy.dotify:dotify.translator.impl:?"),
				mavenBundle("org.daisy.dotify:dotify.hyphenator.impl:?"),
				mavenBundle("org.daisy.pipeline:calabash-adapter:?"),
				// logging
				logbackClassic(),
				mavenBundle("org.slf4j:jcl-over-slf4j:1.7.2")) // required by httpclient (TODO: add to runtime dependencies of calabash)
		);
	}
	
	@Test
	public void testSelect() {
		provider.get(query("(locale:sv-SE)")).iterator().next();
	}
	
	@Test
	public void testFuzzySelect() {
		provider.get(query("(locale:sv_SE_blah)")).iterator().next();
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
