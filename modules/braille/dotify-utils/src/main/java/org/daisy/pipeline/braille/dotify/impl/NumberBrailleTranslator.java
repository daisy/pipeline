package org.daisy.pipeline.braille.dotify.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Iterables.size;

import cz.vutbr.web.css.CSSProperty;

import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.CSSStyledText;

/**
 * BrailleTranslator that can translate numbers.
 *
 * Requires that input text is a string consisting of only digits (for
 * generating page numbers), braille pattern characters (U+28xx), white
 * space characters (SPACE, NBSP, BRAILLE PATTERN BLANK) and
 * pre-hyphenation characters (SHY and ZWSP).
 */
class NumberBrailleTranslator extends AbstractBrailleTranslator implements BrailleTranslator {

	private static NumberBrailleTranslator INSTANCE = null;
	
	static NumberBrailleTranslator getInstance() {
		if (INSTANCE == null)
			INSTANCE = new NumberBrailleTranslator();
		return INSTANCE;
	}
	
	private final static char SHY = '\u00ad';
	private final static char ZWSP = '\u200b';
	private final static char SPACE = ' ';
	private final static char CR = '\r';
	private final static char LF = '\n';
	private final static char TAB = '\t';
	private final static char NBSP = '\u00a0';

	private final static Pattern VALID_INPUT = Pattern.compile("[0-9\u2800-\u28ff" + SHY + ZWSP + SPACE + LF + CR + TAB + NBSP + "]*");
	private final static Pattern NUMBER = Pattern.compile("[0-9]+");
	private final static String NUMSIGN = "\u283c";
	private final static String[] DIGIT_TABLE = new String[]{
		"\u281a","\u2801","\u2803","\u2809","\u2819","\u2811","\u280b","\u281b","\u2813","\u280a"};

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

	@Override
	public LineBreakingFromStyledText lineBreakingFromStyledText() {
		return lineBreakingFromStyledText;
	}

	private final LineBreakingFromStyledText lineBreakingFromStyledText = new LineBreakingFromStyledText() {
		public LineIterator transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
			List<String> braille = new ArrayList<>();
			for (CSSStyledText t : styledText)
				braille.add(NumberBrailleTranslator.this.transform(t));
			StringBuilder brailleString = new StringBuilder();
			int fromChar = 0;
			int toChar = to >= 0 ? 0 : -1;
			for (String s : braille) {
				brailleString.append(s);
				if (--from == 0)
					fromChar = brailleString.length();
				if (--to == 0)
					toChar = brailleString.length();
			}
			return new DefaultLineBreaker.LineIterator(brailleString.toString(), fromChar, toChar, '\u2800', '\u2824', 1);
		}
		@Override
		public String toString() {
			return NumberBrailleTranslator.this + ".lineBreakingFromStyledText()";
		}
	};

	private String transform(CSSStyledText styledText) {
		SimpleInlineStyle style = styledText.getStyle();
		String text = styledText.getText();
		if (style != null) {
			CSSProperty ws = style.getProperty("white-space");
			if (ws != null) {
				if (ws == WhiteSpace.PRE_WRAP)
					text = text.replaceAll("[\\x20\t\\u2800]+", "$0\u200B")
					           .replaceAll("[\\x20\t\\u2800]", "\u00A0");
				if (ws == WhiteSpace.PRE_WRAP || ws == WhiteSpace.PRE_LINE)
					text = text.replaceAll("[\\n\\r]", "\u2028");
				style.removeProperty("white-space"); }
			CSSProperty textTransform = style.getProperty("text-transform");
			if (textTransform == TextTransform.AUTO)
				style.removeProperty("text-transform");
			if (!style.isEmpty())
				throw new RuntimeException("Translator does not support style '" + style + "'");
		}
		Map<String,String> attrs = styledText.getTextAttributes();
		if (attrs != null && !attrs.isEmpty())
			throw new RuntimeException("Translator does not support text attributes '" + attrs + "'");
		return transform(text);
	}

	private String transform(String text) {
		
		// The input text must consist of only digits, braille pattern characters and
		// pre-hyphenation characters.
		if (!VALID_INPUT.matcher(text).matches())
			throw new RuntimeException("Invalid input: \"" + text + "\"");
		return translateNumbers(text);
	}

	private static String translateNumbers(String text) {
		Matcher m = NUMBER.matcher(text);
		int idx = 0;
		StringBuilder sb = new StringBuilder();
		for (; m.find(); idx = m.end()) {
			sb.append(text.substring(idx, m.start()));
			sb.append(translateNaturalNumber(Integer.parseInt(m.group()))); }
		if (idx == 0)
			return text;
		sb.append(text.substring(idx));
		return sb.toString();
	}

	private static String translateNaturalNumber(int number) {
		StringBuilder sb = new StringBuilder();
		sb.append(NUMSIGN);
		if (number == 0)
			sb.append(DIGIT_TABLE[0]);
		while (number > 0) {
			sb.insert(1, DIGIT_TABLE[number % 10]);
			number = number / 10; }
		return sb.toString();
	}

	@Override
	public String toString() {
		return "o.d.p.b.dotify.impl.NumberBrailleTranslator";
	}
}
