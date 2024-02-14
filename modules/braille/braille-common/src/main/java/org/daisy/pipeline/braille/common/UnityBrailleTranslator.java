package org.daisy.pipeline.braille.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermString;

import org.daisy.dotify.api.table.BrailleConverter;

import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.BrailleCSSProperty.BrailleCharset;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.PropertyValue;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator.util.DefaultLineBreaker;
import static org.daisy.pipeline.braille.common.util.Strings.splitInclDelimiter;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.braille.css.TextStyleParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link org.daisy.pipeline.braille.common.BrailleTranslator} that assumes input text exists of
 * only braille and white space characters. Supports CSS properties "word-spacing", "hyphens",
 * "hyphenate-character", "white-space", and "braille-charset".
 */
public class UnityBrailleTranslator extends AbstractBrailleTranslator implements BrailleTranslator {

	private static final Pattern SPECIAL_CHARS = Pattern.compile("[\\x20\t\\n\\r\\u2800\\xA0\u00AD\u200B\u2028]+");

	private final BrailleConverter brailleCharset;
	private final boolean useBrailleCharsetForInput;

	/**
	 * @param brailleCharset            The character set of the output braille, and of the input in
	 *                                  case it is styled as <code>braille-charset: custom</code> or if
	 *                                  <code>useBrailleCharsetForInput</code> is <code>true</code>.
	 *                                  <code>null</code> means Unicode braille.
	 * @param useBrailleCharsetForInput Whether <code>brailleCharset</code> by default also applies to
	 *                                  the input (if it does not have a <code>braille-charset</code>
	 *                                  style).
	 */
	public UnityBrailleTranslator(BrailleConverter brailleCharset, boolean useBrailleCharsetForInput) {
		this.brailleCharset = brailleCharset;
		this.useBrailleCharsetForInput = useBrailleCharsetForInput;
	}

	public UnityBrailleTranslator _withHyphenator(Hyphenator hyphenator) {
		return this;
	}

	private FromStyledTextToBraille fromStyledTextToBraille = null;

	private final static TextStyleParser cssParser = TextStyleParser.getInstance();
	private final static PropertyValue TEXT_TRANSFORM_NONE
		= cssParser.parse("text-transform: none").get("text-transform");
	private final static PropertyValue BRAILLE_CHARSET_CUSTOM
		= cssParser.parse("braille-charset: custom").get("braille-charset");

	public FromStyledTextToBraille fromStyledTextToBraille() {
		if (fromStyledTextToBraille == null)
			fromStyledTextToBraille = new FromStyledTextToBraille() {
					public Iterable<CSSStyledText> transform(Iterable<CSSStyledText> input, int from, int to) {
						List<CSSStyledText> braille = new ArrayList<>(); {
							int i = 0;
							for (CSSStyledText styledText : input) {
								if (i >= from && (to < 0 || i < to)) {
									SimpleInlineStyle style = styledText.getStyle();
									String text = styledText.getText();
									boolean unicodeBraille = brailleCharset == null || !useBrailleCharsetForInput;
									if (style != null) {
										style = (SimpleInlineStyle)style.clone();
										CSSProperty val = style.getProperty("hyphens");
										if (val == Hyphens.MANUAL || val == Hyphens.NONE) {
											if (val == Hyphens.NONE)
												text = text.replaceAll("[\u00AD\u200B]","");
											style.removeProperty("hyphens"); }
										val = style.getProperty("braille-charset");
										if (val != null) {
											if (val == BrailleCharset.CUSTOM)
												unicodeBraille = false;
											else if (val == BrailleCharset.UNICODE)
												unicodeBraille = true; }}
									if (unicodeBraille && brailleCharset != null) {
										StringBuilder b; {
											b = new StringBuilder();
											boolean special = false;
											for (String s : splitInclDelimiter(text, SPECIAL_CHARS)) {
												if (!s.isEmpty())
													b.append(special ? s : brailleCharset.toText(s));
												special = !special;
											}
										}
										text = b.toString();
									}
									// update/add text-transform and braille-charset properties
									List<PropertyValue> s = null; {
										if (style == null) {
											if (s == null) s = new ArrayList<>();
											s.add(TEXT_TRANSFORM_NONE);
											if (brailleCharset != null)
												s.add(BRAILLE_CHARSET_CUSTOM);
										} else {
											TextTransform tt = style.getProperty("text-transform");
											if (tt != TextTransform.NONE) {
												if (tt != null)
													style.removeProperty("text-transform");
												if (s == null) s = new ArrayList<>();
												s.add(TEXT_TRANSFORM_NONE);
											}
											BrailleCharset bc = style.getProperty("braille-charset");
											if (brailleCharset != null) {
												if (bc != BrailleCharset.CUSTOM) {
													if (bc != null)
														style.removeProperty("braille-charset");
													if (s == null) s = new ArrayList<>();
													s.add(BRAILLE_CHARSET_CUSTOM);
												}
											} else
												if (bc != null && bc != BrailleCharset.UNICODE)
													style.removeProperty("braille-charset");
										}
									}
									if (s != null) {
										if (style != null)
											style.forEach(s::add);
										style = new SimpleInlineStyle(s);
									}
									Map<String,String> a = styledText.getTextAttributes();
									if (a != null)
										a = new HashMap<>(a);
									braille.add(new CSSStyledText(text, style, a));
								}
								i++;
							}
						}
						return braille;
					}
				};
		return fromStyledTextToBraille;
	}

	private LineBreakingFromStyledText lineBreakingFromStyledText = null;

	public LineBreakingFromStyledText lineBreakingFromStyledText() {
		if (lineBreakingFromStyledText == null) {
			Character blankChar = brailleCharset == null
				? '\u2800'
				: brailleCharset.toText("\u2800").toCharArray()[0];
			Character hyphenChar = brailleCharset == null
				? '\u2824'
				: brailleCharset.toText("\u2824").toCharArray()[0];
			lineBreakingFromStyledText = new DefaultLineBreaker(blankChar, hyphenChar, brailleCharset, logger) {
					protected BrailleStream translateAndHyphenate(java.lang.Iterable<CSSStyledText> input, int from, int to) {
						List<String> braille = new ArrayList<>(); {
							int i = 0;
							for (CSSStyledText styledText : input) {
								String text = styledText.getText();
								if (i >= from && (to < 0 || i < to)) {
									SimpleInlineStyle style = styledText.getStyle();
									if (style != null)
										style = (SimpleInlineStyle)style.clone();
									boolean unicodeBraille = brailleCharset == null || !useBrailleCharsetForInput;
									if (style != null) {
										CSSProperty val = style.getProperty("hyphens");
										if (val == Hyphens.MANUAL || val == Hyphens.NONE) {
											if (val == Hyphens.NONE)
												text = text.replaceAll("[\u00AD\u200B]","");
											style.removeProperty("hyphens"); }
										val = style.getProperty("white-space");
										if (val != null) {
											if (val == WhiteSpace.PRE_WRAP)
												text = text.replaceAll("[\\x20\t\\u2800]+", "$0\u200B") // ZERO WIDTH SPACE
												           .replaceAll("[\\x20\t\\u2800]", "\u00A0"); // NO-BREAK SPACE
											if (val == WhiteSpace.PRE_WRAP || val == WhiteSpace.PRE_LINE)
												text = text.replaceAll("[\\n\\r]", "\u2028"); // LINE SEPARATOR
											style.removeProperty("white-space"); }
										val = style.getProperty("text-transform");
										if (val == TextTransform.NONE || val == TextTransform.AUTO)
											style.removeProperty("text-transform");
										val = style.getProperty("braille-charset");
										if (val != null) {
											if (val == BrailleCharset.CUSTOM)
												unicodeBraille = false;
											else if (val == BrailleCharset.UNICODE)
												unicodeBraille = true;
											style.removeProperty("braille-charset"); }
										for (String prop : style.getPropertyNames()) {
											logger.warn("'{}: {}' not supported in combination with 'text-transform: none'",
											            prop, style.get(prop));
											logger.debug("(text was: '" + text + "')"); }}
									if (unicodeBraille && brailleCharset != null) {
										StringBuilder b; {
											b = new StringBuilder();
											boolean special = false;
											for (String s : splitInclDelimiter(text, SPECIAL_CHARS)) {
												if (!s.isEmpty())
													b.append(special ? s : brailleCharset.toText(s));
												special = !special;
											}
										}
										text = b.toString();
									}
								} else {
									// not converting to braille character set because not part of final output and we're not even
									// sure that it is braille
									// FIXME: may not even be useful to pass it as context to DefaultLineBreaker.LineIterator
								}
								braille.add(text);
								i++;
							}
						}
						StringBuilder joined = new StringBuilder();
						int fromChar = 0;
						int toChar = to >= 0 ? 0 : -1;
						for (String s : braille) {
							joined.append(s);
							if (--from == 0)
								fromChar = joined.length();
							if (--to == 0)
								toChar = joined.length();
						}
						return new FullyHyphenatedAndTranslatedString(joined.toString(), fromChar, toChar, hyphenChar);
					}
				};
		}
		return lineBreakingFromStyledText;
	}

	private static final Logger logger = LoggerFactory.getLogger(UnityBrailleTranslator.class);
}
