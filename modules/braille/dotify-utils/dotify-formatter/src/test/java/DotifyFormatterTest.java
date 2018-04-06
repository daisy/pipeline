import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Iterables.size;

import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.TransformProvider;

import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.ops4j.pax.exam.util.PathUtils;

import org.osgi.framework.BundleContext;

import org.slf4j.Logger;

public class DotifyFormatterTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("css-core"),
			brailleModule("css-utils"),
			brailleModule("pef-utils"),
			brailleModule("obfl-utils"),
			brailleModule("common-utils"),
			brailleModule("dotify-utils"),
			brailleModule("liblouis-core"),
			"org.daisy.pipeline.modules.braille:liblouis-native:jar:" + thisPlatform() + ":?",
			// because of bug in lou_indexTables we need to include liblouis-tables even though we're not using it
			brailleModule("liblouis-tables"),
			"com.google.guava:guava:?",
			"org.daisy.dotify:dotify.api:?",
			"org.daisy.dotify:dotify.common:?",
			"org.daisy.dotify:dotify.translator.impl:?",
			"org.daisy.pipeline:calabash-adapter:?",
			// logging
			"org.slf4j:jul-to-slf4j:?",
			"org.daisy.pipeline:logging-activator:?",
			// FIXME: Dotify needs older version of jing
			"org.daisy.libs:jing:20120724.0.0",
		};
	}
	
	@Inject
	private BundleContext context;
	
	@Before
	public void NumberBrailleTranslatorProvider() {
		NumberBrailleTranslator.Provider provider = new NumberBrailleTranslator.Provider();
		Hashtable<String,Object> properties = new Hashtable<String,Object>();
		context.registerService(BrailleTranslatorProvider.class.getName(), provider, properties);
		context.registerService(TransformProvider.class.getName(), provider, properties);
	}
	
	@Override @Test
	public void runXProcSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		boolean success = xprocspecRunner.run(new File(baseDir, "src/test/xprocspec"),
		                                      new File(baseDir, "target/xprocspec-reports"),
		                                      new File(baseDir, "target/surefire-reports"),
		                                      new File(baseDir, "target/xprocspec"),
		                                      new XProcSpecRunner.Reporter.DefaultReporter());
		assertTrue("XProcSpec tests should run with success", success);
	}
	
	private static class NumberBrailleTranslator extends AbstractBrailleTranslator {
		
		@Override
		public FromStyledTextToBraille fromStyledTextToBraille() {
			return fromStyledTextToBraille;
		}
		
		private final FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
			public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText) {
				int size = size(styledText);
				String[] braille = new String[size];
				int i = 0;
				for (CSSStyledText t : styledText)
					braille[i++] = NumberBrailleTranslator.this.transform(t.getText(), t.getStyle());
				return Arrays.asList(braille);
			}
		};
		
		private final static char SHY = '\u00ad';
		private final static char ZWSP = '\u200b';
		private final static char SPACE = ' ';
		private final static char CR = '\r';
		private final static char LF = '\n';
		private final static char TAB = '\t';
		private final static char NBSP = '\u00a0';
		private final static Pattern VALID_INPUT = Pattern.compile("[ivxlcdm0-9\u2800-\u28ff" + SHY + ZWSP + SPACE + LF + CR + TAB + NBSP + "]*");
		private final static Pattern NUMBER = Pattern.compile("(?<natural>[0-9]+)|(?<roman>[ivxlcdm]+)");
		private final static String NUMSIGN = "\u283c";
		private final static String[] DIGIT_TABLE = new String[]{
			"\u281a","\u2801","\u2803","\u2809","\u2819","\u2811","\u280b","\u281b","\u2813","\u280a"};
		private final static String[] DOWNSHIFTED_DIGIT_TABLE = new String[]{
			"\u2834","\u2802","\u2806","\u2812","\u2832","\u2822","\u2816","\u2836","\u2826","\u2814"};
		
		private String transform(String text, SimpleInlineStyle style) {
			if (!VALID_INPUT.matcher(text).matches())
				throw new RuntimeException("Invalid input: \"" + text + "\"");
			if (style != null
			    &&style.getProperty("text-transform") == TextTransform.list_values
			    && style.getValue("text-transform").toString().equals("downshift"))
				return translateNumbers(text, true);
			return translateNumbers(text, false);
		}
		
		private static String translateNumbers(String text, boolean downshift) {
			Matcher m = NUMBER.matcher(text);
			int idx = 0;
			StringBuilder sb = new StringBuilder();
			for (; m.find(); idx = m.end()) {
				sb.append(text.substring(idx, m.start()));
				String number = m.group();
				if (m.group("roman") != null)
					sb.append(translateRomanNumber(number));
				else
					sb.append(translateNaturalNumber(Integer.parseInt(number), downshift)); }
			if (idx == 0)
				return text;
			sb.append(text.substring(idx));
			return sb.toString();
		}
		
		private static String translateNaturalNumber(int number, boolean downshift) {
			StringBuilder sb = new StringBuilder();
			String[] table = downshift ? DOWNSHIFTED_DIGIT_TABLE : DIGIT_TABLE;
			if (number == 0)
				sb.append(table[0]);
			while (number > 0) {
				sb.insert(0, table[number % 10]);
				number = number / 10; }
			if (!downshift)
				sb.insert(0, NUMSIGN);
			return sb.toString();
		}
		
		private static String translateRomanNumber(String number) {
			return number.replace('i', '⠊')
			             .replace('v', '⠧')
			             .replace('x', '⠭')
			             .replace('l', '⠇')
			             .replace('c', '⠉')
			             .replace('d', '⠙')
			             .replace('m', '⠍');
		}
		
		public static class Provider implements BrailleTranslatorProvider<NumberBrailleTranslator> {
			final static NumberBrailleTranslator instance = new NumberBrailleTranslator();
			public Iterable<NumberBrailleTranslator> get(Query query) {
				return Optional.<NumberBrailleTranslator>fromNullable(
					query.toString().equals("(number-translator)(input:text-css)(output:braille)") ? instance : null).asSet();
			}
			public TransformProvider<NumberBrailleTranslator> withContext(Logger context) {
				return this;
			}
		}
	}
}
