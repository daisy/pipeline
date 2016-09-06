import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import org.daisy.pipeline.braille.common.CSSStyledText;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import org.daisy.pipeline.braille.liblouis.LiblouisTablePath;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiTest {
	
	@Inject
	LiblouisTablePath path;
	
	@Test
	public void testTablePath() {
		assertEquals("http://www.sbs.ch/pipeline/liblouis/tables/", path.getIdentifier().toString());
		assertTrue(path.resolve(asURI("sbs.dis")) != null);
	}
	
	@Inject
	LiblouisTranslator.Provider translatorProvider;
	
	private static final String g1_table = join(
		new String[]{
			"sbs.dis",
			"sbs-de-core6.cti",
			"sbs-special.cti",
			"sbs-de-accents.cti",
			"sbs-whitespace.mod",
			"sbs-de-letsign.mod",
			"sbs-de-begendcaps.mod",
			"sbs-numsign.mod",
			"sbs-litdigit-upper.mod",
			"sbs-litdigit-lower.mod",
			"sbs-de-core.mod",
			"sbs-de-g0-core.mod",
			"sbs-de-g1-core.mod",
			"sbs-de-hyph-new.mod",
			"sbs-de-hyph-none.mod",
			"sbs-de-hyph-old.mod",
			"sbs-de-hyph-word.mod",
			"sbs-de-accents.mod",
			"sbs-de-accents-ch.mod",
			"sbs-de-accents-reduced.mod",
			"sbs-special.mod"
		},
		","
	);
		
	@Test
	public void testTranslator() {
		assertEquals(
			braille("WO4ENENDE"),
			translatorProvider.get(mutableQuery().add("liblouis-table", g1_table).add("output", "ascii"))
			                  .iterator().next().fromStyledTextToBraille()
			                  .transform(text("wochenende")));
	}
	
	@Test
	public void testWhitelist() {
		assertEquals(
			braille("WOOOH"),
			translatorProvider.get(mutableQuery().add("liblouis-table", g1_table + ",sbs-de-g1-white.mod").add("output", "ascii"))
			                  .iterator().next().fromStyledTextToBraille()
			                  .transform(text("wochenende")));
		assertEquals(
			braille("WOOOHOOOOW!"),
			translatorProvider.get(mutableQuery().add("liblouis-table", g1_table + ",sbs-de-g1-white-xyz.mod").add("output", "ascii"))
			                  .iterator().next().fromStyledTextToBraille()
			                  .transform(text("wochenende")));
	}
	
	@Configuration
	public Option[] config() {
		return options(
			systemProperty("ch.sbs.whitelist.base")
				.value(new File(PathUtils.getBaseDir(), "target/test-classes/whitelist/").getAbsolutePath()),
			logbackConfigFile(),
			felixDeclarativeServices(),
			domTraversalPackage(),
			thisBundle(),
			junitBundles(),
			mavenBundlesWithDependencies(
				brailleModule("liblouis-core"),
				brailleModule("liblouis-native").forThisPlatform(),
				logbackClassic()
			)
		);
	}
	
	private Iterable<CSSStyledText> text(String... text) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		for (String t : text)
			styledText.add(new CSSStyledText(t, ""));
		return styledText;
	}
	
	private Iterable<String> braille(String... text) {
		return Arrays.asList(text);
	}
}
