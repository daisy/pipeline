package org.daisy.pipeline.braille.css.impl;

import java.util.concurrent.TimeUnit;
import java.util.Map;
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
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.PropertyValue;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle.ValidatedDeclarations;

import org.daisy.pipeline.braille.css.impl.ContentList.ContentFunction;

import org.w3c.dom.Element;

public class BrailleCssParser {

	private BrailleCssParser() {}

	/**
	 * Style assumed to be specified in the context of a (pseudo-)element.
	 *
	 * @param context element for evaluating attr() and content() values against.
	 * @param mutable Whether the caller wishes to mutate the returned declaration.
	 */
	public static Optional<Declaration> parseDeclaration(String property, String value, Element context, boolean mutable) {
		String style = String.format("%s: %s", property, value);
		Declaration declaration = declCache.get(style);
		if (declaration == null) {
			declaration = BrailleCssStyle.of(style, Context.ELEMENT).getDeclaration(property);
			if (declaration == null)
				return Optional.empty();
			declCache.put(style, declaration);
		}
		if (context != null) {
			Declaration evaluated = BrailleCssStyle.evaluateDeclaration(declaration, context);
			if (evaluated != declaration)
				return Optional.of(evaluated);
		}
		if (mutable) {
			Declaration unlocked = BrailleCssStyle.unlockDeclaration(declaration);
			if (unlocked != declaration)
				return Optional.of(unlocked);
			return Optional.of((Declaration)declaration.clone());
		}
		return Optional.of(declaration);
	}

	/**
	 * @param style assumed to be specified in the context of a (pseudo-)element
	 * @param context element for evaluating attr() and content() values against.
	 * @param mutable Whether the caller wishes to mutate the returned style object.
	 */
	public static SimpleInlineStyle parseSimpleInlineStyle(String style, Element context, boolean mutable) {
		BrailleCssStyle s = BrailleCssStyle.of(style, Context.ELEMENT);
		// evaluate attr() and content() values in content and string-set properties
		if (context != null)
			s = s.evaluate(context);
		try {
			return s.asSimpleInlineStyle(mutable);
		} catch (UnsupportedOperationException e) {
			throw new IllegalArgumentException(e);
		}
	}

	final static Map<String,Declaration> declCache = CacheBuilder.newBuilder()
	                                                             .expireAfterAccess(60, TimeUnit.SECONDS)
	                                                             .<String,Declaration>build()
	                                                             .asMap();

	/**
	 * Declaration that caches itself when it is serialized.
	 *
	 * Clones are not cached.
	 */
	static class CachingDeclaration extends PropertyValue {

		private boolean disableCaching = false;
		private final PropertyValue declaration;

		CachingDeclaration(PropertyValue declaration) {
			super(declaration);
			this.declaration = declaration;
		}

		private String valueSerialized = null;
		private String serialized = null;

		@Override
		public String toString() {
			if (serialized == null)
				valueToString(); // this also sets serialized and updates cache
			return serialized;
		}

		// for use in BrailleCssSerializer.serializePropertyValue()
		String valueToString() {
			if (valueSerialized == null) {
				valueSerialized = BrailleCssSerializer.serializePropertyValue(declaration);
				serialized = getProperty() + ": " + valueSerialized;
				if (!disableCaching)
					declCache.put(serialized, this);
			} else {
				// access cache to keep entry longer in it
				if (!disableCaching)
					declCache.get(serialized);
			}
			return valueSerialized;
		}

		@Override
		public CachingDeclaration clone() {
			CachingDeclaration clone = (CachingDeclaration)super.clone();
			clone.disableCaching = true;
			return clone;
		}
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
			put(context, serializedStyle, null, false, style);
		}

		void put(Context context, String serializedStyle, ValidatedDeclarations parent, boolean concretizeInherit, BrailleCssStyle style) {
			CacheKey key = new CacheKey(context, serializedStyle, parent, concretizeInherit);
			cache.put(key, style);
		}

		BrailleCssStyle get(Context context, String serializedStyle) {
			return get(context, serializedStyle, null, false);
		}

		BrailleCssStyle get(Context context, String serializedStyle, ValidatedDeclarations parent, boolean concretizeInherit) {
			CacheKey key = new CacheKey(context, serializedStyle, parent, concretizeInherit);
			return cache.get(key);
		}

		private static class CacheKey implements Comparable<CacheKey> {
			private final Context context;
			private final String style;
			private final String parent;
			private final boolean concretizeInherit;
			public CacheKey(Context context, String style, ValidatedDeclarations parent, boolean concretizeInherit) {
				if (parent != null && context != Context.ELEMENT)
					throw new IllegalArgumentException();
				this.context = context;
				this.style = style;
				this.parent = parent != null ? BrailleCssSerializer.toString(parent) : null;
				this.concretizeInherit = concretizeInherit;
			}
			@Override
			public int compareTo(CacheKey that) {
				int i = this.context.compareTo(that.context);
				if (i != 0)
					return i;
				i = this.style.compareTo(that.style);
				if (i != 0)
					return i;
				if (this.parent == null)
					return that.parent == null ? 0 : -1;
				else if (that.parent == null)
					return 1;
				i = this.parent.compareTo(that.parent);
				if (i != 0)
					return i;
				if (this.concretizeInherit == that.concretizeInherit)
					return 0;
				else
					return this.concretizeInherit ? -1 : 1;
			}
			@Override
			public int hashCode() {
				final int prime = 31;
				int hash = 1;
				hash = prime * hash + context.hashCode();
				hash = prime * hash + style.hashCode();
				hash = prime * hash + (parent == null ? 0 : parent.hashCode());
				hash = prime * hash + Boolean.hashCode(concretizeInherit);
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
				if (!this.style.equals(that.style))
					return false;
				if (this.parent == null)
					return that.parent == null;
				else if (that.parent == null)
					return false;
				else if (!this.parent.equals(that.parent))
					return false;
				else
					return this.concretizeInherit == that.concretizeInherit;
			}
		}
	}

	static class DeepDeclarationTransformer extends SupportedBrailleCSS {

		public DeepDeclarationTransformer(boolean allowComponentProperties, boolean allowShorthandProperties) {
			super(allowComponentProperties, allowShorthandProperties);
		}

		@Override
		public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
			if ("text-transform".equalsIgnoreCase(d.getProperty())) {
				if (super.processTextTransform(d, properties, values)) {
					if (properties.get("text-transform") == TextTransform.list_values) {
						Term<?> value = values.get("text-transform");
						if (value instanceof TermList)
							values.put("text-transform", TextTransformList.of((TermList)value));
						else
							throw new IllegalStateException(); // should not happen
					}
					return true;
				} else
					return false;
			} else if ("content".equalsIgnoreCase(d.getProperty())) {
				if (super.processContent(d, properties, values)) {
					if (properties.get("content") == Content.content_list) {
						Term<?> value = values.get("content");
						if (value instanceof TermList) {
							ContentList l = ContentList.of((TermList)value, this);
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
							values.put("string-set", StringSetList.of((TermList)value, this));
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
