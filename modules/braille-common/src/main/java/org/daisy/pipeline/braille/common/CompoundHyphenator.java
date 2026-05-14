package org.daisy.pipeline.braille.common;

import java.util.function.Supplier;
import java.util.HashMap;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Iterables;

import org.daisy.braille.css.LanguageRange;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.LanguageBasedDispatchingFullHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.NoHyphenator;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import org.daisy.pipeline.braille.css.CSSStyledText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Hyphenator} that dispatches to sub-hyphenators based on locale.
 */
public class CompoundHyphenator extends AbstractHyphenator {

	private final Map<LanguageRange,Hyphenator> hyphenators;
	private final boolean implementsFullHyphenator;
	private final boolean implementsLineBreaker;

	private final static Locale UND = parseLocale("und");
	private final static LanguageRange ANY_LANGUAGE = new LanguageRange() {
			@Override
			public String toString() { return "*"; }
			public String toString(int depth) { return toString(); }
			public boolean matches(Locale language) { return true; }
		};
	private final static FullHyphenator COMPOUND_WORD_HYPHENATOR = new NoHyphenator();

	/**
	 * @param subHyphenators     ordered map of language ranges to {@link Hyphenator}s
	 * @param fallbackHyphenator hyphenator to use when no sub-hyphenator matches, or {@code null}
	 */
	public CompoundHyphenator(Map<LanguageRange,Supplier<Hyphenator>> subHyphenators, Hyphenator fallbackHyphenator) {
		hyphenators = new LinkedHashMap<>();
		for (Map.Entry<LanguageRange,Supplier<Hyphenator>> h : subHyphenators.entrySet())
			try {
				hyphenators.put(h.getKey(), h.getValue().get());
			} catch (NoSuchElementException e) {
				logger.warn("No hyphenator found for handling language range '" + h.getKey() + "'");
			}
		if (fallbackHyphenator != null)
			hyphenators.put(ANY_LANGUAGE, fallbackHyphenator);
		implementsFullHyphenator = Iterables.all(
			hyphenators.values(),
			h -> {
				try {
					h.asFullHyphenator();
					return true; }
				catch (UnsupportedOperationException e) {
					return false; }} );
		implementsLineBreaker = Iterables.all(
			hyphenators.values(),
			h -> {
				try {
					h.asLineBreaker();
					return true; }
				catch (UnsupportedOperationException e) {
					return false; }} );
	}

	private final Map<Locale,Hyphenator> cache = new HashMap<>();

	private Optional<Hyphenator> getHyphenator(Locale language) {
		Hyphenator h = cache.get(language);
		if (h != null)
			return Optional.of(h);
		for (LanguageRange l : hyphenators.keySet())
			if (l.matches(language)) {
				h = hyphenators.get(l);
				break;
			}
		if (h != null)
			cache.put(language, h);
		return Optional.ofNullable(h);
	}

	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper("CompoundHyphenator")
			.add("hyphenators", hyphenators);
	}

	private FullHyphenator fullHyphenator = null;

	/**
	 * @throws UnsupportedOperationException if any of the sub-hyphenators throws an {@link UnsupportedOperationException}
	 */
	@Override
	public FullHyphenator asFullHyphenator() throws UnsupportedOperationException {
		if (!implementsFullHyphenator)
			throw new UnsupportedOperationException();
		if (fullHyphenator == null)
			fullHyphenator = new LanguageBasedDispatchingFullHyphenator() {
					@Override
					protected Iterable<CSSStyledText> transform(Iterable<CSSStyledText> text, Locale language)
							throws NonStandardHyphenationException {
						FullHyphenator h = ((language == null || UND.equals(language))
							? Optional.<Hyphenator>empty()
							: getHyphenator(language)
						).map(Hyphenator::asFullHyphenator).orElse(COMPOUND_WORD_HYPHENATOR);
						return h.transform(text);
					}
					@Override
					public String toString() {
						return CompoundHyphenator.this.toString();
					}
				};
		return fullHyphenator;
	};

	private LineBreaker lineBreaker = null;

	/**
	 * @throws UnsupportedOperationException if any of the sub-hyphenators throws an {@link UnsupportedOperationException}
	 */
	@Override
	public LineBreaker asLineBreaker() throws UnsupportedOperationException {
		if (!implementsLineBreaker)
			throw new UnsupportedOperationException();
		if (lineBreaker == null)
			lineBreaker = new DefaultLineBreaker() {
					protected Break breakWord(String word, Locale language, int limit, boolean force) {
						Optional<LineBreaker> h = ((language == null || UND.equals(language))
							? Optional.<Hyphenator>empty()
							: getHyphenator(language)
						).map(Hyphenator::asLineBreaker);
						if (!h.isPresent())
							return super.breakWord(word, language, limit, force);
						else {
							LineIterator lines = h.get().transform(word, language);
							String next = lines.nextLine(limit, force);
							boolean hyphen = lines.lineHasHyphen();
							return new Break(next + lines.remainder(), next.length(), hyphen);
						}
					}
					@Override
					public String toString() {
						return CompoundHyphenator.this.toString();
					}
				};
		return lineBreaker;
	}

	private static final Logger logger = LoggerFactory.getLogger(CompoundHyphenator.class);
}
