package org.daisy.pipeline.braille.dotify.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermInteger;

import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BrailleTranslatorProvider} of PreTranslatedBrailleTranslator and NumberBrailleTranslator.
 */
@Component(
	name = "org.daisy.pipeline.braille.dotify.impl.PreTranslatedBrailleTranslatorProvider",
	service = {
		BrailleTranslatorProvider.class
	}
)
public class PreTranslatedBrailleTranslatorProvider extends AbstractTransformProvider<BrailleTranslator>
                                                    implements BrailleTranslatorProvider<BrailleTranslator>  {

	private static final Logger logger = LoggerFactory.getLogger(PreTranslatedBrailleTranslatorProvider.class);

	protected Iterable<BrailleTranslator> _get(Query query) {
		boolean isPreTranslatedQuery = false; {
			for (Query.Feature f : query)
				if ("input".equals(f.getKey()) && "braille".equals(f.getValue().orElse(null)))
					isPreTranslatedQuery = true;
				else if (!("locale".equals(f.getKey()) ||
				           "input".equals(f.getKey()) && "text-css".equals(f.getValue().orElse(null)) ||
				           "output".equals(f.getKey()) && "braille".equals(f.getValue().orElse(null)))) {
					isPreTranslatedQuery = false;
					break; }}
		if (isPreTranslatedQuery)
			return AbstractTransformProvider.util.Iterables.of(PreTranslatedBrailleTranslator.getInstance());
		try {
			MutableQuery q = mutableQuery(query);
			for (Query.Feature f : q.removeAll("input"))
				if (!"text-css".equals(f.getValue().get()))
					throw new NoSuchElementException();
			for (Query.Feature f : q.removeAll("output"))
				if (!"braille".equals(f.getValue().get()))
					throw new NoSuchElementException();
			if (!q.isEmpty())
				throw new NoSuchElementException();
			return AbstractTransformProvider.util.Iterables.of(NumberBrailleTranslator.getInstance());
		} catch (NoSuchElementException e) {}
		return AbstractTransformProvider.util.Iterables.<BrailleTranslator>empty();
	}

	/**
	 * {@link org.daisy.pipeline.braille.common.BrailleTranslator} that assumes input text exists of
	 * only braille and white space characters. Supports CSS properties "word-spacing", "hyphens"
	 * and "white-space".
	 */
	private static class PreTranslatedBrailleTranslator extends AbstractBrailleTranslator {

		private static PreTranslatedBrailleTranslator INSTANCE = null;
		private static PreTranslatedBrailleTranslator getInstance() {
			if (INSTANCE == null)
				INSTANCE = new PreTranslatedBrailleTranslator();
			return INSTANCE;
		}

		@Override
		public LineBreakingFromStyledText lineBreakingFromStyledText() {
			return lineBreakingFromStyledText;
		}

		private final LineBreakingFromStyledText lineBreakingFromStyledText = new LineBreakingFromStyledText() {
			public LineIterator transform(java.lang.Iterable<CSSStyledText> input, int from, int to) {
				List<String> braille = new ArrayList<>();
				int wordSpacing; {
					wordSpacing = -1;
					for (CSSStyledText styledText : input) {
						SimpleInlineStyle style = styledText.getStyle();
						int spacing = 1;
						String text = styledText.getText();
						if (style != null) {
							CSSProperty val = style.getProperty("word-spacing");
							if (val != null) {
								if (val == WordSpacing.length) {
									spacing = style.getValue(TermInteger.class, "word-spacing").getIntValue();
									if (spacing < 0) {
										if (logger != null)
											logger.warn("word-spacing: {} not supported, must be non-negative", val);
										spacing = 1; }}
										
								// FIXME: assuming style is mutable and text.iterator() does not create copies
								style.removeProperty("word-spacing"); }
							if (style.getProperty("hyphens") == Hyphens.NONE) {
								text = text.replaceAll("[\u00AD\u200B]","");
								style.removeProperty("hyphens"); }
							val = style.getProperty("white-space");
							if (val != null) {
								if (val == WhiteSpace.PRE_WRAP)
									text = text.replaceAll("[\\x20\t\\u2800]+", "$0\u200B")
									           .replaceAll("[\\x20\t\\u2800]", "\u00A0");
								if (val == WhiteSpace.PRE_WRAP || val == WhiteSpace.PRE_LINE)
									text = text.replaceAll("[\\n\\r]", "\u2028");
								style.removeProperty("white-space"); }
							for (String prop : style.getPropertyNames())
								logger.warn("{}: {} not supported", prop, style.get(prop)); }
						if (wordSpacing < 0)
							wordSpacing = spacing;
						else if (wordSpacing != spacing)
							throw new RuntimeException("word-spacing must be constant, but both "
							                           + wordSpacing + " and " + spacing + " specified");
						Map<String,String> attrs = styledText.getTextAttributes();
						if (attrs != null)
							for (String k : attrs.keySet())
								logger.warn("Text attribute \"{}:{}\" ignored", k, attrs.get(k));
						braille.add(text); }
					if (wordSpacing < 0) wordSpacing = 1; }
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
				return new DefaultLineBreaker.LineIterator(brailleString.toString(), fromChar, toChar, '\u2800', '\u2824', wordSpacing);
			}
		};
	}
}
