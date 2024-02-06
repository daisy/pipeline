package org.daisy.pipeline.braille.css.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.Combinator;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Selector.SelectorPart;
import cz.vutbr.web.css.SourceLocator;
import cz.vutbr.web.css.Term;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeHyphenationResource;
import org.daisy.braille.css.InlineStyle.RuleRelativePage;
import org.daisy.braille.css.InlineStyle.RuleRelativeVolume;
import org.daisy.braille.css.PropertyValue;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.braille.css.RuleCounterStyle;
import org.daisy.braille.css.RuleHyphenationResource;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.braille.css.RuleVolume;
import org.daisy.braille.css.RuleVolumeArea;
import org.daisy.braille.css.VendorAtRule;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser.CachingDeclaration;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser.DeepDeclarationTransformer;
import org.daisy.pipeline.css.CounterStyle;

import org.w3c.dom.Element;

/**
 * Tree reprentation of a CSS style. The tree structure is based on Sass' nesting of selectors.
 *
 * This class is immutable
 */
public final class BrailleCssStyle implements Cloneable {

	// these fields need to be package private because they are used in BrailleCssSerializer and BrailleCssParser
	// note that even though the declarations are assumed to not change, we don't assume they are unmodifiable
	Iterable<? extends Declaration> declarations;
	SortedMap<String,BrailleCssStyle> nestedStyles; // sorted by key

	private final Context context;
	public final Object underlyingObject; // - CounterStyle
	                                      // - null

	/**
	 * {@link SimpleInlineStyle} that is part of a {@link BrailleCssStyle}.
	 *
	 * The purpose of having this class instead of {@link SimpleInlineStyle} is:
	 *
	 * 1. to recognize styles that were created within {@link BrailleCssStyle} itself.
	 *
	 * 2. to control by whom and when styles can be mutated. Objects are mutable, but should be regarded as
	 *    being immutable outside this class, unless the {@code locked} field was set to {@code false}.
	 */
	// also used in BrailleCssParser
	static class ValidatedDeclarations extends SimpleInlineStyle {
		private static final ValidatedDeclarations EMPTY = new ValidatedDeclarations(null);
		private ValidatedDeclarations(Iterable<PropertyValue> validatedDeclarations) {
			super(validatedDeclarations);
		}
		private ValidatedDeclarations(Iterable<? extends Declaration> declarations, SupportedBrailleCSS validator) {
			super(declarations, null, validator);
		}
		private boolean locked = true;
	}

	private BrailleCssStyle(Builder builder) {
		this.context = builder.context;
		this.underlyingObject = builder.underlyingObject;
		this.declarations = builder.declarations == null || builder.declarations.isEmpty()
			? null
			: builder.validate.isPresent()
				? new ValidatedDeclarations(builder.declarations.values(), builder.validate.get())
				: ImmutableList.copyOf(builder.declarations.values())
			;
		this.nestedStyles = builder.nestedStyles != null && !builder.nestedStyles.isEmpty()
			? ImmutableSortedMap.copyOfSorted(builder.buildNestedStyles())
			: null;
	}

	public boolean isEmpty() {
		return declarations == null && nestedStyles == null;
	}

	private boolean evaluated = false;

	/**
	 * Evaluate <code>attr()</code> and <code>content()</code> values in <code>content</code> and
	 * <code>string-set</code> properties.
	 */
	public BrailleCssStyle evaluate(Element context) {
		if (evaluated) return this;
		BrailleCssStyle copy = null;
		if (declarations != null) {
			Iterable<? extends Declaration> evaluatedDeclarations = evaluateDeclarations(context);
			if (copy == null && evaluatedDeclarations != declarations)
				copy = clone();
			if (copy != null) {
				copy.declarations = evaluatedDeclarations;
				copy.declarationMap = null;
			}
		}
		if (nestedStyles != null) {
			SortedMap<String,BrailleCssStyle> nestedStylesCopy = new TreeMap<>(sortSelectors);
			for (Map.Entry<String,BrailleCssStyle> e : nestedStyles.entrySet()) {
				BrailleCssStyle nestedStyle = e.getValue();
				BrailleCssStyle nestedStyleEvaluated = nestedStyle.evaluate(context);
				if (copy == null && nestedStyleEvaluated != nestedStyle)
					copy = clone();
				nestedStylesCopy.put(e.getKey(), nestedStyleEvaluated);
			}
			if (copy != null)
				copy.nestedStyles = nestedStylesCopy;
		}
		if (copy != null) {
			copy.serialized = null;
			copy.evaluated = true;
			return copy;
		} else
			return this;
	}

	private Iterable<? extends Declaration> evaluateDeclarations(Element context) {
		Iterable<? extends Declaration> copy = null;
		if (declarations != null)
			for (Declaration d : declarations) {
				if (d instanceof PropertyValue) {
					PropertyValue pv = (PropertyValue)d;
					CSSProperty p = pv.getCSSProperty();
					if (p == Content.content_list) {
						Term<?> v = pv.getValue();
						if (v instanceof ContentList) {
							// clone because evaluate() mutates the value
							if (copy == null)
								copy = copyDeclarations();
							// find value in cloned declarations
							for (Declaration dd : copy)
								if (((PropertyValue)dd).getCSSProperty() == Content.content_list) {
									((ContentList)((PropertyValue)dd).getValue()).evaluate(context);
									break; }
						} else
							throw new IllegalStateException(); // coding error
					} else if (p == StringSet.list_values) {
						Term<?> v = pv.getValue();
						if (v instanceof StringSetList) {
							// clone because evaluate() mutates the value
							if (copy == null)
								copy = copyDeclarations();
							// find value in cloned declarations
							for (Declaration dd : copy)
								if (((PropertyValue)dd).getCSSProperty() == StringSet.list_values) {
									((StringSetList)((PropertyValue)dd).getValue()).evaluate(context);
									break; }
						} else
							throw new IllegalStateException(); // coding error
					}
				}
			}
		if (copy != null)
			return copy;
		else
			return declarations;
	}

	// also used in BrailleCssParser
	static Declaration evaluateDeclaration(Declaration declaration, Element context) {
		if (!(declaration instanceof PropertyValue))
			return declaration;
		PropertyValue pv = (PropertyValue)declaration;
		PropertyValue copy = null;
		CSSProperty p = pv.getCSSProperty();
		if (p == Content.content_list) {
			if (pv.getValue() instanceof ContentList) {
				// clone because evaluate() mutates the value
				copy = (PropertyValue)declaration.clone();
				((ContentList)copy.getValue()).evaluate(context);
			} else
				throw new IllegalStateException(); // coding error
		} else if (p == StringSet.list_values) {
			if (pv.getValue() instanceof StringSetList) {
				// clone because evaluate() mutates the value
				copy = (PropertyValue)declaration.clone();
				((StringSetList)copy.getValue()).evaluate(context);
			} else
				throw new IllegalStateException(); // coding error
		}
		if (copy != null)
			return copy;
		else
			return declaration;
	}

	/**
	 * @return a deep copy of the {@code declarations} field
	 */
	private Iterable<? extends Declaration> copyDeclarations() {
		if (declarations instanceof ValidatedDeclarations)
			// make sure that declarations field stays a ValidatedDeclarations object because
			// it is assumed throughout the code
			return (ValidatedDeclarations)((ValidatedDeclarations)declarations).clone();
		else {
			List<Declaration> declarationsCopy = new ArrayList<>();
			for (Declaration dd : declarations)
				declarationsCopy.add((Declaration)dd.clone());
			return ImmutableList.copyOf(declarationsCopy);
		}
	}

	private Map<String,Declaration> declarationMap = null;

	private Map<String,Declaration> getDeclarationMap() {
		if (declarations != null) {
			synchronized (this) {
				if (declarationMap == null) {
					declarationMap = new TreeMap<>();
					for (Declaration d : declarations)
						if (context == Context.ELEMENT) { // context not set if caching is not allowed
							if (!(d instanceof PropertyValue))
								throw new IllegalStateException(); // coding error
							declarationMap.put(d.getProperty(), new CachingDeclaration((PropertyValue)d));
						} else
							declarationMap.put(d.getProperty(), d);
				}
			}
		}
		return declarationMap;
	}

	public Iterable<String> getPropertyNames() {
		if (declarations != null)
			return getDeclarationMap().keySet();
		else
			return Collections.emptyList();
	}

	/**
	 * Caller must guarantee that the {@link Declaration} objects will not be modified.
	 */
	public Iterable<Declaration> getDeclarations() {
		if (declarations != null)
			return getDeclarationMap().values();
		else
			return Collections.emptyList();
	}

	public Iterable<String> getSelectors() {
		if (nestedStyles != null)
			return nestedStyles.keySet();
		else
			return Collections.emptyList();
	}

	private List<BrailleCssStyle> rules = null;

	public Iterable<BrailleCssStyle> getRules() {
		if (nestedStyles != null) {
			synchronized (this) {
				if (rules == null) {
					rules = new ArrayList<>();
					for (Map.Entry<String,BrailleCssStyle> e : nestedStyles.entrySet()) {
						String selector = e.getKey();
						rules.add(new Builder(context).add(selector, e.getValue()).build());
					}
				}
			}
			return rules;
		} else
			return Collections.emptyList();
	}

	/**
	 * Return the style as a {@link SimpleInlineStyle} object.
	 *
	 * @param mutable Whether the caller wishes to mutate the returned object.
	 * @throws UnsupportedOperationException if this is not a simple inline style
	 */
	public SimpleInlineStyle asSimpleInlineStyle(boolean mutable) {
		if (isEmpty())
			return SimpleInlineStyle.EMPTY;
		else if (nestedStyles != null
		         || context != Context.ELEMENT)
			throw new UnsupportedOperationException("not a simple inline style");
		else {
			if (!(declarations instanceof ValidatedDeclarations))
				throw new IllegalStateException(); // coding error
			ValidatedDeclarations s = (ValidatedDeclarations)declarations;
			if (mutable) {
				s = (ValidatedDeclarations)s.clone(); // make a deep copy
				for (Declaration d : s) {
					if (d instanceof PropertyValue) {
						PropertyValue pv = (PropertyValue)d;
						CSSProperty p = pv.getCSSProperty();
						if (p == TextTransform.list_values) {
							Term<?> v = pv.getValue();
							if (v instanceof TextTransformList)
								((TextTransformList)v).locked = false;
							break;
						}
					}
				}
				// mark as mutable
				s.locked = false;
			}
			return s;
		}
	}

	// used in BrailleCssParser
	static Declaration unlockDeclaration(Declaration declaration) {
		if (!(declaration instanceof PropertyValue))
			return declaration;
		PropertyValue pv = (PropertyValue)declaration;
		if (pv.getCSSProperty() == TextTransform.list_values) {
			if (pv.getValue() instanceof TextTransformList) {
				// clone because we're going to make the value mutable
				pv = (PropertyValue)pv.clone();
				((TextTransformList)pv.getValue()).locked = false;
			} else
				throw new IllegalStateException(); // coding error
		}
		return pv;
	}

	/**
	 * Get the declaration for the given property name.
	 *
	 * Caller must guarantee that the object will not be modified.
	 */
	public Declaration getDeclaration(String propertyName) {
		return getDeclaration(propertyName, false);
	}

	/**
	 * @param includeInitial Whether to include the initial value if the style does not contain the property.
	 */
	public Declaration getDeclaration(String propertyName, boolean includeInitial) {
		if (declarations != null) {
			Declaration d = getDeclarationMap().get(propertyName);
			if (d != null)
				return d;
		}
		if (includeInitial)
			return getDefaultPropertyValue(propertyName);
		return null;
	}

	/**
	 * Get the nested style for the given selector.
	 */
	public BrailleCssStyle getNestedStyle(String selector) {
		if (nestedStyles != null)
			return nestedStyles.get(selector);
		else
			return null;
	}

	/**
	 * Remove declaration if key is a property name, or nested style if key is a selector.
	 */
	public BrailleCssStyle remove(String key) {
		if (getDeclaration(key) != null ||
			nestedStyles != null && nestedStyles.containsKey(key))
			return new Builder(this).remove(key).build();
		else
			return this;
	}

	/**
	 * Add other styles to this style. Properties are overwritten by properties declared in
	 * following style items.
	 *
	 * @param styles objects must be of type {@link BrailleCssStyle} or {@link Declaration}.
	 */
	public BrailleCssStyle add(Iterator<Object> styles) {
		Builder b = null;
		while (styles.hasNext()) {
			Object s = styles.next();
			if (s instanceof BrailleCssStyle) {
				if (((BrailleCssStyle)s).isEmpty())
					continue;
				if (b == null) b = new Builder(this);
				b.add((BrailleCssStyle)s);
			} else if (s instanceof Declaration) {
				if (b == null) b = new Builder(this);
				Declaration d = (Declaration)s;
				if (d instanceof PropertyValue)
					b.add(d, ((PropertyValue)d).getSupportedBrailleCSS());
				else
					b.add(d, Optional.empty());
			} else
				throw new IllegalArgumentException();
		}
		if (b != null)
			return b.build();
		else
			return this;
	}

	/**
	 * Add a declaration. If a declaration for the same property already exists, it is overwritten.
	 */
	public BrailleCssStyle add(Declaration declaration) {
		Builder b = new Builder(this);
		if (declaration instanceof PropertyValue)
			b.add(declaration, ((PropertyValue)declaration).getSupportedBrailleCSS());
		else
			b.add(declaration, Optional.empty());
		return b.build();
	}

	/**
	 * Add a rule. If a rule with the same selector already exists, the rules are merged. Properties
	 * in the existing rule are overwritten by properties in the new rule.
	 */
	public BrailleCssStyle add(String key, BrailleCssStyle nestedStyle) {
		return new Builder(this).add(key, nestedStyle).build();
	}

	@Override
	public BrailleCssStyle clone() {
		try {
			return (BrailleCssStyle)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("coding error");
		}
	}

	private String serialized = null;

	@Override
	public String toString() {
		return toString(null);
	}

	/**
	 * @param relativeTo If not <code>null</code>, include only those declarations that are needed
	 *                   to reconstruct the style with <code>relativeTo</code> as the parent
	 *                   style. Relativizes even if the parent style is empty.
	 */
	public String toString(BrailleCssStyle relativeTo) {
		if (relativeTo != null) {
			if (relativeTo.declarations != null && !(relativeTo.declarations instanceof ValidatedDeclarations))
				throw new IllegalArgumentException();
			String s = relativize((ValidatedDeclarations)relativeTo.declarations).build().toString();
			if (context != null) // context not set if caching is not allowed
				BrailleCssParser.cache.put(context,
				                           s,
				                           relativeTo.declarations != null
				                               ? (ValidatedDeclarations)relativeTo.declarations
				                               : ValidatedDeclarations.EMPTY,
				                           true,
				                           this);
			return s;
		}
		if (serialized == null) {
			serialized = toString(this, null);
			// cache
			if (context != null) // context not set if caching is not allowed
				BrailleCssParser.cache.put(context, serialized, this);
		} else {
			// access cache to keep entry longer in it
			if (context != null)
				BrailleCssParser.cache.get(context, serialized);
		}
		return serialized;
	}

	private Builder relativize(ValidatedDeclarations base) {
		if (declarations != null && !(declarations instanceof ValidatedDeclarations))
			throw new IllegalArgumentException();
		Builder relative = new Builder(this);
		if (base != null)
			for (PropertyValue p : base) {
				if (p.getCSSProperty().inherited()) {
					PropertyValue pp = (PropertyValue)getDeclaration(p.getProperty());
					if (pp != null) {
						if (equal(p, pp))
							relative.remove(p.getProperty());
					} else {
						PropertyValue def = p.getDefault();
						if (def != null && !equalIgnoreSource(p, def))
							relative.add(def, def.getSupportedBrailleCSS());
					}
				}
			}
		// omit properties that have the default value and are not inherited or don't exist in the parent style
		if (declarations != null)
			for (Declaration d : getDeclarationMap().values()) {
				PropertyValue p = (PropertyValue)d;
				PropertyValue def = p.getDefault();
				if ((!p.getCSSProperty().inherited() || base == null || base.getProperty(p.getProperty()) == null) && equal(p, def))
					relative.remove(p.getProperty());
			}
		// relativize nested styles
		BrailleCssStyle main = null;
		for (String sel : getSelectors()) {
			if (sel.startsWith("@"))
				// skip at-rules
				continue;
			BrailleCssStyle nested = getNestedStyle(sel);
			if (sel.equals("&::before") ||
			    sel.equals("&::after") ||
			    (sel.startsWith("&:") && !sel.startsWith("&::"))) {
				// pseudo-classes and ::before and ::after pseudo-elements inherit from main style
				if (relative.declarations != null && !relative.declarations.isEmpty()) {
					relative.remove(sel);
					if (main == null) main = relative.build();
					if (main.declarations != null && !(main.declarations instanceof ValidatedDeclarations))
						throw new IllegalStateException();
					relative.add(sel, nested.relativize((ValidatedDeclarations)main.declarations));
				}
			} else {
				relative.remove(sel);
				relative.add(sel, nested.relativize(base));
			}
		}
		return relative;
	}

	private static String toString(BrailleCssStyle style, String base) {
		StringBuilder b = new StringBuilder();
		StringBuilder rel = new StringBuilder();
		if (style.declarations != null)
			b.append(BrailleCssSerializer.serializeDeclarationList(style.declarations));
		if (style.nestedStyles != null)
			for (Map.Entry<String,BrailleCssStyle> e : style.nestedStyles.entrySet()) {
				if (base != null && e.getKey().startsWith("&")) {
					if (rel.length() > 0) rel.append(" ");
					rel.append(toString(e.getValue(), base + e.getKey().substring(1)));
				} else {
					if (b.length() > 0) {
						if (b.charAt(b.length() - 1) != '}') b.append(";");
						b.append(" ");
					}
					b.append(toString(e.getValue(), e.getKey()));
				}
			}
		if (base != null && b.length() > 0) {
			b.insert(0, base + " { ");
			b.append(" }");
		}
		if (rel.length() > 0) {
			if (b.length() > 0) {
				if (b.charAt(b.length() - 1) != '}') b.append(";");
				b.append(" ");
			}
			b.append(rel);
		}
		return b.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof BrailleCssStyle))
			return false;
		BrailleCssStyle that = (BrailleCssStyle)other;
		if (context != that.context)
			return false;
		// using toString() for efficiency, because toString() is memoized
		return toString().equals(that.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	private static boolean equal(PropertyValue a, PropertyValue b) {
		return equal(a, b, false);
	}

	private static boolean equalIgnoreSource(PropertyValue a, PropertyValue b) {
		return equal(a, b, true);
	}

	private static boolean equal(PropertyValue a, PropertyValue b, boolean ignoreSource) {
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		else {
			if (a.getCSSProperty() != b.getCSSProperty())
				return false;
			Term<?> va = a.getValue();
			Term<?> vb = b.getValue();
			if (!equal(va != null ? BrailleCssSerializer.toString(va) : null,
			           vb != null ? BrailleCssSerializer.toString(vb) : null))
				return false;
			if (!ignoreSource && !equal(a.getSource(), b.getSource()))
				return false;
		}
		return true;
	}

	private static boolean equal(SourceLocator a, SourceLocator b) {
		if (a != null && a.getURL() == null)
			a = null;
		if (b != null && b.getURL() == null)
			b = null;
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		else if (!equal(a.getURL(), b.getURL()))
			return false;
		else if (a.getLineNumber() != b.getLineNumber())
			return false;
		else if (a.getColumnNumber() != b.getColumnNumber())
			return false;
		else
			return true;
	}

	private static boolean equal(Object a, Object b) {
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		else
			return a.equals(b);
	}

	// cache initial property values (initial according DEFAULT_VALIDATOR)
	private final static Map<String,PropertyValue> defaultPropertyValues = new TreeMap<>();

	private static PropertyValue getDefaultPropertyValue(String name) {
		synchronized (defaultPropertyValues) {
			PropertyValue def = defaultPropertyValues.get(name);
			if (def == null) {
				def = new PropertyValue(
					name,
					DEFAULT_VALIDATOR.getDefaultProperty(name),
					DEFAULT_VALIDATOR.getDefaultValue(name),
					null,
					DEFAULT_VALIDATOR) {};
				defaultPropertyValues.put(name, def);
			}
			return def;
		}
	}

	private static PropertyValue getDefaultPropertyValue(PropertyValue p) {
		PropertyValue def = p.getDefault();
		if (def != null)
			return def;
		else
			return getDefaultPropertyValue(p.getProperty());
	}

	private static class Builder {

		private final Context context;
		private final Object underlyingObject;
		/**
		 * Whether to validate declarations (not of nested styles)
		 */
		private Optional<SupportedBrailleCSS> validate;
		private Map<String,Declaration> declarations;
		private Map<String,Object> nestedStyles; // values are instances of Builder or BrailleCssStyle

		/**
		 * @param context <code>null</code> means neither of { ELEMENT, PAGE, VOLUME, TEXT_TRANSFORM, COUNTER_STYLE, VENDOR_RULE }
		 */
		private Builder(Context context) {
			this(context, null);
		}

		private Builder(Context context, Object underlyingObject) {
			this.context = context;
			this.underlyingObject = underlyingObject;
		}

		private Builder() {
			this(null, null);
		}

		private Builder(BrailleCssStyle style) {
			this(style.context, style.underlyingObject);
			add(style);
		}

		/**
		 * @param declaration assumed to not change
		 */
		private Builder add(Declaration declaration, SupportedBrailleCSS validate) {
			return add(declaration, Optional.of(validate));
		}

		private Builder add(Declaration declaration, Optional<SupportedBrailleCSS> validate) {
			if (declaration != null) {
				if (this.declarations == null) {
					this.declarations = new TreeMap<>();
					this.validate = validate;
				} else if (!this.validate.equals(validate))
					throw new IllegalArgumentException();
				declarations.put(declaration.getProperty(), declaration);
			}
			return this;
		}

		private Builder add(String selector, Object nestedStyle) {
			if (!(nestedStyle instanceof BrailleCssStyle || nestedStyle instanceof Builder))
				throw new IllegalArgumentException();
			if (this.nestedStyles == null) this.nestedStyles = new TreeMap<String,Object>();
			if (selector == null) {
				if (nestedStyle instanceof BrailleCssStyle)
					add((BrailleCssStyle)nestedStyle);
				else
					add((Builder)nestedStyle);
			} else if (this.nestedStyles.containsKey(selector)) {
				Object s = this.nestedStyles.get(selector);
				if (s instanceof Builder) {
					if (nestedStyle instanceof BrailleCssStyle)
						((Builder)s).add((BrailleCssStyle)nestedStyle);
					else
						((Builder)s).add((Builder)nestedStyle);
				} else {
					if (nestedStyle instanceof BrailleCssStyle)
						this.nestedStyles.put(selector, new Builder((BrailleCssStyle)s).add((BrailleCssStyle)nestedStyle));
					else
						this.nestedStyles.put(selector, new Builder((BrailleCssStyle)s).add((Builder)nestedStyle));
				}
			} else
				this.nestedStyles.put(selector, nestedStyle);
			return this;
		}

		private Builder add(BrailleCssStyle style) {
			if (style.declarations != null)
				for (Declaration d : style.declarations) {
					if (d instanceof PropertyValue)
						add(d, ((PropertyValue)d).getSupportedBrailleCSS());
					else
						add(d, Optional.empty()); }
			if (style.nestedStyles != null)
				for (Map.Entry<String,BrailleCssStyle> e : style.nestedStyles.entrySet())
					add(e.getKey(), e.getValue());
			return this;
		}

		private Builder add(Builder style) {
			if (style.declarations != null)
				for (Declaration d : style.declarations.values())
					add(d, style.validate);
			if (style.nestedStyles != null)
				for (Map.Entry<String,Object> e : style.nestedStyles.entrySet()) {
					Object s = e.getValue();
					if (s instanceof Builder)
						add(e.getKey(), (Builder)s);
					else
						add(e.getKey(), (BrailleCssStyle)s);
				}
			return this;
		}

		/**
		 * @param key property name or selector
		 */
		private Builder remove(String key) {
			if (this.declarations != null && this.declarations.remove(key) != null)
				;
			else if (this.nestedStyles != null)
				this.nestedStyles.remove(key);
			return this;
		}

		private BrailleCssStyle build() {
			return new BrailleCssStyle(this);
		}

		private SortedMap<String,BrailleCssStyle> buildNestedStyles() {
			if (nestedStyles == null)
				return null;
			SortedMap<String,BrailleCssStyle> map = null;
			for (Map.Entry<String,Object> e : nestedStyles.entrySet()) {
				Object v = e.getValue();
				BrailleCssStyle s = v instanceof Builder ? ((Builder)v).build() : (BrailleCssStyle)v;
				if (!s.isEmpty()) {
					if (map == null)
						map = new TreeMap<String,BrailleCssStyle>(sortSelectors);
					map.put(e.getKey(), s);
				}
			}
			return map;
		}
	}

	/**
	 * @param declarations assumed to not change
	 */
	public static BrailleCssStyle of(SimpleInlineStyle declarations, Context context) {
		BrailleCssStyle style = new Builder(context).build();
		style.declarations = declarations instanceof ValidatedDeclarations
			? declarations
			// assuming that declarations originate from a ValidatedDeclarations
			: new ValidatedDeclarations(declarations); // shallow copy
		return style;
	}

	/**
	 * @param declaration assumed to not change
	 */
	public static BrailleCssStyle of(Declaration declaration, Context context) {
		Builder style = new Builder(context);
		if (declaration instanceof PropertyValue)
			style.add(declaration, ((PropertyValue)declaration).getSupportedBrailleCSS());
		else
			style.add(declaration,
			          Optional.ofNullable(
			              (context == Context.ELEMENT ||
			               context == Context.PAGE ||
			               context == Context.VOLUME) ? DEFAULT_VALIDATOR : null));
		return style.build();
	}

	/**
	 * @param page assumed to not change
	 */
	private static BrailleCssStyle of(RulePage page) {
		return of(page, false);
	}

	private static BrailleCssStyle of(RuleRelativePage page) {
		return of(page.asRulePage(), true);
	}

	private static BrailleCssStyle of(RulePage page, boolean relative) {
		return of(page, relative, DEFAULT_VALIDATOR);
	}

	/**
	 * @param supportedCss {@link SupportedBrailleCSS} for declarations inside page and margin area rules.
	 */
	// used in BrailleCssSerializer.toString(RulePage, SupportedBrailleCSS)
	static BrailleCssStyle of(RulePage page, SupportedBrailleCSS supportedCss) {
		return of(page, false, supportedCss);
	}

	private static BrailleCssStyle of(RulePage page, boolean relative, SupportedBrailleCSS validate) {
		// assumed to be anonymous page
		Builder style = new Builder(validate == DEFAULT_VALIDATOR ? Context.PAGE : null);
		for (Rule<?> r : page)
			if (r instanceof Declaration)
				style.add((Declaration)r, validate);
			else if (r instanceof RuleMargin) {
				Builder margin = new Builder();
				for (Declaration d : (RuleMargin)r)
					margin.add(d, validate);
				style.add("@" + ((RuleMargin)r).getMarginArea(), margin);
			} else
				throw new RuntimeException("coding error");
		String pseudo = page.getPseudo();
		Builder relativeRule = (pseudo == null)
			? style
			: new Builder(validate == DEFAULT_VALIDATOR ? Context.PAGE : null).add("&:" + pseudo, style);
		if (relative)
			return relativeRule.build();
		else
			return new Builder().add("@page", relativeRule).build();
	}

	/**
	 * @param volumeArea assumed to not change
	 */
	private static BrailleCssStyle of(RuleVolumeArea volumeArea) {
		Builder style = new Builder();
		for (Rule<?> r : volumeArea)
			if (r instanceof Declaration)
				style.add((Declaration)r, DEFAULT_VALIDATOR);
			else if (r instanceof RulePage)
				style.add(of((RulePage)r));
			else
				throw new RuntimeException("coding error");
		return new Builder().add("@" + volumeArea.getVolumeArea().value, style).build();
	}

	/**
	 * @param volume assumed to not change
	 */
	private static BrailleCssStyle of(RuleVolume volume) {
		return of(volume, false);
	}

	private static BrailleCssStyle of(RuleRelativeVolume volume) {
		return of(volume.asRuleVolume(), true);
	}

	private static BrailleCssStyle of(RuleVolume volume, boolean relative) {
		Builder style = new Builder(Context.VOLUME);
		for (Rule<?> r : volume)
			if (r instanceof Declaration)
				style.add((Declaration)r, DEFAULT_VALIDATOR);
			else if (r instanceof RuleVolumeArea)
				style.add(of((RuleVolumeArea)r));
			else
				throw new RuntimeException("coding error");
		String pseudo = volume.getPseudo();
		Builder relativeRule = (pseudo == null)
			? style
			: new Builder(Context.VOLUME).add("&:" + pseudo, style);
		if (relative)
			return relativeRule.build();
		else
			return new Builder().add("@volume", relativeRule).build();
	}

	/**
	 * @param hyphenationResource assumed to not change
	 */
	private static BrailleCssStyle of(RuleHyphenationResource hyphenationResource) {
		return of(hyphenationResource, false);
	}

	private static BrailleCssStyle of(RuleRelativeHyphenationResource hyphenationResource) {
		return of(hyphenationResource.asRuleHyphenationResource(), true);
	}

	private static BrailleCssStyle of(RuleHyphenationResource hyphenationResource, boolean relative) {
		Builder style = new Builder(Context.HYPHENATION_RESOURCE);
		for (Declaration d : hyphenationResource)
			style.add(d, Optional.empty());
		Builder relativeRule = new Builder(Context.HYPHENATION_RESOURCE)
			.add("&:lang(" + BrailleCssSerializer.serializeLanguageRanges(hyphenationResource.getLanguageRanges()) + ")", style);
		if (relative)
			return relativeRule.build();
		else
			return new Builder().add("@hyphenation-resource", relativeRule).build();
	}

	/**
	 * @param rule assumed to not change
	 */
	private static BrailleCssStyle of(VendorAtRule<? extends Rule<?>> rule) {
		Builder style = new Builder();
		for (Rule<?> r : rule)
			if (r instanceof Declaration)
				style.add((Declaration)r, Optional.empty());
			else if (r instanceof VendorAtRule)
				style.add("@" + ((VendorAtRule)r).getName(), of((VendorAtRule<? extends Rule<?>>)r));
			else
				throw new RuntimeException("coding error");
		return style.build();
	}

	/**
	 * @param inlineStyle assumed to not change
	 */
	// used in BrailleCssSerializer
	static BrailleCssStyle of(InlineStyle inlineStyle, Context context) {
		Builder style = new Builder(context);
		Map<String,RuleCounterStyle> counterStyleRules = null;
		for (RuleBlock<?> rule : inlineStyle) {
			if (rule instanceof RuleMainBlock)
				// Note that the declarations have not been transformed by SupportedBrailleCSS yet.
				// This will be done when build() is called.
				for (Declaration d : (RuleMainBlock)rule)
					style.add(d, Optional.ofNullable(
					                 (context == Context.ELEMENT ||
					                  context == Context.PAGE ||
					                  context == Context.VOLUME) ? DEFAULT_VALIDATOR : null));
			else if (rule instanceof RuleRelativeBlock) {
				String[] selector = serializeSelector(((RuleRelativeBlock)rule).getSelector());
				if (context == Context.COUNTER_STYLE && selector.length == 1 && selector[0].startsWith("& ")) {
					String name = selector[0].substring(2);
					RuleCounterStyle counterStyle = new RuleCounterStyle(name); {
						for (Rule<?> r : (RuleRelativeBlock)rule)
							if (!(r instanceof Declaration))
								throw new RuntimeException(); // @page can not occur inside @counter-style
						counterStyle.replaceAll((RuleBlock<Declaration>)rule); }
					if (counterStyleRules == null)
						counterStyleRules = new HashMap<>();
					counterStyleRules.put(name, counterStyle);
				} else {
					Builder decls = new Builder(); {
						for (Rule<?> r : (RuleRelativeBlock)rule)
							if (r instanceof Declaration)
								decls.add((Declaration)r,
								          Optional.ofNullable(context == Context.ELEMENT ? DEFAULT_VALIDATOR : null));
							else if (r instanceof RulePage)
								decls.add(of((RulePage)r));
							else
								throw new RuntimeException("coding error");
					}
					if (selector.length > 0) {
						for (int i = selector.length - 1; i > 0; i--)
							decls = new Builder().add(selector[i], decls);
						style.add(selector[0], decls);
					}
				}
			} else if (rule instanceof RulePage)
				style.add(of((RulePage)rule));
			else if (rule instanceof RuleVolume)
				style.add(of((RuleVolume)rule));
			else if (rule instanceof RuleTextTransform) {
				String name = ((RuleTextTransform)rule).getName();
				Builder textTransform = new Builder(Context.TEXT_TRANSFORM);
				for (Declaration d : (RuleTextTransform)rule)
					textTransform.add(d, Optional.empty());
				if (name == null)
					style.add("@text-transform", textTransform);
				else
					style.add("@text-transform", new Builder().add("& " + name, textTransform)); }
			else if (rule instanceof RuleHyphenationResource)
				style.add(of((RuleHyphenationResource)rule));
			else if (rule instanceof RuleCounterStyle) {
				if (counterStyleRules == null)
					counterStyleRules = new HashMap<>();
				counterStyleRules.put(((RuleCounterStyle)rule).getName(), (RuleCounterStyle)rule); }
			else if (rule instanceof RuleMargin) {
				Builder margin = new Builder();
				for (Declaration d : (RuleMargin)rule)
					margin.add(d, DEFAULT_VALIDATOR);
				style.add("@" + ((RuleMargin)rule).getMarginArea(), margin);
			} else if (rule instanceof RuleVolumeArea)
				style.add(of((RuleVolumeArea)rule));
			else if (rule instanceof RuleRelativePage)
				style.add(of((RuleRelativePage)rule));
			else if (rule instanceof RuleRelativeVolume)
				style.add(of((RuleRelativeVolume)rule));
			else if (rule instanceof RuleRelativeHyphenationResource)
				style.add(of((RuleRelativeHyphenationResource)rule));
			else if (rule instanceof VendorAtRule)
				style.add("@" + ((VendorAtRule)rule).getName(), of((VendorAtRule<? extends Rule<?>>)rule));
			else
				throw new RuntimeException("coding error");
		}
		if (counterStyleRules != null) {
			for (Map.Entry<String,CounterStyle> e : CounterStyle.parseCounterStyleRules(counterStyleRules.values()).entrySet()) {
				Builder counterStyle = new Builder(Context.COUNTER_STYLE, e.getValue());
				for (Declaration d : counterStyleRules.get(e.getKey()))
					counterStyle.add(d, Optional.empty());
				if (context == Context.COUNTER_STYLE)
					style.add("& " + e.getKey(), counterStyle);
				else
					style.add("@counter-style", new Builder().add("& " + e.getKey(), counterStyle));
			}
		}
		return style.build();
	}

	public static BrailleCssStyle of(String inlineStyle, Context context) {
		if (context == null)
			throw new IllegalArgumentException();
		BrailleCssStyle s = BrailleCssParser.cache.get(context, inlineStyle);
		if (s == null) {
			// try if a declaration was cached
			if (context == Context.ELEMENT) {
				Declaration d = BrailleCssParser.declCache.get(inlineStyle);
				if (d != null)
					s = of(d, context);
			}
			if (s == null)
				s = of(new InlineStyle(inlineStyle, context), context);
			BrailleCssParser.cache.put(context, inlineStyle, s);
		}
		return s;
	}

	/**
	 * Concretizes "inherit" even if the parent style is null or empty.
	 */
	public static BrailleCssStyle of(String inlineStyle, Context context, BrailleCssStyle parent) {
		if (context != Context.ELEMENT)
			throw new IllegalArgumentException();
		ValidatedDeclarations parentDecls; {
			if (parent == null || parent.declarations == null)
				parentDecls = ValidatedDeclarations.EMPTY;
			else if (!(parent.declarations instanceof ValidatedDeclarations))
				throw new IllegalArgumentException();
			else
				parentDecls = (ValidatedDeclarations)parent.declarations;
		}
		BrailleCssStyle s = BrailleCssParser.cache.get(context, inlineStyle, parentDecls, true);
		if (s == null) {
			s = of(inlineStyle, context);
			s = s.inheritFrom(parentDecls);
			BrailleCssParser.cache.put(context, inlineStyle, parentDecls, true, s);
		}
		return s;
	}

	private BrailleCssStyle inheritFrom(ValidatedDeclarations base) {
		if (declarations != null && !(declarations instanceof ValidatedDeclarations))
			throw new IllegalStateException();
		ValidatedDeclarations decls = declarations != null
			? (ValidatedDeclarations)declarations
			: ValidatedDeclarations.EMPTY;
		if (!base.isEmpty())
			decls = (ValidatedDeclarations)decls.inheritFrom(base);
		decls = (ValidatedDeclarations)decls.concretize();
		if (!decls.isEmpty()) {
			// Make sure that the resulting SimpleInlineStyle is not based on a parent and that it
			// is not concretized because that would result in an exception when inheritFrom() is
			// called on it. Note that concretize() does not have the same effect.
			// Also perform special inheritance of text-transform.
			List<PropertyValue> list = new ArrayList<>();
			decls.forEach(list::add);
			ListIterator<PropertyValue> i = list.listIterator();
			while (i.hasNext()) {
				PropertyValue v = i.next();
				PropertyValue flat = null;
				if (!base.isEmpty() && v.getCSSProperty() instanceof TextTransform)
					for (PropertyValue vv : base)
						if (vv.getCSSProperty() instanceof TextTransform) {
							TextTransformList t = TextTransformList.of(v);
							t.inheritFrom(TextTransformList.of(vv)); // this mutates the value
							if (t != v.getValue())
								flat = new PropertyValue(
									v.getProperty(),
									t.equalsAuto() ? TextTransform.AUTO
									               : t.equalsInitial() ? TextTransform.INITIAL
									                                   : t.equalsNone() ? TextTransform.NONE
									                                                    : TextTransform.list_values,
									t.equalsAuto() || t.equalsInitial() || t.equalsNone() ? null : t,
									v.getSourceDeclaration(),
									v.getSupportedBrailleCSS());
							break;
						}
				if (flat == null)
					flat = new PropertyValue(
						v.getProperty(),
						v.getCSSProperty(),
						v.getValue(),
						v.getSourceDeclaration(),
						v.getSupportedBrailleCSS());
				i.set(flat);
			}
			decls = new ValidatedDeclarations(list);
		}
		SortedMap<String,BrailleCssStyle> nested = null;
		if (nestedStyles != null)
			for (String sel : nestedStyles.keySet())
				if (!sel.startsWith("@")) {
					BrailleCssStyle n = nestedStyles.get(sel);
					BrailleCssStyle nn; {
						if (sel.equals("&::before") ||
						    sel.equals("&::after") ||
						    (sel.startsWith("&:") && !sel.startsWith("&::")))
							// pseudo-classes and ::before and ::after pseudo-elements inherit from main style
							nn = n.inheritFrom(decls);
						else
							nn = n.inheritFrom(base);
					}
					if (n != nn) {
						if (nested == null)
							nested = new TreeMap<>(nestedStyles);
						nested.put(sel, nn);
					}
				}
		if (declarations != null || !decls.isEmpty() || nested != null) {
			BrailleCssStyle copy = clone();
			copy.declarations = decls.isEmpty() ? null : decls;
			copy.declarationMap = null;
			if (nested != null) {
				copy.nestedStyles = nested;
				copy.rules = null;
			}
			copy.serialized = null;
			return copy;
		}
		return this;
	}

	// note that this BrailleCssStyle will never be cached because the context is unknown
	public static BrailleCssStyle of(String selector, BrailleCssStyle style) {
		return new Builder().add(selector, style).build();
	}

	private static final Comparator<String> sortSelectors = new Comparator<String>() {
			public int compare(String selector1, String selector2) {
				if (selector1.startsWith("&") && !selector2.startsWith("&"))
					return 1;
				else if (!selector1.startsWith("&") && selector2.startsWith("&"))
					return -1;
				else
					return selector1.compareTo(selector2);
			}
		};

	/* Split the selector parts and serialize */
	private static String[] serializeSelector(List<Selector> combinedSelector) {
		List<String> selector = new ArrayList<>();
		for (CombinatorSelectorPart part : flattenSelector(combinedSelector))
			selector.add("&" + part);
		return selector.toArray(new String[selector.size()]);
	}

	/* Convert a combined selector, which may contain pseudo element parts with other pseudo
	 * elements stacked onto them, into a flat list of selector parts and combinators */
	private static List<CombinatorSelectorPart> flattenSelector(List<Selector> combinedSelector) {
		List<CombinatorSelectorPart> selector = new ArrayList<>();
		for (Selector s : combinedSelector)
			flattenSelector(selector, s);
		return selector;
	}

	private static void flattenSelector(List<CombinatorSelectorPart> collect, Selector selector) {
		Combinator combinator = selector.getCombinator();
		for (SelectorPart part : selector) {
			flattenSelector(collect, combinator, part);
			combinator = null;
		}
	}

	private static void flattenSelector(List<CombinatorSelectorPart> collect, Combinator combinator, SelectorPart part) {
		collect.add(new CombinatorSelectorPart(combinator, part));
		if (part instanceof PseudoElementImpl) {
			PseudoElementImpl pe = (PseudoElementImpl)part;
			if (!pe.getCombinedSelectors().isEmpty())
				for (Selector s: pe.getCombinedSelectors())
					flattenSelector(collect, s);
			else {
				if (!pe.getPseudoClasses().isEmpty())
					for (PseudoClass pc : pe.getPseudoClasses())
						collect.add(new CombinatorSelectorPart(null, pc));
				if (pe.hasStackedPseudoElement())
					flattenSelector(collect, null, pe.getStackedPseudoElement());
			}
		}
	}

	private static class CombinatorSelectorPart {
		final Combinator combinator;
		final SelectorPart selector;
		CombinatorSelectorPart(Combinator combinator, SelectorPart selector) {
			this.combinator = combinator;
			this.selector = selector;
		}
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			if (combinator != null)
				b.append(combinator.value());
			if (selector instanceof PseudoElementImpl) {
				PseudoElementImpl pe = (PseudoElementImpl)selector;
				b.append(":");
				if (!pe.isSpecifiedAsClass())
					b.append(":");
				b.append(pe.getName());
				String[] args = pe.getArguments();
				if (args.length > 0) {
					b.append("(");
					for (int i = 0; i < args.length; i++) {
						if (i > 0) b.append(", ");
						b.append(args[i]);
					}
					b.append(")");
				}
			} else
				b.append(selector);
			return b.toString();
		}
	}

	private static final SupportedBrailleCSS DEFAULT_VALIDATOR = new DeepDeclarationTransformer(true, false);

}
