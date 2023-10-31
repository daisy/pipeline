package org.daisy.pipeline.braille.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.dotify.api.translator.UnsupportedMetricException;
import org.daisy.pipeline.braille.common.Hyphenator.NonStandardHyphenationException;
import org.daisy.pipeline.braille.css.CSSStyledText;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BrailleTranslator} that dispatches to sub-translators based on text-transform values.
 */
public class CompoundBrailleTranslator extends AbstractBrailleTranslator {

	private final Map<String,BrailleTranslator> translators;
	private final boolean implementsFromStyledTextToBraille;
	private final boolean implementsLineBreakingFromStyledText;

	public CompoundBrailleTranslator(BrailleTranslator mainTranslator, Map<String,Supplier<BrailleTranslator>> subTranslators) {
		if (subTranslators.containsKey("auto") || mainTranslator == null)
			throw new IllegalArgumentException();
		translators = new HashMap<>();
		translators.put("auto", mainTranslator);
		for (Map.Entry<String,Supplier<BrailleTranslator>> t : subTranslators.entrySet())
			try {
				translators.put(t.getKey(), t.getValue().get());
			} catch (NoSuchElementException e) {
				logger.warn("No braille translator found for handling text-transform '" + t.getKey() + "'");
			}
		implementsFromStyledTextToBraille = Iterables.all(
			translators.values(),
			t -> {
				try {
					t.fromStyledTextToBraille();
					return true; }
				catch (UnsupportedOperationException e) {
					return false; }} );
		implementsLineBreakingFromStyledText = Iterables.all(
			translators.values(),
			t -> {
				try {
					t.lineBreakingFromStyledText();
					return true; }
				catch (UnsupportedOperationException e) {
					return false; }} );
	}

	protected CompoundBrailleTranslator(CompoundBrailleTranslator from) {
		super(from);
		this.translators = from.translators;
		this.implementsFromStyledTextToBraille = from.implementsFromStyledTextToBraille;
		this.implementsLineBreakingFromStyledText = from.implementsLineBreakingFromStyledText;
	}

	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper("CompoundBrailleTranslator")
			.add("translators", translators);
	}

	/**
	 * @throws UnsupportedOperationException if the main translator or one of the sub-translators's
	 *                                       {@code #withHyphenator()} method throws {@link UnsupportedOperationException}
	 */
	public CompoundBrailleTranslator _withHyphenator(Hyphenator hyphenator) {
		Map<String,BrailleTranslator> translatorsWithHyphenator = new HashMap<>(); {
			for (String k : translators.keySet())
				translatorsWithHyphenator.put(k, translators.get(k).withHyphenator(hyphenator));
		}
		return new CompoundBrailleTranslator(translatorsWithHyphenator.remove("auto"),
		                                     Maps.transformValues(translatorsWithHyphenator, x -> (() -> x)));
	}

	private static abstract class TransformImpl<T> {

		abstract Iterable<T> transform(Iterable<CSSStyledText> styledText, int from, int to, String textTransform);
		abstract Iterable<String> transformContext(Iterable<CSSStyledText> styledText, int from, int to, String textTransform);
		
		abstract boolean supports(String textTransform);

		List<T> transform(Iterable<CSSStyledText> styledText, int from, int to) {
			if (to < 0)
				to = Iterables.size(styledText);
			if (from < 0 || from > to)
				throw new IndexOutOfBoundsException();
			List<T> transformed = new ArrayList<>();
			if (from == to) return transformed;
			List<CSSStyledText> styledTextList = new ArrayList<>();
			styledText.forEach(styledTextList::add);
			// segments with same text-transform to be transformed next
			List<CSSStyledText> buffer = new ArrayList<CSSStyledText>();
			// already transformed segments (text-transform: none) to be used as context for next transformation
			List<CSSStyledText> context = new ArrayList<CSSStyledText>();
			String textTransform = "auto";
			for (int i = styledTextList.size(); i >= 0; i--) {
				CSSStyledText next;
				String nextTextTransform; {
					if (i == 0) {
						next = null;
						nextTextTransform = null;
					} else {
						next = styledTextList.get(i - 1);
						nextTextTransform = "auto";
						SimpleInlineStyle style = next.getStyle();
						if (style != null) {
							CSSProperty val = style.getProperty("text-transform");
							if (val != null) {
								if (val == TextTransform.NONE) {
									if (supports("none")) {
										nextTextTransform = "none";
										style.removeProperty("text-transform"); }
								} else if (val == TextTransform.list_values) {
									TermList values = style.getValue(TermList.class, "text-transform");

									// According to the spec values should be "applied" from left to right, and
									// values of inner elements always come before values of outer elements (see
									// http://braillespecs.github.io/braille-css/#the-text-transform-property).
									// The way it works here is that the sub-translator that maps to the first
									// text-transform value (starting from left) is used to translate the segment,
									// and all values to the left and right are passed to the sub-translator.
									Iterator<Term<?>> it = values.iterator();
									while (it.hasNext()) {
										String tt = ((TermIdent)it.next()).getValue();
										if (supports(tt)) {
											nextTextTransform = tt;
											it.remove();
											break; }}
									if (values.isEmpty())
										style.removeProperty("text-transform"); }}}
					}
				}
				if (next == null || (nextTextTransform != textTransform && buffer.size() > 0)) {
					if (i < to) {
						int j = 0;
						for (T t : transform(org.daisy.pipeline.braille.common.util.Iterables.clone(
						                         Iterables.concat(buffer, context)),
						                     from > i ? from - i : 0,
						                     to < i + buffer.size() ? to - i : buffer.size(),
						                     textTransform))
							transformed.add(j++, t);
					}
					if (i <= from)
						return transformed;
					else {
						// set context for next transform
						try {
							int j = 0;
							for (String s : transformContext(org.daisy.pipeline.braille.common.util.Iterables.clone(
							                                     Iterables.concat(buffer, context)),
							                                 from > i ? from - i : 0,
							                                 buffer.size(),
							                                 textTransform))
								context.add(j++, new CSSStyledText(s, "text-transform: none; braille-charset: custom"));
							buffer.clear();
						} catch (NonStandardHyphenationException e) {
							// try without hyphenation
							for (int j = buffer.size() - 1; j >= 0; j--) {
								CSSStyledText st = (CSSStyledText)buffer.get(j).clone();
								SimpleInlineStyle style = st.getStyle();
								if (style != null && style.getProperty("hyphens") == Hyphens.AUTO)
									style.removeProperty("hyphens");
								buffer.set(j, st);
								context.add(0,
								            new CSSStyledText(
								                transformContext(org.daisy.pipeline.braille.common.util.Iterables.clone(
								                                     Iterables.concat(buffer, context)),
								                                 j,
								                                 j + 1,
								                                 textTransform).iterator().next(),
								                "text-transform: none; braille-charset: custom"));
								buffer.remove(j);
							}
						}
					}
				}
				if (next != null) {
					buffer.add(0, next);
					textTransform = nextTextTransform;
				}
			}
			return transformed;
		}
	}

	private FromStyledTextToBraille fromStyledTextToBraille = null;

	/**
	 * @throws UnsupportedOperationException if the main translator or one of the sub-translators's
	 *                                       {@code #withHyphenator()} method throws {@link UnsupportedOperationException}
	 */
	@Override
	public FromStyledTextToBraille fromStyledTextToBraille() {
		if (!implementsFromStyledTextToBraille)
			throw new UnsupportedOperationException();
		if (fromStyledTextToBraille == null)
			fromStyledTextToBraille = new FromStyledTextToBraille() {
					TransformImpl<String> impl = new TransformImpl<String>() {
							Iterable<String> transform(Iterable<CSSStyledText> styledText, int from, int to, String textTransform) {
								return translators.get(textTransform).fromStyledTextToBraille().transform(styledText, from, to);
							}
							Iterable<String> transformContext(Iterable<CSSStyledText> styledText, int from, int to, String textTransform) {
								return transform(styledText, from, to, textTransform);
							}
							boolean supports(String textTransform) {
								return translators.containsKey(textTransform);
							}
						};
					public Iterable<String> transform(Iterable<CSSStyledText> styledText, int from, int to) {
						return impl.transform(styledText, from, to);
					}
					@Override
					public String toString() {
						return CompoundBrailleTranslator.this.toString();
					}
				};
		return fromStyledTextToBraille;
	}

	private LineBreakingFromStyledText lineBreakingFromStyledText = null;

	/**
	 * @throws UnsupportedOperationException if the main translator or one of the sub-translators's
	 *                                       {@code #withHyphenator()} method throws {@link UnsupportedOperationException}
	 */
	@Override
	public LineBreakingFromStyledText lineBreakingFromStyledText() {
		if (!implementsLineBreakingFromStyledText)
			throw new UnsupportedOperationException();
		if (lineBreakingFromStyledText == null)
			lineBreakingFromStyledText = new LineBreakingFromStyledText() {
					TransformImpl<LineIterator> impl = new TransformImpl<LineIterator>() {
							Iterable<LineIterator> transform(Iterable<CSSStyledText> styledText, int from, int to, String textTransform) {
								return Collections.singleton(
									translators.get(textTransform).lineBreakingFromStyledText().transform(styledText, from, to));
							}
							Iterable<String> transformContext(Iterable<CSSStyledText> styledText, int from, int to, String textTransform) {
								return translators.get(textTransform).fromStyledTextToBraille().transform(styledText, from, to);
							}
							boolean supports(String textTransform) {
								return translators.containsKey(textTransform);
							}
						};
					public LineIterator transform(Iterable<CSSStyledText> styledText, int from, int to) {
						return concatLineIterators(impl.transform(styledText, from, to));
					}
					@Override
					public String toString() {
						return CompoundBrailleTranslator.this.toString();
					}
				};
		return lineBreakingFromStyledText;
	}

	/*
	 * Note that this function can not be used to concatenate any LineIterator. It is assumed that
	 * the LineIterator are translations of consecutive segments of the same string, where each
	 * translation has taken into account the whole context.
	 */
	static BrailleTranslator.LineIterator concatLineIterators(List<BrailleTranslator.LineIterator> iterators) {
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
			BrailleTranslator.LineIterator prev = null;
			while (limit > row.length()) {
				if (current == null || current == prev) break;
				String s = current.nextTranslatedRow(limit - row.length(), force, wholeWordsOnly);
				if (s.isEmpty() && current.hasNext())
					break;
				else
					force = false;
				row += s;
				prev = current;
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

	private static final Logger logger = LoggerFactory.getLogger(CompoundBrailleTranslator.class);

}
