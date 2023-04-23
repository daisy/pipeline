package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.ForwardingList;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.SelectorPart;
import cz.vutbr.web.css.SourceLocator;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.csskit.AbstractRuleBlock;

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
	private List<RuleBlock<?>> nestedStyles;
	
	public InlineStyle(String style) {
		this(style, BrailleCSSParserFactory.Context.ELEMENT);
	}
	
	public InlineStyle(String style, BrailleCSSParserFactory.Context context) {
		this(style, context, null);
	}
	
	public InlineStyle(String style, BrailleCSSParserFactory.Context context, SourceLocator location) {
		nestedStyles = new ArrayList<RuleBlock<?>>();
		List<Declaration> mainDeclarations = new ArrayList<Declaration>();
		for (RuleBlock<?> block : parserFactory.parseInlineStyle(style, context, location)) {
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
					nestedStyles.add(new RuleRelativeBlock(relativeSelector, set));
				}
			} else if (block instanceof RuleTextTransform
			           || block instanceof RuleHyphenationResource
			           || block instanceof RuleCounterStyle
			           || block instanceof RulePage
			           || block instanceof RuleVolume
			           || block instanceof RuleMargin
			           || block instanceof RuleVolumeArea
			           || block instanceof RuleRelativeBlock
			           || block instanceof RuleRelativePage
			           || block instanceof RuleRelativeVolume
			           || block instanceof RuleRelativeHyphenationResource
			           || block instanceof AnyAtRule
			           ) {
				nestedStyles.add(block);
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
		final Iterator<RuleBlock<?>> nestedStylesItr = filterNonEmpty(nestedStyles.listIterator());
		return new Iterator<RuleBlock<?>>() {
			int cursor = 0;
			public boolean hasNext() {
				switch (cursor) {
				case 0:
					if (mainStyleItr.hasNext())
						return true;
				case 1:
				default:
					return nestedStylesItr.hasNext();
				}
			}
			public RuleBlock<?> next() {
				switch (cursor) {
				case 0:
					if (mainStyleItr.hasNext())
						return mainStyleItr.next();
					cursor++;
				case 1:
				default:
					return nestedStylesItr.next();
				}
			}
			public void remove() {
				switch (cursor) {
				case 0:
					mainStyleItr.remove();
					break;
				case 1:
				default:
					nestedStylesItr.remove();
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
		clone.nestedStyles = new ArrayList<RuleBlock<?>>();
		for (RuleBlock<?> b : nestedStyles)
			clone.nestedStyles.add((RuleBlock<?>)b.clone());
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
	
	public static class RuleRelativeBlock extends AbstractRuleBlock<Rule<?>> {
		
		private final List<Selector> selector;
		
		public RuleRelativeBlock(List<Selector> selector) {
			this.selector = selector;
		}
		
		public RuleRelativeBlock(List<Selector> selector, List<Declaration> declarations) {
			this(selector);
			unlock();
			addAll(declarations);
		}
		
		public boolean add(Rule<?> r) {
			if (!(r instanceof Declaration || r instanceof RulePage))
				throw new IllegalArgumentException("Element must be either a Declaration or a RulePage, but got " + r);
			return super.add(r);
		}
		
		public List<Selector> getSelector() {
			return selector;
		}
	}
	
	private static abstract class ForwardingRuleBlock<E> extends ForwardingList<E> {
		
		final RuleBlock<E> delegate;
		
		ForwardingRuleBlock(RuleBlock<E> delegate) {
			this.delegate = delegate;
		}
		
		public List<E> delegate() {
			return delegate;
		}
		
		public StyleSheet getStyleSheet() {
			return delegate.getStyleSheet();
		}
		
		public void setStyleSheet(StyleSheet sheet) {
			delegate.setStyleSheet(sheet);
		}
		
		public List<E> asList() {
			return delegate.asList();
		}
		
		public Rule<E> replaceAll(List<E> replacement) {
			return delegate.replaceAll(replacement);
		}
		
		public Rule<E> unlock() {
			return delegate.unlock();
		}
	}
	
	public static class RuleRelativePage extends ForwardingRuleBlock<Rule<?>> implements RuleBlock<Rule<?>> {
		
		private final RulePage page;
		
		public RuleRelativePage(RulePage page) {
			super(page);
			if (page.getName() != null || page.getPseudo() == null)
				throw new RuntimeException();
			this.page = page;
		}
		
		public RulePage asRulePage() {
			return page;
		}
		
		@Override
		public RuleRelativePage clone() {
			return new RuleRelativePage((RulePage)delegate.clone());
		}
	}
	
	public static class RuleRelativeVolume extends ForwardingRuleBlock<Rule<?>> implements RuleBlock<Rule<?>> {
		
		private final RuleVolume volume;
		
		public RuleRelativeVolume(RuleVolume volume) {
			super(volume);
			if (volume.getPseudo() == null)
				throw new RuntimeException();
			this.volume = volume;
		}
		
		public RuleVolume asRuleVolume() {
			return volume;
		}
		
		@Override
		public RuleRelativeVolume clone() {
			return new RuleRelativeVolume((RuleVolume)delegate.clone());
		}
	}
	
	public static class RuleRelativeHyphenationResource extends ForwardingRuleBlock<Declaration> implements RuleBlock<Declaration> {
		
		private final RuleHyphenationResource hyphenationResource;
		
		public RuleRelativeHyphenationResource(RuleHyphenationResource hyphenationResource) {
			super(hyphenationResource);
			this.hyphenationResource = hyphenationResource;
		}
		
		public RuleHyphenationResource asRuleHyphenationResource() {
			return hyphenationResource;
		}
		
		@Override
		public RuleRelativeHyphenationResource clone() {
			return new RuleRelativeHyphenationResource((RuleHyphenationResource)delegate.clone());
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
