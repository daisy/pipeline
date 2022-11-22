package org.daisy.pipeline.braille.css.impl;

import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.google.common.cache.CacheBuilder;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.CounterIncrement;
import cz.vutbr.web.css.CSSProperty.CounterSet;
import cz.vutbr.web.css.CSSProperty.CounterReset;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermList;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.SupportedBrailleCSS;

import org.daisy.pipeline.braille.css.impl.ContentList.ContentFunction;

import org.w3c.dom.Element;

public class BrailleCssParser {

	private BrailleCssParser() {}

	/**
	 * Style assumed to be specified in the context of a (pseudo-)element.
	 *
	 * @param context element for evaluating attr() values against.
	 * @param mutable Whether the caller wishes to mutate the returned declaration.
	 */
	public static Optional<Declaration> parseDeclaration(String property, String value, Element context, boolean mutable) {
		String style = String.format("%s: %s", property, value);
		try {
			return Optional.of(parseSimpleInlineStyle(style, context, mutable).iterator().next());
		} catch (NoSuchElementException e) {
			return Optional.empty();
		}
	}

	/**
	 * @param style assumed to be specified in the context of a (pseudo-)element
	 * @param context element for evaluating attr() values against.
	 * @param mutable Whether the caller wishes to mutate the returned style object.
	 */
	public static SimpleInlineStyle parseSimpleInlineStyle(String style, Element context, boolean mutable) {
		BrailleCssStyle s = BrailleCssStyle.of(style, Context.ELEMENT);
		if (s.nestedStyles != null)
			throw new IllegalArgumentException("not a simple inline style");
		if (s.declarations == null)
			return SimpleInlineStyle.EMPTY;
		if (!(s.declarations instanceof SimpleInlineStyle))
			throw new IllegalStateException(); // coding error
		// evaluate attr() values in content and string-set properties
		BrailleCssStyle evaluated = context != null ? s.evaluate(context) : s;
		SimpleInlineStyle declarations = (SimpleInlineStyle)evaluated.declarations;
		if (mutable && evaluated == s) {
			return (SimpleInlineStyle)declarations.clone();
		}
		return declarations;
	}

	/* =================================================================== */
	/* The fields and classes below are used only from BrailleCssStyle but */
	/* we keep it in this class because it is all related with parsing.    */
	/* =================================================================== */

	final static Cache cache = new Cache();

	static class Cache {

		private final Map<CacheKey,BrailleCssStyle> cache = CacheBuilder.newBuilder()
		                                                                .expireAfterAccess(60, TimeUnit.SECONDS)
		                                                                .<CacheKey,BrailleCssStyle>build()
		                                                                .asMap();
		void put(Context context, String serializedStyle, BrailleCssStyle style) {
			CacheKey key = new CacheKey(context, serializedStyle);
			cache.put(key, style);
		}

		BrailleCssStyle get(Context context, String serializedStyle) {
			CacheKey key = new CacheKey(context, serializedStyle);
			return cache.get(key);
		}

		private static class CacheKey implements Comparable<CacheKey> {
			private final Context context;
			private final String style;
			public CacheKey(Context context, String style) {
				this.context = context;
				this.style = style;
			}
			@Override
			public int compareTo(CacheKey that) {
				int i = this.context.compareTo(that.context);
				if (i != 0)
					return i;
				return this.style.compareTo(that.style);
			}
			@Override
			public int hashCode() {
				final int prime = 31;
				int hash = 1;
				hash = prime * hash + context.hashCode();
				hash = prime * hash + style.hashCode();
				return hash;
			}
			@Override
			public boolean equals(Object o) {
				if (o == null)
					return false;
				if (!(o instanceof CacheKey))
					return false;
				CacheKey that = (CacheKey)o;
				if (this.context != that.context)
					return false;
				return this.style.equals(that.style);
			}
		}
	}

	static class DeepDeclarationTransformer extends SupportedBrailleCSS {

		public DeepDeclarationTransformer(boolean allowComponentProperties, boolean allowShorthandProperties) {
			super(allowComponentProperties, allowShorthandProperties);
		}

		@Override
		public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
			if ("content".equalsIgnoreCase(d.getProperty())) {
				if (super.processContent(d, properties, values)) {
					if (properties.get("content") == Content.content_list) {
						Term<?> value = values.get("content");
						if (value instanceof TermList) {
							ContentList l = ContentList.of((TermList)value);
							for (Term<?> t : l)
								if (t instanceof ContentFunction && !((ContentFunction)t).target.isPresent())
									throw new IllegalArgumentException("unexpected term in content list: " + t);
							values.put("content", l);
						} else
							throw new IllegalStateException(); // should not happen
					}
					return true;
				} else
					return false;
			} else if ("string-set".equalsIgnoreCase(d.getProperty())) {
				if (super.processStringSet(d, properties, values)) {
					if (properties.get("string-set") == StringSet.list_values) {
						Term<?> value = values.get("string-set");
						if (value instanceof TermList)
							values.put("string-set", StringSetList.of((TermList)value));
						else
							throw new IllegalStateException(); // should not happen
					}
					return true;
				} else
					return false;
			} else if ("counter-set".equalsIgnoreCase(d.getProperty())) {
				if (super.processCounterSet(d, properties, values)) {
					if (properties.get("counter-set") == CounterSet.list_values) {
						Term<?> value = values.get("counter-set");
						if (value instanceof TermList)
							values.put("counter-set", CounterSetList.of((TermList)value));
						else
							throw new IllegalStateException(); // should not happen
					}
					return true;
				} else
					return false;
			} else if ("counter-reset".equalsIgnoreCase(d.getProperty())) {
				if (super.processCounterReset(d, properties, values)) {
					if (properties.get("counter-reset") == CounterReset.list_values) {
						Term<?> value = values.get("counter-reset");
						if (value instanceof TermList)
							values.put("counter-reset", CounterSetList.of((TermList)value));
						else
							throw new IllegalStateException(); // should not happen
					}
					return true;
				} else
					return false;
			} else if ("counter-increment".equalsIgnoreCase(d.getProperty())) {
				if (super.processCounterIncrement(d, properties, values)) {
					if (properties.get("counter-increment") == CounterIncrement.list_values) {
						Term<?> value = values.get("counter-increment");
						if (value instanceof TermList)
							values.put("counter-increment", CounterSetList.of((TermList)value));
						else
							throw new IllegalStateException(); // should not happen
					}
					return true;
				} else
					return false;
			} else
				return super.parseDeclaration(d, properties, values);
		}
	}
}
