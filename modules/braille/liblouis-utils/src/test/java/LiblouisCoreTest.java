import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableCatalogService;

import static org.daisy.common.file.URLs.asURI;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslator.FromStyledTextToBraille;
import org.daisy.pipeline.braille.common.BrailleTranslator.LineBreakingFromStyledText;
import org.daisy.pipeline.braille.common.BrailleTranslator.LineIterator;
import org.daisy.pipeline.braille.common.CompoundBrailleTranslator;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.Provider;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.braille.liblouis.LiblouisHyphenator;
import org.daisy.pipeline.braille.liblouis.LiblouisTable;
import org.daisy.pipeline.braille.liblouis.LiblouisTableResolver;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;
import org.daisy.pipeline.braille.pef.TableRegistry;

import org.daisy.pipeline.junit.AbstractTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.ComparisonFailure;
import org.junit.Test;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.util.PathUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiblouisCoreTest extends AbstractTest {
	
	@Inject
	public LiblouisTranslator.Provider provider;
	
	@Inject
	public LiblouisHyphenator.Provider hyphenatorProvider;
	
	@Inject
	public LiblouisTableResolver resolver;
	
	@Inject
	public TableCatalogService tableCatalog;
	
	private static final Logger messageBus = LoggerFactory.getLogger("JOB_MESSAGES");
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("braille-common"),
			brailleModule("braille-css-utils"),
			brailleModule("pef-utils"),
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("common-utils"),
			"org.liblouis:liblouis-java:?",
			"org.daisy.dotify:dotify.library:?",
			"org.daisy.pipeline:calabash-adapter:?",
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Bundle-Name", "test-module");
		// needed because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/mock-hyphenator-provider.xml,"
		                                   + "OSGI-INF/table-path.xml");
		return probe;
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			thisBundle(thisPlatform()),
			composite(super.config()));
	}
	
	@Test
	public void testResolveTableFile() {
		assertEquals("foobar.uti", asFile(resolver.resolve(asURI("foobar.uti"))).getName());
	}
	
	@Test
	public void testResolveTable() {
		assertEquals("foobar.uti", (resolver.resolveLiblouisTable(new LiblouisTable("foobar.uti"), null)[0]).getName());
	}
	
	@Test
	public void testGetTranslatorFromQuery1() {
		provider.withContext(messageBus).get(query("(locale:foo)")).iterator().next();
	}
	
	@Test
	public void testGetTranslatorFromQuery2() {
		provider.withContext(messageBus).get(query("(table:'foobar.uti')")).iterator().next();
	}
	
	@Test
	public void testGetTranslatorFromQuery3() {
		provider.withContext(messageBus).get(query("(document-locale:foo_BA)")).iterator().next();
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testGetTranslatorFromQuery4() {
		provider.withContext(messageBus).get(query("(locale:bar)")).iterator().next();
	}
	
	@Test
	public void testTranslate() {
		assertEquals(braille("⠋⠕⠕⠃⠁⠗"),
		             provider.withContext(messageBus)
		                     .get(query("(table:'foobar.uti')")).iterator().next()
		                     .fromStyledTextToBraille().transform(text("foobar")));
	}
	
	@Test
	public void testTranslateStyled() {
		assertArrayEquals(new String[]{"⠨⠋⠕⠕⠃⠁⠗"},
		                  provider.withContext(messageBus)
		                          .get(query("(table:'foobar.uti,ital.cti')")).iterator().next()
		                          .fromTypeformedTextToBraille().transform(new String[]{"foobar"}, new String[]{"italic"}));
	}
	
	@Test
	public void testTextTransformUncontracted() {
		FromStyledTextToBraille translator = new CompoundBrailleTranslator(
			provider.withContext(messageBus)
			        .get(query("(locale:foo)(contraction:full)(charset:'foobar.dis')")).iterator().next(),
			ImmutableMap.of(
				"uncontracted",
				() -> provider.withContext(messageBus)
				              .get(query("(locale:foo)(contraction:no)(charset:'foobar.dis')")).iterator().next()
			)
		).fromStyledTextToBraille();
		assertEquals(braille("fu ", "foo", " fu"),
		             translator.transform(styledText("foo ", "",
		                                             "foo",  "text-transform:uncontracted",
		                                             " foo", "")));
	}
	
	@Test
	public void testTextTransformNone() {
		assertEquals(braille("foo", "bar"),
		             provider.withContext(messageBus)
		                     .get(query("(table:'foobar.uti')(charset:'foobar.dis')")).iterator().next()
		                     .fromStyledTextToBraille().transform(
		                         styledText("foo", "",
		                                    "⠃⠁⠗", "text-transform: none")));
	}
	
	@Test
	public void testCompoundTranslator() {
		LineBreakingFromStyledText translator = new CompoundBrailleTranslator(
			provider.withContext(messageBus)
			        .get(query("(locale:foo)(contraction:full)(charset:'foobar.dis')")).iterator().next(),
			ImmutableMap.of(
				"uncontracted",
				() -> provider.withContext(messageBus)
				              .get(query("(locale:foo)(contraction:no)(charset:'foobar.dis')")).iterator().next()
			)
		).lineBreakingFromStyledText();
		assertEquals(
			"xxxxxxx\n" +
			"abc def \n" +
			"ghi",
			fillLines(
				translator.transform(
					styledText(
						"xxxxxxx abc def ", "",
						"ghi", "text-transform: uncontracted")),
				10));
		assertEquals(
			"xxxxxxx\n" +
			"abc\n" +
			"defghij",
			fillLines(
				translator.transform(
					styledText(
						"xxxxxxx abc def", "",
						"ghij", "text-transform: uncontracted")),
				10));
	}
	
	@Test
	public void testTranslateSegments() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.uti')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠋⠕⠕","⠃⠁⠗"),
		             translator.transform(text("foo","bar")));
		assertEquals(braille("⠋⠕⠕","","⠃⠁⠗"),
		             translator.transform(text("foo","","bar")));
	}
	
	@Test
	public void testTranslateStyledSegments() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.uti,ital.cti')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠋⠕⠕ ", "⠨⠃⠁⠗", " ⠃⠁⠵"),
		             translator.transform(styledText("foo ", "",
		                                             "bar",  "text-transform:-louis-ital",
		                                             // this doesn't work anymore because the print properties
		                                             // text-decoration, font-weight and color are not supported by
		                                             // org.daisy.braille.css.SimpleInlineStyle
		                                             " baz", "font-style: italic;"
		                                  )));
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
	public void testUnicodeNormalization() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(locale:foo)(contraction:no)")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠷"), translator.transform(text("á")));
		assertEquals(braille("⠷"), translator.transform(text("\u0061\u0301")));
	}
	
	@Test
	public void testUCS4() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.utb')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠛⠗⠊⠝"), translator.transform(text("\uD83D\uDE00"))); // U+1F600 grinning face
		assertEquals(braille("⠛⠗⠊⠝\u00AD⠛⠗⠊⠝"), translator.transform(text("\uD83D\uDE00\u00AD\uD83D\uDE00")));
	}
	
	@Test
	public void testUndefinedChar() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(locale:foo)(contraction:full)(dots-for-undefined-char:'⣀')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⣀"), translator.transform(text("€")));
	}
	
	@Test
	public void testMaskVirtualDots() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.utb')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠁⠃⠉ ⠼⠁⠃⠉"), translator.transform(text("abc 123")));
	}
	
	@Test
	public void testHyphenate() {
		assertEquals(text("foo\u00ADbar"),
		             hyphenatorProvider.withContext(messageBus)
		                 .get(query("(table:'foobar.uti,foobar.dic')")).iterator().next()
		                 .asFullHyphenator()
		                 .transform(styledText("foobar", "hyphens: auto")));
	}
	
	@Test
	public void testHyphenateCompoundWord() {
		assertEquals(text("foo-\u200Bbar"),
		             hyphenatorProvider.withContext(messageBus)
		                 .get(query("(table:'foobar.uti,foobar.dic')")).iterator().next()
		                 .asFullHyphenator()
		                 .transform(styledText("foo-bar", "hyphens: auto")));
	}
	
	@Test
	public void testManualWordBreak() {
		assertEquals(text("foo\u00ADbar foo\u00ADbar foob\u00ADar"),
		             hyphenatorProvider.withContext(messageBus)
		                 .get(query("(table:'foobar.uti,foobar.dic')")).iterator().next()
		                 .asFullHyphenator()
		                 .transform(styledText("foobar foo\u00ADbar foob\u00ADar", "hyphens: auto")));
		assertEquals(braille("⠋⠕⠕\u00AD⠃⠁⠗ ⠋⠕⠕\u00AD⠃⠁⠗ ⠋⠕⠕⠃\u00AD⠁⠗"),
		             provider.withContext(messageBus)
		                     .get(query("(table:'foobar.uti,foobar.dic')")).iterator().next()
		                     .fromStyledTextToBraille()
		                     .transform(styledText("foobar foo\u00ADbar foob\u00ADar", "hyphens:auto")));
		assertEquals(
			"⠋⠕⠕⠤\n" +
			"⠃⠁⠗\n" +
			"⠋⠕⠕⠤\n" +
			"⠃⠁⠗\n" +
			"⠋⠕⠕⠃⠤\n" +
			"⠁⠗",
			fillLines(
				provider.withContext(messageBus)
		                .get(query("(table:'foobar.uti,foobar.dic')")).iterator().next()
				        .lineBreakingFromStyledText()
				        .transform(styledText("foobar foo\u00ADbar foob\u00ADar", "hyphens:auto")),
				5));
	}
	
	@Test
	public void testHyphenateCharacter() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.ctb')(charset:'foobar.dis')")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			"abc-\n" +
			"def \n" +
			"abc'\n" +
			"def",
			fillLines(translator.transform(styledText("abc\u00ADdef ", "",
			                                          "abc\u00ADdef", "hyphenate-character: '⠈'")), 4));
	}
	
	@Test
	public void testTranslateAndHyphenateCompoundWord() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.utb,foobar.dic')(hyphenator:auto)")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(
			braille("⠋⠕⠕⠤\u200B⠃⠁⠗"),
			translator.transform(styledText("foo-bar", "hyphens:auto")));
		// break opportunity expected after '-' regardless of value of hyphens
		assertEquals(
			braille("⠋⠕⠕⠤\u200B⠃⠁⠗"),
			translator.transform(styledText("foo-bar", "hyphens:manual")));
		assertEquals(
			braille("⠋⠕⠕⠤\u200B⠃⠁⠗"),
			translator.transform(styledText("foo-bar", "hyphens:none")));
		// and regardless of value of hyphenator feature
		translator = provider.withContext(messageBus)
		                     .get(query("(table:'foobar.utb,foobar.dic')(hyphenator:none)")).iterator().next()
		                    .fromStyledTextToBraille();
		assertEquals(
			braille("⠋⠕⠕⠤\u200B⠃⠁⠗"),
			translator.transform(styledText("foo-bar", "hyphens:auto")));
		assertEquals(
			braille("⠋⠕⠕⠤\u200B⠃⠁⠗"),
			translator.transform(styledText("foo-bar", "hyphens:manual")));
		assertEquals(
			braille("⠋⠕⠕⠤\u200B⠃⠁⠗"),
			translator.transform(styledText("foo-bar", "hyphens:none")));
	}
	
	@Test
	public void testTranslateAndHyphenateNonStandard() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.ctb')(hyphenator:mock)(charset:'foobar.dis')")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			"fu\n" +
			"bar\n" +
			"z",
			fillLines(translator.transform(styledText("foobarz", "hyphens:auto")), 3));
		assertEquals(
			"fub-\n" +
			"barz",
			fillLines(translator.transform(styledText("foobarz", "hyphens:auto")), 4));
		assertEquals(
			"fub-\n" +
			"barz",
			fillLines(translator.transform(styledText("foobarz", "hyphens:auto")), 5));
		assertEquals(
			"fubarz",
			fillLines(translator.transform(styledText("foobarz", "hyphens:auto")), 6));
		assertEquals(
			"fuba\n" +
			"rz\n" +
			"fub-\n" +
			"barz",
			fillLines(translator.transform(styledText("foobarz ", "",
			                                          "foobarz", "hyphens:auto")), 4));
	}
	
	@Test
	public void testTranslateAndHyphenateSomeSegments() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.uti,foobar.dic')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(braille("⠋⠕⠕\u00AD⠃⠁⠗ ","⠋⠕⠕⠃⠁⠗"),
		             translator.transform(styledText("foobar ", "hyphens:auto",
		                                             "foobar",  "hyphens:none")));
	}
	
	@Test
	public void testWhiteSpaceProcessing() {
		BrailleTranslator translator = provider.withContext(messageBus)
		                                       .get(query("(table:'foobar.uti')")).iterator().next();
		assertEquals(braille("⠋⠕⠕    ⠃⠁⠗ ⠃⠁⠵"),
		             translator.fromStyledTextToBraille()
		                       .transform(text("foo    bar\nbaz")));
		assertEquals(braille("⠋⠕⠕    ⠃⠁⠗\n⠃⠁⠵"),
		             translator.fromStyledTextToBraille()
		                       .transform(styledText("foo    bar\nbaz", "white-space:pre-wrap")));
		assertEquals(braille("",
		                     "⠋⠕⠕    ⠃⠁⠗\n\u00AD",
		                     "",
		                     "⠃⠁⠵"),
		             translator.fromStyledTextToBraille()
		                       .transform(styledText("",             "",
		                                             "foo    bar\n", "white-space:pre-wrap",
		                                             "\u00AD",       "",
		                                             "baz",          "")));
		assertEquals(braille("\n"),
		             translator.fromStyledTextToBraille()
		                       .transform(styledText("\n", "white-space:pre-line")));
		// test no-break space
		assertEquals(
			"⠁⠃⠉\n" +
			"⠙⠑⠋ ⠛⠓⠊⠚",
			fillLines(
				translator.lineBreakingFromStyledText()
				          .transform(styledText("abc def ghij", "")),
				10));
	}
	
	@Test
	public void testWhiteSpaceLost() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.uti,delete-ws.utb')")).iterator().next()
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
		                                             .get(query("(table:'foobar.uti,squash-ws.utb')(charset:'foobar.dis')")).iterator().next()
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
		                                             .get(query("(table:'foobar.uti')(charset:'foobar.dis')")).iterator().next()
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
		                                             .get(query("(table:'foobar.uti,foobar.dic')(charset:'foobar.dis')")).iterator().next()
		                                             .fromStyledTextToBraille();
		assertEquals(
			braille("f o o\u00AD b a r"),
			translator.transform(styledText("foobar", "letter-spacing:1; hyphens:auto")));
	}
	
	@Test
	public void testTranslateWithLetterSpacingAndContractions() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'foobar.ctb')(charset:'foobar.dis')")).iterator().next()
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
		                                             .get(query("(table:'foobar.ctb')(charset:'foobar.dis')")).iterator().next()
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
		                                                .get(query("(table:'foobar.uti')(charset:'foobar.dis')")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			"foo  bar",
			translator.transform(styledText("foo bar", "word-spacing:2")).getTranslatedRemainder());
		assertEquals(
			"foo   bar",
			translator.transform(styledText("foo bar", "word-spacing:3")).getTranslatedRemainder());
	}

	@Test
	public void testTranslateWithWhiteSpaceProcessingAndWordSpacing() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.uti')(charset:'foobar.dis')")).iterator().next()
		                                                .lineBreakingFromStyledText();
		// space in input, two spaces in output
		assertEquals(
			"foo  bar",
			translator.transform(styledText("foo bar", "word-spacing:2")).getTranslatedRemainder());
		// two spaces in input, two spaces in output
		assertEquals(
			"foo  bar",
			translator.transform(styledText("foo  bar", "word-spacing:2")).getTranslatedRemainder());
		// newline + tab in input, two spaces in output
		assertEquals(
			"foo  bar",
			translator.transform(styledText("foo\n	bar", "word-spacing:2")).getTranslatedRemainder());
		// no-break space in input, space in output
		assertEquals(
			"foo bar",
			translator.transform(styledText("foo bar", "word-spacing:2")).getTranslatedRemainder());
		// no-break space + space in input, three spaces in output
		assertEquals(
			"foo   bar",
			translator.transform(styledText("foo  bar", "word-spacing:2")).getTranslatedRemainder());
		// zero-width space in input, no space in output
		assertEquals(
			"foobar",
			translator.transform(styledText("foo​bar", "word-spacing:2")).getTranslatedRemainder());
	}
	
	@Test
	public void testTranslateWithLetterSpacing() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.uti')(charset:'foobar.dis')")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			"f o o b a r   q u u x   #123456",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:1; word-spacing:3")).getTranslatedRemainder());
		assertEquals(
			"f  o  o  b  a  r     q  u  u  x     #123456",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:2; word-spacing:5")).getTranslatedRemainder());
	}

	@Test
	public void testTranslateWithLetterSpacingAndWordSpacing() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.uti')(charset:'foobar.dis')")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			"f o o b a r  q u u x  #123456",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:1; word-spacing:2")).getTranslatedRemainder());
		assertEquals(
			"f o o b a r   q u u x   #123456",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:1; word-spacing:3")).getTranslatedRemainder());
		assertEquals(
			"f  o  o  b  a  r    q  u  u  x    #123456",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:2; word-spacing:4")).getTranslatedRemainder());
		assertEquals(
			"f  o  o  b  a  r     q  u  u  x     #123456",
			translator.transform(styledText("foobar quux 123456", "letter-spacing:2; word-spacing:5")).getTranslatedRemainder());
	}

	@Test
	public void testTranslateWithWordSpacingAndLineBreaking() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.uti')(charset:'foobar.dis')")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			//                   |<- 20
			"foobar  foobar\n" +
			"foobar",
			fillLines(translator.transform(styledText("foobar foobar foobar", "word-spacing:2")), 20));
		assertEquals(
			//                   |<- 20
			"f o o b a r\n" +
			"f o o b a r\n" +
			"f o o b a r",
			fillLines(translator.transform(styledText("foobar foobar foobar", "letter-spacing:1; word-spacing:3")), 20));
		assertEquals(
			//                        |<- 25
			"f o o - b a r   f o o -\n" +
			"b a r   f o o - b a r",
			fillLines(translator.transform(styledText("foo-​bar foo-​bar foo-​bar", "letter-spacing:1; word-spacing:3")), 25)); // words are split up using hyphen + zwsp
		assertEquals(
			//                   |<- 20
			"f o o b a r   f o o-\n" +
			"b a r   f o o b a r",
			fillLines(translator.transform(styledText("foo­bar foo­bar foo­bar", "letter-spacing:1; word-spacing:3")), 20)); // words are split up using shy
	}

	@Test
	public void testTranslateWithPreservedLineBreaks() {
		LineBreakingFromStyledText translator = provider.withContext(messageBus)
		                                                .get(query("(table:'foobar.uti')(charset:'foobar.dis')")).iterator().next()
		                                                .lineBreakingFromStyledText();
		assertEquals(
			//                   |<- 20
			"foobar\n" +
			"quux",
			fillLines(translator.transform(text("foobar\u2028quux")), 20));
		assertEquals(
			//                   |<- 20
			"foobar\n" +
			"quux",
			fillLines(translator.transform(styledText("foobar\nquux", "white-space:pre-line")), 20));
		assertEquals(
			//                   |<- 20
			"foobar\n" +
			"quux",
			fillLines(translator.transform(styledText("foobar\u2028quux", "word-spacing:2")), 20));
		assertEquals(
			//                   |<- 20
			"norf\n" +
			"quux\n" +
			"foobar\n" +
			"xyzzy",
			fillLines(translator.transform(styledText("norf\u2028quux\u2028foobar\u2028xyzzy", "word-spacing:2")), 20));
	}
	
	@Inject
	public TableRegistry tableProvider;
	
	@Test
	public void testDisplayTableProvider() {
		
		// (liblouis-table: ...)
		Table table = tableProvider.get(query("(liblouis-table:'foobar.dis')")).iterator().next();
		BrailleConverter converter = table.newBrailleConverter();
		assertEquals("⠋⠕⠕⠀⠃⠁⠗", converter.toBraille("foo bar"));
		assertEquals("foo bar", converter.toText("⠋⠕⠕⠀⠃⠁⠗"));
		// virtual dots
		assertEquals("⠁⠃⠉⠀⠁⠃⠉", converter.toBraille("abc 123"));
		
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
	
	@Test//(expected=ComparisonFailure.class)
	public void testTranslatorStatefulBug() {
		FromStyledTextToBraille translator = provider.withContext(messageBus)
		                                             .get(query("(table:'stateful.utb')")).iterator().next()
		                                             .fromStyledTextToBraille();
		String p = translator.transform(text("p")).iterator().next();
		//assertEquals("⠰⠏", p);
		translator.transform(text("xx"));
		// this is expected to fail:
		assertEquals(p, translator.transform(text("p")).iterator().next());
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
	
	private static UrlProvisionOption thisBundle(String classifier) {
		File classes = new File(PathUtils.getBaseDir() + "/target/classes");
		Properties dependencies = new Properties(); {
			try {
				dependencies.load(new FileInputStream(new File(classes, "META-INF/maven/dependencies.properties"))); }
			catch (IOException e) {
				throw new RuntimeException(e); }
		}
		String artifactId = dependencies.getProperty("artifactId");
		String version = dependencies.getProperty("version");
		// assuming JAR is named ${artifactId}-${version}.jar
		return bundle("reference:" +
		              new File(PathUtils.getBaseDir() + "/target/" + artifactId + "-" + version + "-" + classifier + ".jar").toURI());
	}
}
