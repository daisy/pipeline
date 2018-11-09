package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.SelectorPart;
import cz.vutbr.web.csskit.AbstractRuleBlock;

import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;

/*
 * Represents style attributes that are the result of "inlining" a style sheet
 * attached to a document. Inlining is an operation intended to be done by CSS
 * processors internally, and as such the resulting style attributes are not
 * valid in an input document.
 */
public class InlineStyle implements Cloneable, Iterable<RuleBlock<?>> {
	
	private final static BrailleCSSParserFactory parserFactory = new BrailleCSSParserFactory();
	private static final SelectorPart dummyElementSelectorPart;
	static {
		RuleFactory ruleFactory = new BrailleCSSRuleFactory();
		dummyElementSelectorPart = ruleFactory.createElementDOM(null, true);
	}
	
	private final static RuleMainBlock emptyBlock = new RuleMainBlock();

	private Optional<RuleMainBlock> mainStyle;
	private List<RuleRelativeBlock> relativeRules;
	private List<RuleTextTransform> textTransformDefs;
	
	public InlineStyle(String style) {
		relativeRules = new ArrayList<RuleRelativeBlock>();
		textTransformDefs = new ArrayList<RuleTextTransform>();
		List<Declaration> mainDeclarations = new ArrayList<Declaration>();
		for (RuleBlock<?> block : parserFactory.parseInlineStyle(style)) {
			if (block == null) {}
			else if (block instanceof RuleSet) {
				RuleSet set = (RuleSet)block;
				List<CombinedSelector> selectors = set.getSelectors();
				assertThat(selectors.size() == 1); // because no commas in selector
				CombinedSelector combinedSelector = selectors.get(0);
				Selector selector = combinedSelector.get(0);
				assertThat(selector.size() >= 1); // because first part of selector is always the style attribute's parent element
				assertThat(dummyElementSelectorPart.equals(selector.get(0))); // see BrailleCSSParserFactory.parseInlineStyle()
				if (selector.size() == 1 && combinedSelector.size() == 1) {
					mainDeclarations.addAll(set);
				} else {
					List<Selector> relativeSelector = new ArrayList<Selector>();
					selector.remove(0);
					if (selector.size() > 0) {
						relativeSelector.add(selector);
					}
					relativeSelector.addAll(combinedSelector.subList(1, combinedSelector.size()));
					relativeRules.add(new RuleRelativeBlock(relativeSelector, set));
				}
			} else if (block instanceof RuleTextTransform) {
				textTransformDefs.add((RuleTextTransform)block);
			} else {
				throw new RuntimeException("coding error");
			}
		}
		mainStyle = new Optional<RuleMainBlock>(new RuleMainBlock(mainDeclarations));
	}
	
	public RuleMainBlock getMainStyle() {
		if (mainStyle.isPresent())
			return mainStyle.get();
		else
			return emptyBlock;
	}
	
	public Iterator<RuleBlock<?>> iterator() {
		final Iterator<RuleMainBlock> mainStyleItr = filterNonEmpty(mainStyle.listIterator());
		final Iterator<RuleRelativeBlock> relativeRulesItr = filterNonEmpty(relativeRules.listIterator());
		final Iterator<RuleTextTransform> textTransformDefsItr = filterNonEmpty(textTransformDefs.listIterator());
		return new Iterator<RuleBlock<?>>() {
			int cursor = 0;
			public boolean hasNext() {
				switch (cursor) {
				case 0:
					if (mainStyleItr.hasNext())
						return true;
				case 1:
					if (relativeRulesItr.hasNext())
						return true;
				case 2:
				default:
					return textTransformDefsItr.hasNext();
				}
			}
			public RuleBlock<?> next() {
				switch (cursor) {
				case 0:
					if (mainStyleItr.hasNext())
						return mainStyleItr.next();
					cursor++;
				case 1:
					if (relativeRulesItr.hasNext())
						return relativeRulesItr.next();
					cursor++;
				case 2:
				default:
					return textTransformDefsItr.next();
				}
			}
			public void remove() {
				switch (cursor) {
				case 0:
					mainStyleItr.remove();
					break;
				case 1:
					relativeRulesItr.remove();
					break;
				case 2:
					textTransformDefsItr.remove();
				}
			}
		};
	}
	
	public boolean isEmpty() {
		return !iterator().hasNext();
	}
	
	@Override
	public Object clone() {
		InlineStyle clone; {
			try {
				clone = (InlineStyle)super.clone(); }
			catch (CloneNotSupportedException e) {
				throw new InternalError("coding error"); }}
		if (mainStyle.isPresent())
			clone.mainStyle = new Optional<RuleMainBlock>(mainStyle.get());
		clone.relativeRules = new ArrayList<RuleRelativeBlock>();
		for (RuleRelativeBlock b : relativeRules)
			clone.relativeRules.add((RuleRelativeBlock)b.clone());
		clone.textTransformDefs = new ArrayList<RuleTextTransform>();
		for (RuleTextTransform b : textTransformDefs)
			clone.textTransformDefs.add((RuleTextTransform)b.clone());
		return clone;
	}
	
	public static class RuleMainBlock extends AbstractRuleBlock<Declaration> {
		
		private RuleMainBlock() {
			super();
		}
		
		private RuleMainBlock(List<Declaration> declarations) {
			this();
			replaceAll(declarations);
		}
	}
	
	public static class RuleRelativeBlock extends AbstractRuleBlock<Declaration> {
		
		private final List<Selector> selector;
		
		public RuleRelativeBlock(List<Selector> selector, List<Declaration> declarations) {
			this.selector = new ArrayList<Selector>(selector);
			replaceAll(declarations);
		}
		
		public List<Selector> getSelector() {
			return selector;
		}
	}
	
	private static class Optional<T> extends ArrayList<T>{

		private Optional(T object) {
			add(object);
		}
		
		private boolean isPresent() {
			return !isEmpty();
		}
		
		private T get() {
			return get(0);
		}
		
		private static final long serialVersionUID = 1L;
		
	}
	
	public static <C extends Collection<?>> Iterator<C> filterNonEmpty(final ListIterator<? extends C> input) {
		return new Iterator<C>() {
			public boolean hasNext() {
				boolean hasNext = false;
				int lookahead = 0;
				while (input.hasNext()) {
					C next = input.next();
					lookahead++;
					if (!next.isEmpty()) {
						hasNext = true;
						break;
					}
				}
				while (lookahead > 0) {
					input.previous();
					lookahead--;
				}
				return hasNext;
			}
			public C next() {
				while (true) {
					C next = input.next();
					if (!next.isEmpty())
						return next;
				}
			}
			public void remove() {
				input.remove();
			}
		};
	}
	
	private static void assertThat(boolean test) {
		if (!test)
			throw new RuntimeException("Coding error");
	}
}
