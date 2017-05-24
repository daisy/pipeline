import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import com.google.common.base.Optional;

import org.daisy.braille.api.table.BrailleConverter;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.api.table.TableCatalogService;

import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.BrailleTranslator.FromStyledTextToBraille;
import org.daisy.pipeline.braille.common.BrailleTranslator.LineBreakingFromStyledText;
import org.daisy.pipeline.braille.common.BrailleTranslator.LineIterator;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.HyphenatorProvider;
import org.daisy.pipeline.braille.common.Provider;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;

import org.daisy.pipeline.braille.liblouis.LiblouisHyphenator;
import org.daisy.pipeline.braille.liblouis.LiblouisTable;
import org.daisy.pipeline.braille.liblouis.LiblouisTableResolver;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator.Typeform;
import org.daisy.pipeline.braille.pef.TableProvider;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class LiblouisCoreTest {
	
	@Inject
	LiblouisTranslator.Provider provider;
	
	@Inject
	LiblouisHyphenator.Provider hyphenatorProvider;
	
	@Inject
	LiblouisTableResolver resolver;
	
	@Inject
	private TableCatalogService tableCatalog;
	
	private static final Logger messageBus = LoggerFactory.getLogger("JOB_MESSAGES");
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			domTraversalPackage(),
			felixDeclarativeServices(),
			thisBundle(),
			junitBundles(),
			mavenBundlesWithDependencies(
				mavenBundle("org.liblouis:liblouis-java:?"),
				mavenBundle("org.daisy.braille:braille-utils.api:?"),
				brailleModule("common-utils"),
				brailleModule("pef-core"),
				brailleModule("css-core"),
				brailleModule("liblouis-native").forThisPlatform(),
				// logging
				logbackClassic(),
				mavenBundle("org.slf4j:jcl-over-slf4j:1.7.2")), // required by httpclient (TODO: add to runtime dependencies of calabash)
			bundle("reference:file:" + PathUtils.getBaseDir() + "/target/test-classes/table_paths/")
		);
	}
	
	@Test
	public void testResolveTableFile() {
		assertEquals("foobar.cti", asFile(resolver.resolve(asURI("foobar.cti"))).getName());
	}
	
	@Test
	public void testResolveTable() {
		assertEquals("foobar.cti", (resolver.resolveLiblouisTable(new LiblouisTable("foobar.cti"), null)[0]).getName());
	}
	
	@Test
	public void testGetTranslatorFromQuery1() {
		provider.withContext(messageBus).get(query("(locale:foo)")).iterator().next();
	}
	
	@Test
	public void testGetTranslatorFromQuery2() {
		provider.withContext(messageBus).get(query("(table:'foobar.cti')")).iterator().next();
	}
	
	@Test
	public void testGetTranslatorFromQuery3() {
		provider.withContext(messageBus).get(query("(locale:foo_BAR)")).iterator().next();
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testGetTranslatorFromQuery4() {
		provider.withContext(messageBus).get(query("(locale:bar)")).iterator().next();
	}
	
	@Test
	public void testTranslate() {
		assertEquals(braille("⠋⠕⠕⠃⠁⠗"),
		             provider.withContext(messageBus)
		                     .get(query("(table:'foobar.cti')")).iterator().next()
		                     .fromStyledTextToBraille().transform(text("foobar")));
	}
	
	@Test
	public void testTranslateStyled() {
		assertArrayEquals(new String[]{"⠋⠕⠕⠃⠁⠗"},
		                  provider.withContext(messageBus)
		                          .get(query("(table:'foobar.cti')")).iterator().next()
		                          .fromTypeformedTextToBraille().transform(new String[]{"foobar"}, new byte[]{Typeform.ITALIC}));
	}
	
	@Test
	public void testTranslateSegments() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.cti')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠋⠕⠕","⠃⠁⠗"),
		             translator.transform(text("foo","bar")));
		assertEquals(braille("⠋⠕⠕","","⠃⠁⠗"),
		             translator.transform(text("foo","","bar")));
	}
	
	@Test
	public void testTranslateSegmentsFuzzy() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.ctb')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠋⠥","⠃⠁⠗"),
		             translator.transform(text("foo","bar")));
		assertEquals(braille("⠋⠥","⠃⠁⠗"),
		             translator.transform(text("fo","obar")));
		assertEquals(braille("⠋⠥\u00AD","⠃⠁⠗"),
		             translator.transform(text("fo","o\u00ADbar")));
		assertEquals(braille("⠋⠥","","⠃⠁⠗"),
		             translator.transform(text("fo","","obar")));
	}
	
	@Test
	public void testHyphenate() {
		assertEquals("foo\u00ADbar",
		             hyphenatorProvider.withContext(messageBus)
		                 .get(query("(table:'foobar.cti,foobar.dic')")).iterator().next()
		                 .asFullHyphenator()
		                 .transform("foobar"));
	}
	
	@Test
	public void testHyphenateCompoundWord() {
		assertEquals("foo-\u200Bbar",
		             hyphenatorProvider.withContext(messageBus)
		                 .get(query("(table:'foobar.cti,foobar.dic')")).iterator().next()
		                 .asFullHyphenator()
		                 .transform("foo-bar"));
	}
	
	private static class MockHyphenator extends AbstractHyphenator {
		private static final LineBreaker lineBreaker = new DefaultLineBreaker() {
			protected Break breakWord(String word, int limit, boolean force) {
				if (limit >= 3 && word.equals("foobarz"))
					return new Break("fubbarz", 3, true);
				else if (limit >= word.length())
					return new Break(word, word.length(), false);
				else if (force)
					return new Break(word, limit, false);
				else
					return new Break(word, 0, false);
			}
		};
		@Override
		public LineBreaker asLineBreaker() {
			return lineBreaker;
		}
		private static final MockHyphenator instance = new MockHyphenator() {
			@Override
			public String getIdentifier() {
				return "mock";
			}
		};
		public static class Provider extends AbstractTransformProvider<MockHyphenator>
		                             implements HyphenatorProvider<MockHyphenator> {
			{
				get(query("")); // in order to save instance in id-based cache
			}
			public Iterable<MockHyphenator> _get(Query query) {
				return AbstractTransformProvider.util.Iterables.<MockHyphenator>of(instance);
			}
		}
	}
	
	@Test
	public void testTranslateAndHyphenateNonStandard() {
		registerService(new MockHyphenator.Provider(), HyphenatorProvider.class);
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.ctb')(hyphenator:mock)(output:ascii)")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			"fu\n" +
			"bar\n" +
			"z",
			fillLines(translator.transform(styledText("foobarz", "hyphens:auto")), 3));
		assertEquals(
			"fub⠤\n" +
			"barz",
			fillLines(translator.transform(styledText("foobarz", "hyphens:auto")), 4));
		assertEquals(
			"fub⠤\n" +
			"barz",
			fillLines(translator.transform(styledText("foobarz", "hyphens:auto")), 5));
		assertEquals(
			"fubarz",
			fillLines(translator.transform(styledText("foobarz", "hyphens:auto")), 6));
	}
	
	@Test
	public void testTranslateAndHyphenateSomeSegments() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.cti,foobar.dic')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠋⠕⠕\u00AD⠃⠁⠗ ","⠋⠕⠕⠃⠁⠗"),
		             translator.transform(styledText("foobar ", "hyphens:auto",
		                                             "foobar",  "hyphens:none")));
	}
	
	@Test
	public void testWhiteSpaceProcessing() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.cti')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠋⠕⠕    ⠃⠁⠗ ⠃⠁⠵"),
		             translator.transform(text("foo    bar\nbaz")));
		assertEquals(braille("⠋⠕⠕    ⠃⠁⠗\n⠃⠁⠵"),
		             translator.transform(styledText("foo    bar\nbaz", "white-space:pre-wrap")));
		assertEquals(braille("",
		                     "⠋⠕⠕    ⠃⠁⠗\n\u00AD",
		                     "",
		                     "⠃⠁⠵"),
		             translator.transform(styledText("",             "",
		                                             "foo    bar\n", "white-space:pre-wrap",
		                                             "\u00AD",       "",
		                                             "baz",          "")));
		assertEquals(braille("\n"),
		             translator.transform(styledText("\n", "white-space:pre-line")));
	}
	
	@Test
	public void testWhiteSpaceLost() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.cti,delete-ws.utb')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("",
		                     "⠋⠕⠕⠃⠁⠗\u00AD",
		                     "",
		                     "⠃⠁⠵"),
		             translator.transform(styledText("",             "",
		                                             "foo    bar\n", "white-space:pre-wrap",
		                                             "\u00AD",       "",
		                                             "baz",          "")));
	}
	
	@Test
	public void testSegmentationPreservedDespiteSpacesCollapsed() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.cti,squash-ws.utb')(output:ascii)")).iterator().next()
		                                             .fromStyledTextToBraille();
		int n = 10000;
		String[] textSegments = new String[n]; {
			textSegments[0] = "foo";
			for (int i = 1; i < n - 1; i++)
				textSegments[i] = " ";
			textSegments[n - 1] = "bar"; }
		String[] brailleSegments = new String[n]; {
			brailleSegments[0] = "foo";
			brailleSegments[1] = " ";
			for (int i = 2; i < n - 1; i++)
				brailleSegments[i] = "";
			brailleSegments[n - 1] = "bar"; }
		assertEquals(braille(brailleSegments),
		             translator.transform(text(textSegments)));
	}
	
	@Test
	public void testTranslateWithLetterSpacingAndPunctuations() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.cti')(output:ascii)")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(
			braille("f o o b a r."),
			translator.transform(styledText("foobar.", "letter-spacing:1")));
		assertEquals(
			braille("f  o  o  b  a  r."),
			translator.transform(styledText("foobar.", "letter-spacing:2")));
	}
	
	@Test
	public void testTranslateWithLetterSpacingAndHyphenation() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.cti,foobar.dic')(output:ascii)")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(
			braille("f o o\u00AD b a r"),
			translator.transform(styledText("foobar", "letter-spacing:1; hyphens:auto")));
	}
	
	@Test
	public void testTranslateWithLetterSpacingAndContractions() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.ctb')(output:ascii)")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(
			braille("fu b a r"),
			translator.transform(styledText("foobar", "letter-spacing:1")));
		assertEquals(
			braille("fu  b  a  r"),
			translator.transform(styledText("foobar", "letter-spacing:2")));
	}
	
	@Test
	public void testTranslateWithLetterSpacingAndContractionsFuzzy() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.ctb')(output:ascii)")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("fu ","b a r"),
		             translator.transform(styledText("foo", "letter-spacing:1",
		                                             "bar", "letter-spacing:1")));
		assertEquals(braille("fu ","b a r"),
		             translator.transform(styledText("fo",   "letter-spacing:1",
		                                             "obar", "letter-spacing:1")));
		assertEquals(braille("fu\u00AD ","b a r"),
		             translator.transform(styledText("fo",         "letter-spacing:1",
		                                             "o\u00ADbar", "letter-spacing:1")));
		assertEquals(braille("fu ","","b a r"),
		             translator.transform(styledText("fo",   "letter-spacing:1",
		                                             "",     "letter-spacing:1",
		                                             "obar", "letter-spacing:1")));
	}
	
	@Test
	public void testTranslateWithWordSpacing() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.cti')(output:ascii)")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			"foo⠀⠀bar",
			translator.transform(styledText("foo bar", "word-spacing:2")).getTranslatedRemainder());
		assertEquals(
			"foo⠀⠀⠀bar",
			translator.transform(styledText("foo bar", "word-spacing:3")).getTranslatedRemainder());
	}

	@Test
	public void testTranslateWithWhiteSpaceProcessingAndWordSpacing() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.cti')(output:ascii)")).iterator().next()
		                                                .lineBreakingFromStyledText();
		// space in input, two spaces in output
		assertEquals(
			"foo⠀⠀bar",
			translator.transform(styledText("foo bar", "word-spacing:2")).getTranslatedRemainder());
		// two spaces in input, two spaces in output
		assertEquals(
			"foo⠀⠀bar",
			translator.transform(styledText("foo  bar", "word-spacing:2")).getTranslatedRemainder());
		// newline + tab in input, two spaces in output
		assertEquals(
			"foo⠀⠀bar",
			translator.transform(styledText("foo\n	bar", "word-spacing:2")).getTranslatedRemainder());
		// no-break space in input, space in output
		assertEquals(
			"foo⠀bar",
			translator.transform(styledText("foo bar", "word-spacing:2")).getTranslatedRemainder());
		// no-break space + space in input, three spaces in output
		assertEquals(
			"foo⠀⠀⠀bar",
			translator.transform(styledText("foo  bar", "word-spacing:2")).getTranslatedRemainder());
		// zero-width space in input, no space in output
		assertEquals(
			"foobar",
			translator.transform(styledText("foo​bar", "word-spacing:2")).getTranslatedRemainder());
	}
	
	@Test
	public void testTranslateWithLetterSpacing() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.cti')(output:ascii)")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			"f⠀o⠀o⠀b⠀a⠀r⠀⠀⠀q⠀u⠀u⠀x⠀⠀⠀#abcdef",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:1; word-spacing:3")).getTranslatedRemainder());
		assertEquals(
			"f⠀⠀o⠀⠀o⠀⠀b⠀⠀a⠀⠀r⠀⠀⠀⠀⠀q⠀⠀u⠀⠀u⠀⠀x⠀⠀⠀⠀⠀#abcdef",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:2; word-spacing:5")).getTranslatedRemainder());
	}

	@Test
	public void testTranslateWithLetterSpacingAndWordSpacing() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.cti')(output:ascii)")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			"f⠀o⠀o⠀b⠀a⠀r⠀⠀q⠀u⠀u⠀x⠀⠀#abcdef",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:1; word-spacing:2")).getTranslatedRemainder());
		assertEquals(
			"f⠀o⠀o⠀b⠀a⠀r⠀⠀⠀q⠀u⠀u⠀x⠀⠀⠀#abcdef",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:1; word-spacing:3")).getTranslatedRemainder());
		assertEquals(
			"f⠀⠀o⠀⠀o⠀⠀b⠀⠀a⠀⠀r⠀⠀⠀⠀q⠀⠀u⠀⠀u⠀⠀x⠀⠀⠀⠀#abcdef",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:2; word-spacing:4")).getTranslatedRemainder());
		assertEquals(
			"f⠀⠀o⠀⠀o⠀⠀b⠀⠀a⠀⠀r⠀⠀⠀⠀⠀q⠀⠀u⠀⠀u⠀⠀x⠀⠀⠀⠀⠀#abcdef",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:2; word-spacing:5")).getTranslatedRemainder());
	}

	@Test
	public void testTranslateWithWordSpacingAndLineBreaking() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.cti')(output:ascii)")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			//                   |<- 20
			"foobar⠀⠀foobar\n" +
			"foobar",
			fillLines(translator.transform(styledText("foobar foobar foobar", "word-spacing:2")), 20));
		assertEquals(
			//                   |<- 20
			"f⠀o⠀o⠀b⠀a⠀r\n" +
			"f⠀o⠀o⠀b⠀a⠀r\n" +
			"f⠀o⠀o⠀b⠀a⠀r",
			fillLines(translator.transform(styledText("foobar foobar foobar", "letter-spacing:1; word-spacing:3")), 20));
		assertEquals(
			//                        |<- 25
			"f⠀o⠀o⠀-⠀b⠀a⠀r⠀⠀⠀f⠀o⠀o⠀-\n" +
			"b⠀a⠀r⠀⠀⠀f⠀o⠀o⠀-⠀b⠀a⠀r",
			fillLines(translator.transform(styledText("foo-​bar foo-​bar foo-​bar", "letter-spacing:1; word-spacing:3")), 25)); // words are split up using hyphen + zwsp
		assertEquals(
			//                   |<- 20
			"f⠀o⠀o⠀b⠀a⠀r⠀⠀⠀f⠀o⠀o⠤\n" +
			"b⠀a⠀r⠀⠀⠀f⠀o⠀o⠀b⠀a⠀r",
			fillLines(translator.transform(styledText("foo­bar foo­bar foo­bar", "letter-spacing:1; word-spacing:3")), 20)); // words are split up using shy
	}

	@Test
	public void testTranslateWithPreservedLineBreaks() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.cti')(output:ascii)")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			//                   |<- 20
			"foobar\n" +
			"quux",
			fillLines(translator.transform(styledText("foobar quux", "word-spacing:2")), 20)); // words are split up using U+2028
		assertEquals(
			//                   |<- 20
			"norf\n" +
			"quux\n" +
			"foobar\n" +
			"xyzzy",
			fillLines(translator.transform(styledText("norf quux foobar xyzzy", "word-spacing:2")), 20)); // words are split up using U+2028
	}
	
	@Test
	public void testDisplayTableProvider() {
		Iterable<TableProvider> tableProviders = getServices(TableProvider.class);
		Provider<Query,Table> tableProvider = dispatch(tableProviders);
		
		// (liblouis-table: ...)
		Table table = tableProvider.get(query("(liblouis-table:'foobar.dis')")).iterator().next();
		BrailleConverter converter = table.newBrailleConverter();
		assertEquals("⠋⠕⠕⠀⠃⠁⠗", converter.toBraille("foo bar"));
		assertEquals("foo bar", converter.toText("⠋⠕⠕⠀⠃⠁⠗"));
		
		//  (locale: ...)
		table = tableProvider.get(query("(locale:foo)")).iterator().next();
		converter = table.newBrailleConverter();
		assertEquals("⠋⠕⠕⠀⠃⠁⠗", converter.toBraille("foo bar"));
		assertEquals("foo bar", converter.toText("⠋⠕⠕⠀⠃⠁⠗"));
		
		// (id: ...)
		String id = table.getIdentifier();
		assertEquals(table, tableProvider.get(query("(id:'" + id + "')")).iterator().next());
		// assertEquals(table, tableCatalog.newTable(id));
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
	
	private Iterable<CSSStyledText> text(String... text) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		for (String t : text)
			styledText.add(new CSSStyledText(t, ""));
		return styledText;
	}
	
	private Iterable<String> braille(String... text) {
		return Arrays.asList(text);
	}
	
	private static String fillLines(LineIterator lines, int width) {
		StringBuilder sb = new StringBuilder();
		while (lines.hasNext()) {
			sb.append(lines.nextTranslatedRow(width, true));
			if (lines.hasNext())
				sb.append('\n'); }
		return sb.toString();
	}
	
	@Inject
	private BundleContext context;
	
	private <S> Iterable<S> getServices(Class<S> serviceClass) {
		List<S> services = new ArrayList<S>();
		try {
			for (ServiceReference<? extends S> ref : context.getServiceReferences(serviceClass, null))
				services.add(context.getService(ref)); }
		catch (InvalidSyntaxException e) {
			throw new RuntimeException(e); }
		return services;
	}
	
	private <S> void registerService(S service, Class<S> serviceClass) {
		Hashtable<String,Object> properties = new Hashtable<String,Object>();
		context.registerService(serviceClass.getName(), service, properties);
	}
}
