package org.daisy.pipeline.braille.css.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.google.common.cache.CacheBuilder;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.CounterIncrement;
import cz.vutbr.web.css.CSSProperty.CounterSet;
import cz.vutbr.web.css.CSSProperty.CounterReset;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermList;

import org.daisy.braille.css.BrailleCSSExtension;
import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSRuleFactory;
import org.daisy.braille.css.PropertyValue;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.common.spi.ServiceLoader;
import org.daisy.pipeline.braille.css.impl.ContentList.ContentFunction;
import org.daisy.pipeline.braille.css.TextStyleParser;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;

public abstract class BrailleCssParser implements TextStyleParser {

	private static final Logger logger = LoggerFactory.getLogger(BrailleCssParser.class);

	private static BrailleCssParser INSTANCE_WITH_EXTENSIONS = null;

	public static BrailleCssParser getInstance() {
		if (INSTANCE_WITH_EXTENSIONS == null) {
			List<BrailleCSSExtension> extensions = new ArrayList<>();
			try {
				Iterator<BrailleCSSExtension> i = getCSSExtensions();
					while (i.hasNext()) {
						try {
							BrailleCSSExtension extension = i.next();
							extensions.add(extension);
							logger.debug("Binding BrailleCSSExtension: {}", extension);
						} catch (Throwable e) {
							logger.error("Error while binding BrailleCSSExtension", e);
						}
					}
			} catch (Throwable e) {
				logger.error("Error while binding BrailleCSSExtension services", e);
			}
			boolean allowUnknownVendorExtensions = false;
			BrailleCSSParserFactory parserFactory = new BrailleCSSParserFactory(
				new BrailleCSSRuleFactory(extensions, allowUnknownVendorExtensions));
			Map<Context,SupportedBrailleCSS> supportedBrailleCSS = new HashMap<>();
			INSTANCE_WITH_EXTENSIONS = new BrailleCssParser() {{
				for (Context context : new Context[]{Context.ELEMENT, Context.PAGE, Context.VOLUME})
					supportedBrailleCSS.put(
						context,
						new DeepDeclarationTransformer(
							context, true, false, extensions, allowUnknownVendorExtensions));
			}
					public BrailleCSSParserFactory getBrailleCSSParserFactory() {
						return parserFactory;
					}
					public Optional<SupportedBrailleCSS> getSupportedBrailleCSS(Context context) {
						return Optional.ofNullable(supportedBrailleCSS.get(context));
					}
				};
		}
		return INSTANCE_WITH_EXTENSIONS;
	}

	abstract BrailleCSSParserFactory getBrailleCSSParserFactory();

	abstract Optional<SupportedBrailleCSS> getSupportedBrailleCSS(Context context);

	private SupportedBrailleCSS supportedBrailleCSS = null;

	public final boolean isSupportedCSSProperty(String propertyName) {
		if (supportedBrailleCSS == null) {
			Optional<SupportedBrailleCSS> o = getSupportedBrailleCSS(Context.ELEMENT);
			if (!o.isPresent())
				throw new IllegalStateException();
			supportedBrailleCSS = o.get();
		}
		return supportedBrailleCSS.isSupportedCSSProperty(propertyName);
	}

	/**
	 * The returned object is immutable.
	 */
	public BrailleCssStyle parseInlineStyle(String style, Context context) {
		if (context == null)
			throw new IllegalArgumentException();
		BrailleCssStyle s = cache.get(context, style);
		if (s == null) {
			// try if a declaration was cached
			if (context == Context.ELEMENT) {
				Declaration d = declCache.get(style);
				if (d != null)
					s = BrailleCssStyle.of(d);
			}
			if (s == null)
				s = BrailleCssStyle.of(this, context, style);
			cache.put(context, style, s);
		}
		return s;
	}

	/**
	 * Concretizes "inherit" even if the parent style is null or empty.
	 *
	 * The returned object is immutable.
	 */
	public BrailleCssStyle parseInlineStyle(String style, Context context, BrailleCssStyle parent) {
		if (context != Context.ELEMENT)
			throw new IllegalArgumentException();
		ParsedDeclarations parentDecls; {
			if (parent == null || parent.declarations == null)
				parentDecls = ParsedDeclarations.EMPTY;
			else if (!(parent.declarations instanceof ParsedDeclarations))
				throw new IllegalArgumentException();
			else
				parentDecls = (ParsedDeclarations)parent.declarations;
		}
		BrailleCssStyle s = cache.get(context, style, parentDecls, true);
		if (s == null) {
			s = parseInlineStyle(style, context);
			s = s.inheritFrom(parentDecls);
			cache.put(context, style, parentDecls, true, s);
		}
		return s;
	}

	/**
	 * Style assumed to be specified in the context of a (pseudo-)element.
	 *
	 * @param context element for evaluating attr() and content() values against.
	 * @param mutable Whether the caller wishes to mutate the returned declaration.
	 */
	public Optional<Declaration> parseDeclaration(String property, String value, Element context, boolean mutable) {
		String style = String.format("%s: %s", property, value);
		Declaration declaration = declCache.get(style);
		if (declaration == null) {
			declaration = parseInlineStyle(style, Context.ELEMENT).getDeclaration(property);
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
	public SimpleInlineStyle parseSimpleInlineStyle(String style, Element context, boolean mutable) {
		BrailleCssStyle s = parseInlineStyle(style, Context.ELEMENT);
		// evaluate attr() and content() values in content and string-set properties
		if (context != null)
			s = s.evaluate(context);
		try {
			return s.asSimpleInlineStyle(mutable);
		} catch (UnsupportedOperationException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Concretizes "inherit" even if the parent style is null or empty.
	 */
	public SimpleInlineStyle parseSimpleInlineStyle(String style,
	                                                Element context,
	                                                SimpleInlineStyle parent,
	                                                boolean mutable) {
		BrailleCssStyle s = parseInlineStyle(
			style, Context.ELEMENT, parent != null ? BrailleCssStyle.of(parent) : null);
		// evaluate attr() and content() values in content and string-set properties
		if (context != null)
			s = s.evaluate(context);
		try {
			return s.asSimpleInlineStyle(mutable);
		} catch (UnsupportedOperationException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override // TextStyleParser
	public SimpleInlineStyle parse(String style) {
		if (style == null) style = "";
		// clone because we make SimpleInlineStyle available and SimpleInlineStyle is mutable (and we want it to be)
		return parseSimpleInlineStyle(style, null, true);
	}

	@Override // TextStyleParser
	public SimpleInlineStyle parse(String style, SimpleInlineStyle parent) {
		if (style == null) style = "";
		// clone because we make SimpleInlineStyle available and SimpleInlineStyle is mutable (and we want it to be)
		return parseSimpleInlineStyle(style, null, parent, true);
	}

	private final Map<String,Declaration> declCache = CacheBuilder.newBuilder()
	                                                              .expireAfterAccess(60, TimeUnit.SECONDS)
	                                                              .<String,Declaration>build()
	                                                              .asMap();

	/* =================================================================== */
	/* The fields and classes below are used only from BrailleCssStyle but */
	/* we keep it in this class because it is all related with parsing.    */
	/* =================================================================== */

	/**
	 * {@link SimpleInlineStyle} that is part of a {@link BrailleCssStyle} and created
	 * by {@link BrailleCssParser}.
	 *
	 * The purpose of having this class instead of {@link SimpleInlineStyle} is:
	 *
	 * 1. to easier recognize styles that were created by {@link BrailleCssParser}.
	 *
	 * 2. to control by whom and when styles can be mutated. Objects are mutable, but should be regarded as
	 *    being immutable outside this class, unless the {@code locked} field was set to {@code false}.
	 */
	static class ParsedDeclarations extends SimpleInlineStyle {

		public static final ParsedDeclarations EMPTY = new ParsedDeclarations();
		private BrailleCssParser parser;
		private Context context;

		/**
		 * @param parser {@link BrailleCssParser} to be used to process declarations that are not {@link
		 *               ParsedDeclaration} instances. If non-{@code null}, assert that declarations that are
		 *               {@link ParsedDeclaration} were created using the given {@link BrailleCssParser}. If
		 *               {@code null}, assert that all {@link ParsedDeclaration} declarations were created with
		 *               the same {@link BrailleCssParser}.
		 */
		ParsedDeclarations(BrailleCssParser parser, Context context, Iterable<? extends Declaration> declarations) {
			super(declarations, null, parser != null ? parser.getSupportedBrailleCSS(context).get() : null);
			this.parser = parser;
			this.context = context;
			checkParserAndContext();
		}

		// for ParsedDeclarations.EMPTY
		private ParsedDeclarations() {
			super(null, null, null);
			parser = null;
			context = null;
		}

		/**
		 * Check that all declarations were parsed with the same parser and context,
		 * and attach parser and context to the Quadruple.
		 */
		private void checkParserAndContext() {
			for (Map.Entry<String,cz.vutbr.web.domassign.SingleMapNodeData.Quadruple> e : map.entrySet()) {
				ParsedDeclaration d = new ParsedDeclaration(parser, context, super.get(e.getKey()));
				if (parser == null)
					parser = d.getParser();
				if (context == null)
					context = d.getContext();
				e.setValue(d.quadruple);
			}
		}

		// This is a bit of a lazy implementation of the object's immutability, because it is never
		// actually enforced that the object is not mutated when it is "locked". It is difficult to
		// actually enforce it because a SimpleInlineStyle can be mutated in various ways:
		// declarations can be removed or values can be mutated. This approach is however safe
		// enough, because whenever the object is exposed to the outside (namely through the
		// TextStyleParser/CSSStyledText API), a copy is made. When exposed through the XPath API, no
		// copying is needed because the XPath API doesn't allow for mutation.

		// object should always be cloned before it is unlocked
		boolean locked = true;

		BrailleCssParser getParser() {
			return parser;
		}

		Context getContext() {
			return context;
		}

		// override only method of SimpleInlineStyle that creates new instances of PropertyValue
		@Override
		public PropertyValue get(String property) {
			PropertyValue d = super.get(property);
			if (d != null)
				d = new ParsedDeclaration(parser, context, d);
			return d;
		}

		public PropertyValue getOrDefault(String property) {
			PropertyValue d = get(property);
			if (d == null && parser != null) {
				SupportedBrailleCSS css = parser.getSupportedBrailleCSS(context).orElse(null);
				if (css != null)
					return new ParsedDeclaration(
						parser,
						context,
						property,
						css.getDefaultProperty(property),
						css.getDefaultValue(property),
						null);
			}
			return d;
		}

		@Override
		public ParsedDeclarations inheritFrom(NodeData parent) {
			if (!(parent instanceof ParsedDeclarations))
				throw new IllegalArgumentException();
			ParsedDeclarations r = (ParsedDeclarations)super.inheritFrom(parent);
			BrailleCssParser p = ((ParsedDeclarations)parent).parser;
			if (r.parser == null)
				r.parser = p;
			else if (p != null && r.parser != p)
				throw new IllegalArgumentException();
			Context c = ((ParsedDeclarations)parent).context;
			if (r.context == null)
				r.context = c;
			else if (c != null && r.context != c)
				throw new IllegalArgumentException();
			r.checkParserAndContext();
			if (!r.isEmpty()) {
				// Make sure that the resulting SimpleInlineStyle is not based on a parent and that it
				// is not concretized because that would result in an exception when inheritFrom() is
				// called on it. Note that concretize() does not have the same effect.
				// Also perform special inheritance of text-transform.
				List<PropertyValue> list = new ArrayList<>();
				r.forEach(list::add);
				ListIterator<PropertyValue> i = list.listIterator();
				while (i.hasNext()) {
					ParsedDeclaration v = (ParsedDeclaration)i.next();
					PropertyValue flat = null;
					if (!((ParsedDeclarations)parent).isEmpty() && v.getCSSProperty() instanceof TextTransform)
						for (PropertyValue vv : (ParsedDeclarations)parent)
							if (vv.getCSSProperty() instanceof TextTransform) {
								TextTransformList t = TextTransformList.of(v);
								t.inheritFrom(TextTransformList.of(vv)); // this mutates the value
								if (t != v.getValue())
									flat = new ParsedDeclaration(
										v.getParser(),
										v.getContext(),
										v.getProperty(),
										t.equalsAuto() ? TextTransform.AUTO
										               : t.equalsInitial() ? TextTransform.INITIAL
										                                   : t.equalsNone() ? TextTransform.NONE
										                                                    : TextTransform.list_values,
										t.equalsAuto() || t.equalsInitial() || t.equalsNone() ? null : t,
										v.getSourceDeclaration());
								break;
							}
					if (flat == null)
						flat = new ParsedDeclaration(
							v.getParser(),
							v.getContext(),
							v.getProperty(),
							v.getCSSProperty(),
							v.getValue(),
							v.getSourceDeclaration());
					i.set(flat);
				}
				r = new ParsedDeclarations(r.parser, r.context, list);
			}
			return r;
		}
	}

	/**
	 * Declaration with reference to used parser and parser context. May cache
	 * itself when it is serialized.
	 *
	 * Clones are never cached.
	 */
	static class ParsedDeclaration extends PropertyValue {

		private final BrailleCssParser parser;
		private final Context context;
		private final Quadruple quadruple; // to make propertyValue available outside this class
		private boolean enableCaching = false;

		/**
		 * @throws IllegalArgumentException if the used parser and parser context can
		 *                                  not be derived from the declaration.
		 */
		ParsedDeclaration(PropertyValue declaration) {
			this(null, null, declaration);
		}

		/**
		 * @param parser If {@code null}, parser is derived from declaration
		 * @param context If {@code null}, context is derived from declaration
		 */
		ParsedDeclaration(BrailleCssParser parser, Context context, PropertyValue declaration) {
			super(declaration);
			if (this.propertyValue instanceof Quadruple) {
				this.parser = ((Quadruple)this.propertyValue).getParser();
				this.context = ((Quadruple)this.propertyValue).getContext();
				if (parser != null && this.parser != parser)
					throw new IllegalArgumentException();
				if (context != null && this.context != context)
					throw new IllegalArgumentException();
			} else {
				if (parser == null || context == null)
					throw new IllegalArgumentException();
				this.parser = parser;
				this.context = context;
				this.propertyValue = new Quadruple(this.getProperty(),
				                                   this.getCSSProperty(),
				                                   this.getValue(),
				                                   this.getSourceDeclaration(),
				                                   this.getSupportedBrailleCSS());
			}
			this.quadruple = (Quadruple)this.propertyValue;
			this.enableCaching = (this.context == Context.ELEMENT);
		}

		ParsedDeclaration(BrailleCssParser parser,
		                  Context context,
		                  String propertyName,
		                  CSSProperty property,
		                  Term<?> value,
		                  Declaration sourceDeclaration) {
			this(parser, context, new PropertyValue(propertyName,
			                                        property,
			                                        value,
			                                        sourceDeclaration,
			                                        parser.getSupportedBrailleCSS(context).get()));
		}

		BrailleCssParser getParser() {
			return parser;
		}

		Context getContext() {
			return context;
		}

		@Override
		public ParsedDeclaration getDefault() {
			PropertyValue d = super.getDefault();
			if (d == null)
				return null;
			else if (d instanceof ParsedDeclaration)
				return (ParsedDeclaration)d;
			else
				return new ParsedDeclaration(parser, context, d);
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
				valueSerialized = BrailleCssSerializer.serializePropertyValue(new PropertyValue(this));
				serialized = getProperty() + ": " + valueSerialized;
				if (enableCaching)
					parser.declCache.put(serialized, this);
			} else {
				// access cache to keep entry longer in it
				if (enableCaching)
					parser.declCache.get(serialized);
			}
			return valueSerialized;
		}

		@Override
		public ParsedDeclaration clone() {
			ParsedDeclaration clone = (ParsedDeclaration)super.clone();
			// Clones are never cached because we assume that clones are made to expose a
			// declaration to the outside, where they may possibly be mutated. If a clone is made
			// for another purpose, such as for running evaluate() on it, caching might be
			// desirable. Luckily, evalutation is normally already done before a declaration is
			// serialized. An alternative would be to cache copies of the object every time
			// toString() is called, and to also make copies when objects are retreived from the
			// cache. This would allow objects to be mutated.
			clone.enableCaching = false;
			// forget serialization because object may be mutated after it has been cloned
			clone.valueSerialized = null;
			clone.serialized = null;
			return clone;
		}

		private class Quadruple extends cz.vutbr.web.domassign.SingleMapNodeData.Quadruple {
		
			private Quadruple(String propertyName,
			                  final CSSProperty property,
			                  final Term<?> value,
			                  final Declaration sourceDeclaration,
			                  SupportedBrailleCSS css) {
				super(css, propertyName);
				curProp = property;
				curValue = value;
				curSource = sourceDeclaration;
			}
		
			private BrailleCssParser getParser() {
				return ParsedDeclaration.this.getParser();
			}
		
			private Context getContext() {
				return ParsedDeclaration.this.getContext();
			}
		}
	}

	// used in BrailleCssSerializer
	final Cache cache = new Cache();

	class Cache {

		private final Map<CacheKey,BrailleCssStyle> cache = CacheBuilder.newBuilder()
		                                                                .expireAfterAccess(60, TimeUnit.SECONDS)
		                                                                .<CacheKey,BrailleCssStyle>build()
		                                                                .asMap();
		void put(Context context, String serializedStyle, BrailleCssStyle style) {
			put(context, serializedStyle, null, false, style);
		}

		void put(Context context, String serializedStyle, ParsedDeclarations parent, boolean concretizeInherit, BrailleCssStyle style) {
			if (context == null) return;
			CacheKey key = new CacheKey(context, serializedStyle, parent, concretizeInherit);
			cache.put(key, style);
		}

		BrailleCssStyle get(Context context, String serializedStyle) {
			return get(context, serializedStyle, null, false);
		}

		BrailleCssStyle get(Context context, String serializedStyle, ParsedDeclarations parent, boolean concretizeInherit) {
			if (context == null) return null;
			CacheKey key = new CacheKey(context, serializedStyle, parent, concretizeInherit);
			return cache.get(key);
		}
	}

	private static class CacheKey implements Comparable<CacheKey> {
		private final Context context;
		private final String style;
		private final String parent;
		private final boolean concretizeInherit;
		public CacheKey(Context context, String style, ParsedDeclarations parent, boolean concretizeInherit) {
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

	private class DeepDeclarationTransformer extends SupportedBrailleCSS {

		private final Context context;

		public DeepDeclarationTransformer(Context context,
		                                  boolean allowComponentProperties,
		                                  boolean allowShorthandProperties,
		                                  Collection<BrailleCSSExtension> extensions,
		                                  boolean allowUnknownVendorExtensions) {
			super(allowComponentProperties, allowShorthandProperties, extensions, allowUnknownVendorExtensions);
			this.context = context;
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
							ContentList l = ContentList.of(BrailleCssParser.this, extensions, context, (TermList)value);
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
							values.put("string-set", StringSetList.of(BrailleCssParser.this, context, (TermList)value));
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

	private static Iterator<BrailleCSSExtension> getCSSExtensions() {
		if (OSGiHelper.inOSGiContext())
			return OSGiHelper.getCSSExtensions();
		else
			return SPIHelper.getCSSExtensions();
	}

	// static nested class in order to delay class loading
	private static abstract class OSGiHelper {

		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}

		static Iterator<BrailleCSSExtension> getCSSExtensions() {
			return new Iterator<BrailleCSSExtension>() {
				private BundleContext bc = FrameworkUtil.getBundle(BrailleCssParser.class).getBundleContext();
				private ServiceReference[] refs;
				private int index = 0;  {
					try {
						refs = bc.getServiceReferences(BrailleCSSExtension.class.getName(), null);
					} catch (InvalidSyntaxException e) {
						throw new IllegalStateException(e); // should not happen
					}
				}
				public boolean hasNext() {
					return refs != null && index < refs.length;
				}
				public BrailleCSSExtension next() throws NoSuchElementException {
					if (!hasNext())
						throw new NoSuchElementException();
					return (BrailleCSSExtension)bc.getService(refs[index++]);
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}

	// static nested class in order to delay class loading
	private static abstract class SPIHelper {
		static Iterator<BrailleCSSExtension> getCSSExtensions() {
			return ServiceLoader.load(BrailleCSSExtension.class).iterator();
		}
	}
}
