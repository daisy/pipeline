package org.daisy.pipeline.braille.css.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;

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

	private BrailleCssStyle(Builder builder) {
		this.context = builder.context;
		this.declarations = builder.declarations == null || builder.declarations.isEmpty()
			? null
			: builder.validate.isPresent()
				? new SimpleInlineStyle(builder.declarations.values(),
				                        null,
				                        builder.validate.get())
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
	 * Evaluate <code>attr()</code> values in <code>content</code> and <code>string-set</code> properties.
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
		if (declarations instanceof SimpleInlineStyle)
			// make sure that declarations field stays a SimpleInlineStyle object because
			// it is assumed throughout the code
			return (SimpleInlineStyle)((SimpleInlineStyle)declarations).clone();
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

	/**
	 * Get the declaration for the given property name.
	 *
	 * Caller must guarantee that the object will not be modified.
	 */
	public Declaration getDeclaration(String propertyName) {
		if (declarations != null)
			return getDeclarationMap().get(propertyName);
		else
			return null;
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
			if (declarations != null && !(declarations instanceof SimpleInlineStyle))
				throw new IllegalArgumentException();
			if (relativeTo.declarations != null && !(relativeTo.declarations instanceof SimpleInlineStyle))
				throw new IllegalArgumentException();
			Builder relative = new Builder(this);
			if (relativeTo.declarations != null)
				for (Declaration d : relativeTo.getDeclarationMap().values()) {
					PropertyValue p = (PropertyValue)d;
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
					if ((!p.getCSSProperty().inherited() || relativeTo.getDeclaration(p.getProperty()) == null) && equal(p, def))
						relative.remove(p.getProperty());
				}
			String s = relative.build().toString();
			if (context != null) // context not set if caching is not allowed
				BrailleCssParser.cache.put(context,
				                           s,
				                           relativeTo.declarations != null
				                               ? (SimpleInlineStyle)relativeTo.declarations
				                               : SimpleInlineStyle.EMPTY,
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

	private static class Builder {

		private final Context context;
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
			this.context = context;
		}

		private Builder(BrailleCssStyle style) {
			this(style.context);
			add(style);
		}

		private Builder() {
			this((Context)null);
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
	 * @param declaration assumed to not change
	 */
	private static BrailleCssStyle of(Declaration declaration, Context context) {
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
				String name = ((RuleCounterStyle)rule).getName();
				Builder counterStyle = new Builder(Context.COUNTER_STYLE);
				for (Declaration d : (RuleCounterStyle)rule)
					counterStyle.add(d, Optional.empty());
				style.add("@counter-style", new Builder().add("& " + name, counterStyle)); }
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
		SimpleInlineStyle parentDecls; {
			if (parent == null || parent.declarations == null)
				parentDecls = SimpleInlineStyle.EMPTY;
			else if (!(parent.declarations instanceof SimpleInlineStyle))
				throw new IllegalArgumentException();
			else
				parentDecls = (SimpleInlineStyle)parent.declarations;
		}
		BrailleCssStyle s = BrailleCssParser.cache.get(context, inlineStyle, parentDecls, true);
		if (s == null) {
			s = of(inlineStyle, context);
			SimpleInlineStyle decls = s.declarations != null
				? (SimpleInlineStyle)s.declarations
				: SimpleInlineStyle.EMPTY;
			if (!parentDecls.isEmpty())
				decls = decls.inheritFrom(parentDecls);
			decls = decls.concretize();
			if (!decls.isEmpty())
				// Make sure that the resulting SimpleInlineStyle is not based on a parent and that
				// it is not concretized because that would result in an exception when
				// inheritFrom() is called on it. Note that concretize() does not have the same
				// effect.
				decls = new SimpleInlineStyle(
					Iterables.transform(decls, d -> new PropertyValue(
					                                    d.getProperty(),
					                                    d.getCSSProperty(),
					                                    d.getValue(),
					                                    d.getSourceDeclaration(),
					                                    d.getSupportedBrailleCSS())));
			if (s.declarations != null || !decls.isEmpty()) {
				s = s.clone();
				s.declarations = decls.isEmpty() ? null : decls;
				s.declarationMap = null;
				s.serialized = null;
			}
			BrailleCssParser.cache.put(context, inlineStyle, parentDecls, true, s);
		}
		return s;
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
