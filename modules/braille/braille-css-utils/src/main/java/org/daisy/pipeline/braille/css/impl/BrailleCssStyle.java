package org.daisy.pipeline.braille.css.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.Combinator;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Selector.SelectorPart;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeHyphenationResource;
import org.daisy.braille.css.InlineStyle.RuleRelativePage;
import org.daisy.braille.css.InlineStyle.RuleRelativeVolume;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.RuleCounterStyle;
import org.daisy.braille.css.RuleHyphenationResource;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.braille.css.RuleVolume;
import org.daisy.braille.css.RuleVolumeArea;
import org.daisy.braille.css.VendorAtRule;

/**
 * Tree reprentation of a CSS style. The tree structure is based on Sass' nesting of selectors.
 *
 * This class is immutable
 */
public final class BrailleCssStyle {

	// these fields need to be package private because they are used in BrailleCssSerializer
	// note that even though the declarations are assumed to not change, we don't assume they are unmodifiable
	final SimpleInlineStyle simpleStyle;
	final List<Declaration> declarations;
	final SortedMap<String,BrailleCssStyle> nestedStyles; // sorted by key

	private final Context context;

	private BrailleCssStyle(Builder builder) {
		this.context = builder.context;
		this.declarations = builder.declarations != null && !builder.declarations.isEmpty() && !builder.validate
			? ImmutableList.copyOf(builder.declarations)
			: null;
		this.simpleStyle = builder.declarations != null && !builder.declarations.isEmpty() && builder.validate
			? new SimpleInlineStyle(builder.declarations)
			: null;
		SortedMap<String,BrailleCssStyle> nested = builder.buildNestedStyles();
		this.nestedStyles = nested != null
			? ImmutableSortedMap.copyOfSorted(nested)
			: null;
	}

	public boolean isEmpty() {
		return simpleStyle == null && declarations == null && nestedStyles == null;
	}

	private String serialized = null;

	@Override
	public String toString() {
		if (serialized == null) {
			serialized = BrailleCssSerializer.toString(this);
			// cache
			if (context != null) // context not set if caching is not allowed
				cache.put(context, serialized, this);
		} else {
			// access cache to keep entry longer in it
			if (context != null)
				cache.get(context, serialized);
		}
		return serialized;
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

	private static class Builder {

		private final Context context;
		/**
		 * Whether to validate declarations (not of nested styles)
		 */
		private Boolean validate;
		private List<Declaration> declarations;
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
		private Builder add(Declaration declaration, boolean validate) {
			if (declaration != null) {
				if (this.declarations == null) {
					this.declarations = new ArrayList<Declaration>();
					this.validate = validate;
				} else if (this.validate != validate)
					throw new IllegalArgumentException();
				declarations.add(declaration);
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
			if (style.simpleStyle != null)
				for (String p : style.simpleStyle.getPropertyNames())
					add(style.simpleStyle.getSourceDeclaration(p), true);
			else if (style.declarations != null)
				for (Declaration d : style.declarations)
					add(d, false);
			if (style.nestedStyles != null)
				for (Map.Entry<String,BrailleCssStyle> e : style.nestedStyles.entrySet())
					add(e.getKey(), e.getValue());
			return this;
		}

		private Builder add(Builder style) {
			if (style.declarations != null)
				for (Declaration d : style.declarations)
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
	 * @param page assumed to not change
	 */
	// used in BrailleCssSerializer
	static BrailleCssStyle of(RulePage page) {
		return of(page, false);
	}

	private static BrailleCssStyle of(RuleRelativePage page) {
		return of(page.asRulePage(), true);
	}

	private static BrailleCssStyle of(RulePage page, boolean relative) {
		// assumed to be anonymous page
		Builder style = new Builder(Context.PAGE);
		for (Rule<?> r : page)
			if (r instanceof Declaration)
				style.add((Declaration)r, true);
			else if (r instanceof RuleMargin) {
				Builder margin = new Builder();
				for (Declaration d : (RuleMargin)r)
					margin.add(d, true);
				style.add("@" + ((RuleMargin)r).getMarginArea(), margin);
			} else
				throw new RuntimeException("coding error");
		String pseudo = page.getPseudo();
		Builder relativeRule = (pseudo == null)
			? style
			: new Builder(Context.PAGE).add("&:" + pseudo, style);
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
				style.add((Declaration)r, true);
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
				style.add((Declaration)r, true);
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
			style.add(d, false);
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
				style.add((Declaration)r, false);
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
				// Note that the declarations have not been transformed by BrailleCSSDeclarationTransformer yet.
				// This will be done when build() is called.
				for (Declaration d : (RuleMainBlock)rule)
					style.add(d, context == Context.ELEMENT);
			else if (rule instanceof RuleRelativeBlock) {
				String[] selector = serializeSelector(((RuleRelativeBlock)rule).getSelector());
				Builder decls = new Builder(); {
					for (Rule<?> r : (RuleRelativeBlock)rule)
						if (r instanceof Declaration)
							decls.add((Declaration)r, context == Context.ELEMENT);
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
					textTransform.add(d, false);
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
					counterStyle.add(d, false);
				style.add("@counter-style", new Builder().add("& " + name, counterStyle)); }
			else if (rule instanceof RuleMargin) {
				Builder margin = new Builder();
				for (Declaration d : (RuleMargin)rule)
					margin.add(d, true);
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
		BrailleCssStyle s = cache.get(context, inlineStyle);
		if (s == null) {
			s = of(new InlineStyle(inlineStyle, context), context);
			cache.put(context, inlineStyle, s);
		}
		return s;
	}

	// note that this BrailleCssStyle will never be cached because the context is unknown
	public static BrailleCssStyle of(String selector, BrailleCssStyle style) {
		return new Builder().add(selector, style).build();
	}

	private final static Cache cache = new Cache();

	private static class Cache {

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
			public int compareTo(CacheKey that) {
				int i = this.context.compareTo(that.context);
				if (i != 0)
					return i;
				return this.style.compareTo(that.style);
			}
			public int hashCode() {
				final int prime = 31;
				int hash = 1;
				hash = prime * hash + context.hashCode();
				hash = prime * hash + style.hashCode();
				return hash;
			}
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
}
