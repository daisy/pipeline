import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import static com.google.common.collect.Iterables.size;

import cz.vutbr.web.css.CSSProperty;

import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TransformProvider;
import org.daisy.pipeline.braille.css.CSSStyledText;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;

/**
 * BrailleTranslator that can translate numbers.
 *
 * Requires that input text is a string consisting of only digits or
 * roman numbers (for generating page numbers), braille pattern
 * characters (U+28xx), white space characters (SPACE, NBSP, BRAILLE
 * PATTERN BLANK) and pre-hyphenation characters (SHY and ZWSP).
 */
public class NumberBrailleTranslator extends AbstractBrailleTranslator {
	
	@Override
	public FromStyledTextToBraille fromStyledTextToBraille() {
		return fromStyledTextToBraille;
	}
	
	private final FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
		public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
			int size = size(styledText);
			if (to < 0) to = size;
			String[] braille = new String[to - from];
			int i = 0;
			for (CSSStyledText t : styledText) {
				if (i >= from && i < to)
					braille[i - from] = NumberBrailleTranslator.this.transform(t);
				i++; }
			return Arrays.asList(braille);
		}
		@Override
		public String toString() {
			return NumberBrailleTranslator.this + ".fromStyledTextToBraille()";
		}
	};

	private final static char SHY = '\u00ad';
	private final static char ZWSP = '\u200b';
	private final static char SPACE = ' ';
	private final static char CR = '\r';
	private final static char LF = '\n';
	private final static char TAB = '\t';
	private final static char NBSP = '\u00a0';
	private final static char LS = '\u2028';   // line separator
	private final static Pattern VALID_INPUT = Pattern.compile("[ivxlcdm0-9\\.\u2800-\u28ff" + SHY + ZWSP + SPACE + LF + CR + TAB + NBSP + "]*");
	private final static Pattern NUMBER = Pattern.compile("(?<decimal>[0-9]+\\.[0-9]+)|(?<natural>[0-9]+)|(?<roman>[ivxlcdm]+)");
	private final static String NUMSIGN = "\u283c";
	private final static String DECIMAL_POINT = "\u2832";
	private final static String[] DIGIT_TABLE = new String[]{
		"\u281a","\u2801","\u2803","\u2809","\u2819","\u2811","\u280b","\u281b","\u2813","\u280a"};
	private final static String[] DOWNSHIFTED_DIGIT_TABLE = new String[]{
		"\u2834","\u2802","\u2806","\u2812","\u2832","\u2822","\u2816","\u2836","\u2826","\u2814"};
	
	private String transform(CSSStyledText styledText) {
		Map<String,String> attrs = styledText.getTextAttributes();
		if (attrs != null && !attrs.isEmpty())
			throw new RuntimeException("Translator does not support text attributes '" + attrs + "'");
		return transform(styledText.getText(), styledText.getStyle());
	}
	
	private String transform(String text, SimpleInlineStyle style) {
		if (!VALID_INPUT.matcher(text).matches())
			throw new RuntimeException("Invalid input: \"" + text + "\"");
		boolean noTransform = false;
		boolean downShift = false;
		if (style != null) {
			CSSProperty ws = style.getProperty("white-space");
			if (ws != null) {
				if (ws == WhiteSpace.PRE_WRAP)
					text = text.replaceAll("[\\x20\t\\u2800]+", "$0\u200B")
					           .replaceAll("[\\x20\t\\u2800]", "\u00A0");
				if (ws == WhiteSpace.PRE_WRAP || ws == WhiteSpace.PRE_LINE)
					text = text.replaceAll("[\\n\\r]", ""+LS);
				style.removeProperty("white-space"); }
			CSSProperty textTransform = style.getProperty("text-transform");
			if (textTransform == TextTransform.AUTO)
				style.removeProperty("text-transform");
			else if (textTransform == TextTransform.NONE) {
				noTransform = true;
				style.removeProperty("text-transform");
			} else if (textTransform == TextTransform.list_values
			           && style.getValue("text-transform").toString().equals("downshift")) {
				downShift = true;
				style.removeProperty("text-transform");
			}
			if (!style.isEmpty())
				throw new RuntimeException("Translator does not support style '" + style + "'");
		}
		if (noTransform)
			return text;
		else
			return translateNumbers(text, downShift);
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
			else if (m.group("decimal") != null)
				sb.append(translateDecimalNumber(Integer.parseInt(number.split("\\.")[0]),
				                                 Integer.parseInt(number.split("\\.")[1]),
				                                 downshift));
			else
				sb.append(translateNaturalNumber(Integer.parseInt(number), !downshift, downshift)); }
		if (idx == 0)
			return text;
		sb.append(text.substring(idx));
		return sb.toString();
	}
	
	private static String translateNaturalNumber(int number, boolean numsign, boolean downshift) {
		StringBuilder sb = new StringBuilder();
		String[] table = downshift ? DOWNSHIFTED_DIGIT_TABLE : DIGIT_TABLE;
		if (number == 0)
			sb.append(table[0]);
		while (number > 0) {
			sb.insert(0, table[number % 10]);
			number = number / 10; }
		if (numsign)
			sb.insert(0, NUMSIGN);
		return sb.toString();
	}
	
	private static String translateDecimalNumber(int whole, int decimal, boolean downshift) {
		StringBuilder sb = new StringBuilder();
		sb.append(translateNaturalNumber(whole, true, downshift));
		sb.append(DECIMAL_POINT);
		sb.append(translateNaturalNumber(decimal, false, downshift));
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
	
	@Override
	public String toString() {
		return "NumberBrailleTranslator";
	}
	
	@Component(
		name = "number-braille-translator-provider",
		service = {
			BrailleTranslatorProvider.class,
			TransformProvider.class
		}
	)
	public static class Provider implements BrailleTranslatorProvider<NumberBrailleTranslator> {
		final static NumberBrailleTranslator instance = new NumberBrailleTranslator();
		public Iterable<NumberBrailleTranslator> get(Query query) {
			MutableQuery q = mutableQuery(query);
			q.removeAll("document-locale");
			try {
				if ("text-css".equals(q.removeOnly("input").getValue().orElse(null))
				    && "braille".equals(q.removeOnly("output").getValue().orElse(null))
				    && q.removeOnly("number-translator") != null
				    && q.isEmpty()
				) {
					return Optional.<NumberBrailleTranslator>fromNullable(instance).asSet();
				}
			} catch (Exception e) {}
			return Optional.<NumberBrailleTranslator>fromNullable(null).asSet();
		}
		public TransformProvider<NumberBrailleTranslator> withContext(Logger context) {
			return this;
		}
	}
}
