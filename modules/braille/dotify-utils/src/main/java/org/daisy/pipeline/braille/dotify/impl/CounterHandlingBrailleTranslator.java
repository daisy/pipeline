package org.daisy.pipeline.braille.dotify.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;

import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslator.LineBreakingFromStyledText;
import org.daisy.pipeline.braille.css.CounterStyle;
import org.daisy.pipeline.braille.css.CSSStyledText;

/**
 * {@link BrailleTranslator} that handles the <code>text-transform</code> value
 * "<code>-dotify-counter</code>", which causes numbers to be formatted according to the
 * value of the <code>-dotify-counter-style</code>
 * property. <code>-dotify-counter-style</code> can be the name of a custom counter style,
 * or a <code>symbols()</code> function.
 */
public class CounterHandlingBrailleTranslator extends AbstractBrailleTranslator implements LineBreakingFromStyledText {

	private final LineBreakingFromStyledText backingTranslator;
	private final Map<String,CounterStyle> customCounterStyles;

	public CounterHandlingBrailleTranslator(BrailleTranslator backingTranslator,
	                                        Map<String,CounterStyle> customCounterStyles) {
		this.backingTranslator = backingTranslator.lineBreakingFromStyledText();
		this.customCounterStyles = customCounterStyles;
	}

	@Override
	public LineBreakingFromStyledText lineBreakingFromStyledText() {
		return this;
	}

	public LineIterator transform(Iterable<CSSStyledText> styledText, int from, int to) {
		return backingTranslator.transform(handleCounterStyles(styledText), from, to);
	}

	private Iterable<CSSStyledText> handleCounterStyles(Iterable<CSSStyledText> styledText) {
		List<CSSStyledText> segments = new ArrayList<CSSStyledText>();
		String segment = null;
		SimpleInlineStyle style = null;
		Locale lang = null;
		Map<String,String> attrs = null;
		for (CSSStyledText st : styledText) {
			String t = st.getText();
			SimpleInlineStyle s = st.getStyle();
			Locale l = st.getLanguage();
			Map<String,String> a = st.getTextAttributes();
			if (s != null) {
				if (s.getProperty("text-transform") == TextTransform.list_values) {
					TermList list = s.getValue(TermList.class, "text-transform");
					if (((TermIdent)list.get(0)).getValue().equals("-dotify-counter")) {
						if (list.size() == 1)
							s.removeProperty("text-transform");
						else
							list.remove(0);
						if ("??".equals(t)) {
							// If input text is "??", it will be used for creating a placeholder for content that
							// can not be computed yet (see org.daisy.dotify.formatter.impl.row.SegmentProcessor).
						} else {
							int counterValue = Integer.parseInt(t);
							Term<?> symbolsFunction = s.getValue("-dotify-counter-style");
							if (symbolsFunction != null) {
								try {
									t = new CounterStyle(symbolsFunction).format(counterValue);
								} catch (IllegalArgumentException e) {
									// FIXME: show warning
								}
							} else {
								CSSProperty counterStyleName = s.getProperty("-dotify-counter-style");
								if (counterStyleName != null
								    && customCounterStyles != null
								    && customCounterStyles.containsKey(counterStyleName.toString())) {
									t = customCounterStyles.get(counterStyleName.toString()).format(counterValue);
								}
							}
						}
					}
				}
				s.removeProperty("-dotify-counter-style"); }
			if (segment != null)
				segments.add(new CSSStyledText(segment, style, lang, attrs));
			segment = t;
			style = s;
			lang = l;
			attrs = a; }
		if (segment != null)
			segments.add(new CSSStyledText(segment, style, lang, attrs));
		return segments;
	}
}
