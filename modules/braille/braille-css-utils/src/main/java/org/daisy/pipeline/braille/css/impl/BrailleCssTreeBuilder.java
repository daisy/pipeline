package org.daisy.pipeline.braille.css.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.Combinator;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Selector.SelectorPart;

import org.daisy.braille.css.AnyAtRule;
import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativePage;
import org.daisy.braille.css.InlineStyle.RuleRelativeVolume;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.RuleCounterStyle;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.braille.css.RuleVolume;
import org.daisy.braille.css.RuleVolumeArea;

public final class BrailleCssTreeBuilder {

	private BrailleCssTreeBuilder() {}

	public static class Style implements Comparator<String> {

		List<Declaration> declarations;
		SortedMap<String,Style> nestedStyles; // sorted by key

		public int compare(String selector1, String selector2) {
			if (selector1.startsWith("&") && !selector2.startsWith("&"))
				return 1;
			else if (!selector1.startsWith("&") && selector2.startsWith("&"))
				return -1;
			else
				return selector1.compareTo(selector2);
		}

		private Style add(Declaration declaration) {
			if (declaration != null) {
				if (this.declarations == null) this.declarations = new ArrayList<Declaration>();
				declarations.add(declaration);
			}
			return this;
		}

		private Style add(Iterable<Declaration> declarations) {
			if (declarations != null)
				for (Declaration d : declarations)
					add(d);
			return this;
		}

		Style add(String selector, Style nestedStyle) {
			if (this.nestedStyles == null) this.nestedStyles = new TreeMap<String,Style>(this);
			if (selector == null)
				add(nestedStyle);
			if (this.nestedStyles.containsKey(selector))
				this.nestedStyles.get(selector).add(nestedStyle);
			else
				this.nestedStyles.put(selector, nestedStyle);
			return this;
		}

		Style add(String[] selector, Style nestedStyle) {
			if (selector.length == 0)
				return this;
			for (int i = selector.length - 1; i > 0; i--)
				nestedStyle = new Style().add(selector[i], nestedStyle);
			return add(selector[0], nestedStyle);
		}

		private Style add(Style style) {
			add(style.declarations);
			if (style.nestedStyles != null)
				for (Map.Entry<String,Style> e : style.nestedStyles.entrySet())
					add(e.getKey(), e.getValue());
			return this;
		}

		static Style of(RulePage page) {
			// assumed to be anonymous page
			Style style = new Style();
			for (Rule<?> r : page)
				if (r instanceof Declaration)
					style.add((Declaration)r);
				else if (r instanceof RuleMargin)
					style.add("@" + ((RuleMargin)r).getMarginArea(),
					          new Style().add((List<Declaration>)r));
				else
					throw new RuntimeException("coding error");
			String pseudo = page.getPseudo();
			return pseudo == null
				? style
				: new Style().add("&:" + pseudo, style);
		}

		private static Style of(RuleVolumeArea volumeArea) {
			Style style = new Style();
			for (Rule<?> r : volumeArea)
				if (r instanceof Declaration)
					style.add((Declaration)r);
				else if (r instanceof RulePage)
					style.add("@page", Style.of((RulePage)r));
				else
					throw new RuntimeException("coding error");
			return style;
		}

		private static Style of(RuleVolume volume) {
			Style style = new Style();
			for (Rule<?> r : volume)
				if (r instanceof Declaration)
					style.add((Declaration)r);
				else if (r instanceof RuleVolumeArea)
					style.add("@" + ((RuleVolumeArea)r).getVolumeArea().value, Style.of((RuleVolumeArea)r));
				else
					throw new RuntimeException("coding error");
			String pseudo = volume.getPseudo();
			return pseudo == null
				? style
				: new Style().add("&:" + pseudo, style);
		}

		private static Style of(AnyAtRule rule) {
			Style style = new Style();
			for (Rule<?> r : rule)
				if (r instanceof Declaration)
					style.add((Declaration)r);
				else if (r instanceof AnyAtRule)
					style.add("@" + ((AnyAtRule)r).getName(), Style.of((AnyAtRule)r));
				else
					throw new RuntimeException("coding error");
			return style;
		}

		public static Style of(InlineStyle inlineStyle) {
			Style style = new Style();
			for (RuleBlock<?> rule : inlineStyle) {
				if (rule instanceof RuleMainBlock)
					// Note that the declarations have not been transformed by BrailleCSSDeclarationTransformer yet
					style.add((List<Declaration>)rule);
				else if (rule instanceof RuleRelativeBlock) {
					String[] selector = serializeSelector(((RuleRelativeBlock)rule).getSelector());
					Style decls = new Style(); {
						for (Rule<?> r : (RuleRelativeBlock)rule)
							if (r instanceof Declaration)
								decls.add((Declaration)r);
							else if (r instanceof RulePage)
								decls.add("@page", Style.of((RulePage)r));
							else
								throw new RuntimeException("coding error");
					}
					style.add(selector, decls); }
				else if (rule instanceof RulePage)
					style.add("@page", Style.of((RulePage)rule));
				else if (rule instanceof RuleVolume)
					style.add("@volume", Style.of((RuleVolume)rule));
				else if (rule instanceof RuleTextTransform) {
					String name = ((RuleTextTransform)rule).getName();
					Style textTransform = new Style().add((List<Declaration>)rule);
					if (name == null)
						style.add("@text-transform", textTransform);
					else
						style.add("@text-transform", new Style().add("& " + name, textTransform)); }
				else if (rule instanceof RuleCounterStyle) {
					String name = ((RuleCounterStyle)rule).getName();
					Style counterStyle = new Style().add((List<Declaration>)rule);
					style.add("@counter-style", new Style().add("& " + name, counterStyle)); }
				else if (rule instanceof RuleMargin)
					style.add("@" + ((RuleMargin)rule).getMarginArea(),
					          new Style().add((List<Declaration>)rule));
				else if (rule instanceof RuleVolumeArea)
					style.add("@" + ((RuleVolumeArea)rule).getVolumeArea().value,
					          Style.of((RuleVolumeArea)rule));
				else if (rule instanceof RuleRelativePage)
					style.add(Style.of(((RuleRelativePage)rule).asRulePage()));
				else if (rule instanceof RuleRelativeVolume)
					style.add(Style.of(((RuleRelativeVolume)rule).asRuleVolume()));
				else if (rule instanceof AnyAtRule)
					style.add("@" + ((AnyAtRule)rule).getName(),
					          Style.of((AnyAtRule)rule));
				else
					throw new RuntimeException("coding error");
			}
			return style;
		}

		@Override
		public String toString() {
			return BrailleCssSerializer.toString(this);
		}
	}

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
