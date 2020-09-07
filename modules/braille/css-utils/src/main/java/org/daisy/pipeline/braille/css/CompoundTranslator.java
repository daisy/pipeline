package org.daisy.pipeline.braille.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.dotify.api.translator.UnsupportedMetricException;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.CSSStyledText;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;

/*
 * Translator that dispatches to sub-translator based on text-transform values.
 */
public class CompoundTranslator extends AbstractBrailleTranslator {

	private final Map<String,Supplier<BrailleTranslator>> translators;

	public CompoundTranslator(BrailleTranslator mainTranslator, Map<String,Supplier<BrailleTranslator>> subTranslators) {
		if (subTranslators.containsKey("auto") || mainTranslator == null)
			throw new IllegalArgumentException();
		translators = ImmutableMap.<String,Supplier<BrailleTranslator>>builder()
		                          .put("auto", () -> mainTranslator)
		                          .putAll(subTranslators)
		                          .build();
	}

	private static abstract class TransformImpl<T> {

		abstract Iterable<T> transform(Iterable<CSSStyledText> styledText, int from, int to, String textTransform);

		abstract boolean supports(String textTransform);

		List<T> transform(Iterable<CSSStyledText> styledText, int from, int to) {
			if (to < 0)
				to = Iterables.size(styledText);
			if (from < 0 || from > to)
				throw new IndexOutOfBoundsException();
			List<T> transformed = new ArrayList<>();
			if (from == to) return transformed;
			List<CSSStyledText> buffer = new ArrayList<CSSStyledText>();
			String curTextTransform = "auto";
			for (CSSStyledText st : styledText) {
				SimpleInlineStyle style = st.getStyle();
				String textTransform; {
					textTransform = "auto";
					if (style != null) {
						CSSProperty val = style.getProperty("text-transform");
						if (val != null) {
							if (val == TextTransform.list_values) {
								TermList values = style.getValue(TermList.class, "text-transform");
								
								// According to the spec values should be "applied" from left to right, and
								// values of inner elements always come before values of outer elements (see
								// http://braillespecs.github.io/braille-css/#the-text-transform-property).
								// The way it works here is that the sub-translator that maps to the first
								// text-transform value (starting from left) is used to translate the segment,
								// and all values to the right are ignored.
								Iterator<Term<?>> it = values.iterator();
								while (it.hasNext()) {
									String tt = ((TermIdent)it.next()).getValue();
									if (supports(tt)) {
										textTransform = tt;
										it.remove();
										while (it.hasNext())
											it.remove();
										break; }}
								if (values.isEmpty())
									style.removeProperty("text-transform"); }}}
				}
				if (textTransform != curTextTransform && !buffer.isEmpty()) {
					if (from < buffer.size())
						for (T s : transform(buffer, from, to < buffer.size() ? to : -1, curTextTransform))
							transformed.add(s);
					from -= buffer.size();
					if (from < 0) from = 0;
					if (to > 0) {
						to -= buffer.size();
						if (to <= 0)
							return transformed; }
					buffer = new ArrayList<CSSStyledText>(); }
				curTextTransform = textTransform;
				buffer.add(st); }
			if (!buffer.isEmpty() && from < buffer.size())
				for (T s : transform(buffer, from, to < buffer.size() ? to : -1, curTextTransform))
					transformed.add(s);
			return transformed;
		}
	}

	@Override
	public FromStyledTextToBraille fromStyledTextToBraille() {
		return fromStyledTextToBraille;
	}

	private final FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
		TransformImpl<String> impl = new TransformImpl<String>() {
			Iterable<String> transform(Iterable<CSSStyledText> styledText, int from, int to, String textTransform) {
				return translators.get(textTransform).get().fromStyledTextToBraille().transform(styledText, from, to);
			}
			boolean supports(String textTransform) {
				if (translators.containsKey(textTransform))
					try {
						translators.get(textTransform).get();
						return true;
					} catch (NoSuchElementException e) {
					}
				return false;
			}
		};
		public Iterable<String> transform(Iterable<CSSStyledText> styledText, int from, int to) {
			return impl.transform(styledText, from, to);
		}
	};

	@Override
	public LineBreakingFromStyledText lineBreakingFromStyledText() {
		return lineBreakingFromStyledText;
	}

	private final LineBreakingFromStyledText lineBreakingFromStyledText = new LineBreakingFromStyledText() {
		TransformImpl<LineIterator> impl = new TransformImpl<LineIterator>() {
			Iterable<LineIterator> transform(Iterable<CSSStyledText> styledText, int from, int to, String textTransform) {
				return Collections.singleton(
					translators.get(textTransform).get().lineBreakingFromStyledText().transform(styledText, from, to));
			}
			boolean supports(String textTransform) {
				if (translators.containsKey(textTransform))
					try {
						translators.get(textTransform).get();
						return true;
					} catch (NoSuchElementException e) {
					}
				return false;
			}
		};
		public LineIterator transform(Iterable<CSSStyledText> styledText, int from, int to) {
			return concatLineIterators(impl.transform(styledText, from, to));
		}
	};

	private static BrailleTranslator.LineIterator concatLineIterators(List<BrailleTranslator.LineIterator> iterators) {
		if (iterators.size() == 0)
			return new BrailleTranslator.LineIterator() {
				public String nextTranslatedRow(int limit, boolean force, boolean wholeWordsOnly) {
					return "";
				}
				public String getTranslatedRemainder() {
					return "";
				}
				public int countRemaining() {
					return 0;
				}
				public boolean hasNext() {
					return false;
				}
				public BrailleTranslator.LineIterator copy() {
					return this;
				}
				public boolean supportsMetric(String metric) {
					return false;
				}
				public double getMetric(String metric) {
					throw new UnsupportedMetricException("Metric not supported: " + metric);
				}
			};
		else if (iterators.size() == 1 && iterators.get(0) != null)
			return iterators.get(0);
		else
			return new ConcatLineIterators(iterators);
	}

	private static class ConcatLineIterators implements BrailleTranslator.LineIterator {

		final List<BrailleTranslator.LineIterator> iterators;
		BrailleTranslator.LineIterator current;
		int currentIndex = 0;

		ConcatLineIterators(List<BrailleTranslator.LineIterator> iterators) {
			this.iterators = iterators;
			currentIndex = -1;
			current = null;
			computeCurrent();
		}

		void computeCurrent() {
			while (current == null || !current.hasNext())
				if (currentIndex + 1 < iterators.size())
					current = iterators.get(++currentIndex);
				else {
					current = null;
					break; }
		}

		public String nextTranslatedRow(int limit, boolean force, boolean wholeWordsOnly) {
			String row = "";
			while (limit > row.length()) {
				if (current == null) break;
				row += current.nextTranslatedRow(limit - row.length(), force, wholeWordsOnly);
				computeCurrent(); }
			return row;
		}

		public String getTranslatedRemainder() {
			String remainder = "";
			if (current == null) return remainder;
			for (int i = currentIndex; i < iterators.size(); i++)
				if (iterators.get(i) != null)
					remainder += iterators.get(i).getTranslatedRemainder();
			return remainder;
		}

		public int countRemaining() {
			int remaining = 0;
			if (current == null) return remaining;
			for (int i = currentIndex; i < iterators.size(); i++)
				if (iterators.get(i) != null)
					remaining += iterators.get(i).countRemaining();
			return remaining;
		}

		public boolean hasNext() {
			computeCurrent();
			return current != null;
		}

		public ConcatLineIterators copy() {
			List<BrailleTranslator.LineIterator> iteratorsCopy = new ArrayList<>(iterators.size() - currentIndex);
			for (int i = currentIndex; i < iterators.size(); i++)
				if (iterators.get(i) != null)
					iteratorsCopy.add((BrailleTranslator.LineIterator)iterators.get(i).copy());
			return new ConcatLineIterators(iteratorsCopy);
		}

		public boolean supportsMetric(String metric) {
			return false;
		}

		public double getMetric(String metric) {
			throw new UnsupportedMetricException("Metric not supported: " + metric);
		}
	}
}
