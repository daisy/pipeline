package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.CombinedSelector.Specificity;
import cz.vutbr.web.css.CombinedSelector.Specificity.Level;
import cz.vutbr.web.css.MatchCondition;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.csskit.OutputUtil;
import cz.vutbr.web.csskit.CombinedSelectorImpl.SpecificityImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SelectorImpl extends cz.vutbr.web.csskit.SelectorImpl {
	
	public boolean add(Selector selector) throws UnsupportedOperationException {
		if (size() > 0) {
			SelectorPart lastPart = get(size() - 1);
			if (lastPart instanceof PseudoElement) {
				if (!(lastPart instanceof PseudoElementImpl))
					throw new RuntimeException(); // should not happen
				// if this selector contains a custom pseudo class, we append any following selectors to the stack
				if (((PseudoElementImpl)lastPart).containsCustomPseudoClass()) {
					((PseudoElementImpl)lastPart).add(selector);
					return true;
				}
			}
		}
		throw new UnsupportedOperationException("Selectors should be combined with CombinedSelector");
	}
	
	@Override
	public boolean add(SelectorPart part) {
		if (part instanceof PseudoElement) {
			if (!(part instanceof PseudoElementImpl))
				throw new RuntimeException();
			if (size() > 0) {
				SelectorPart lastPart = get(size() - 1);
				if (lastPart instanceof PseudoElement) {
					if (!(lastPart instanceof PseudoElementImpl))
						throw new RuntimeException(); // should not happen
					return ((PseudoElementImpl)lastPart).add((PseudoElementImpl)part);
				}
			}
		} else if (part instanceof PseudoClass) {
			if (size() > 0) {
				SelectorPart lastPart = get(size() - 1);
				if (lastPart instanceof PseudoElement) {
					if (!(lastPart instanceof PseudoElementImpl))
						throw new RuntimeException(); // should not happen
					return ((PseudoElementImpl)lastPart).add((PseudoClass)part);
				}
			}
		}
		return super.add(part);
	}
	
	public static class PseudoClassImpl extends cz.vutbr.web.csskit.SelectorImpl.PseudoClassImpl {
		
		private final String name;
		private final String[] args;
		
		public PseudoClassImpl(String name, String... args) throws IllegalArgumentException {
			super(name, args);
			this.name = name;
			this.args = args;
		}
		
		public boolean matchesPosition(int position, int elementCount) {
			if (name.equals("first-child")) {
				return position == 1;
			} else if (name.equals("last-child")) {
				return position == elementCount;
			} else if (name.equals("only-child")) {
				return position == 1 && elementCount == 1;
			} else if (name.equals("nth-child")) {
				return positionMatches(position, decodeIndex(args[0]));
			} else if (name.equals("nth-last-child")) {
				return positionMatches(elementCount - position + 1, decodeIndex(args[0]));
			} else {
				log.warn("Don't know how to match " + toString() + " pseudo-class");
				return false;
			}
		}
	}
	
	public static class NegationPseudoClassImpl implements PseudoClass {
		
		private final List<Selector> negatedSelector;
		
		public NegationPseudoClassImpl(List<Selector> negatedSelector) {
			if (negatedSelector.size() < 1)
				throw new RuntimeException(":not() must not be empty");
			this.negatedSelector = negatedSelector;
		}
		
		public boolean matches(Element e, MatchCondition cond) {
			for (Selector s : negatedSelector)
				if (s.matches(e, cond))
					return false;
			return true;
		}
		
		public void computeSpecificity(Specificity specificity) {
			Selector mostSpecificSelector = null;
			Specificity highestSpecificity = null;
			for (Selector sel : negatedSelector) {
				Specificity spec = new SpecificityImpl();
				sel.computeSpecificity(spec);
				if (highestSpecificity == null || spec.compareTo(highestSpecificity) > 0) {
					mostSpecificSelector = sel;
					highestSpecificity = spec; }}
			mostSpecificSelector.computeSpecificity(specificity);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(":not(");
			OutputUtil.appendList(sb, negatedSelector, ", ");
			sb.append(")");
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			return prime + negatedSelector.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NegationPseudoClassImpl other = (NegationPseudoClassImpl) obj;
			if (!negatedSelector.equals(other.negatedSelector))
				return false;
			return true;
		}
	}
	
	public static class RelationalPseudoClassImpl implements PseudoClass {
		
		private final List<CombinedSelector> relativeSelector;
		
		public RelationalPseudoClassImpl(List<CombinedSelector> relativeSelector) {
			if (relativeSelector.size() < 1)
				throw new RuntimeException(":has() must not be empty");
			this.relativeSelector = relativeSelector;
		}
		
		public boolean matches(Element e, MatchCondition cond) {
			for (CombinedSelector s : relativeSelector)
				if (matchesRelative(s, e, cond))
					return true;
			return false;
		}
		
		public static boolean matchesRelative(List<Selector> selector, Element e, MatchCondition cond) {
			Iterator<Selector> it = selector.iterator();
			Selector first = it.next();
			Selector.Combinator combinator = first.getCombinator();
			List<Selector> rest = null;
			if (it.hasNext()) {
				rest = new ArrayList<Selector>();
				while (it.hasNext())
					rest.add(it.next()); }
			switch (combinator) {
			case CHILD:
			case DESCENDANT:
				NodeList children = e.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node child = children.item(i);
					if (child instanceof Element) {
						if (first.matches((Element)child, cond))
							if (rest == null || matchesRelative(rest, (Element)child, cond))
								return true; }}
				if (combinator == Selector.Combinator.DESCENDANT) {
					for (int i = 0; i < children.getLength(); i++) {
						Node child = children.item(i);
						if (child instanceof Element) {
							if (matchesRelative(selector, (Element)child, cond))
								return true; }}}
				break;
			case ADJACENT:
			case PRECEDING:
				Node next = e.getNextSibling();
				while (next != null && !(next instanceof Element))
					next = next.getNextSibling();
				if (next != null) {
					if (first.matches((Element)next, cond))
						if (rest == null || matchesRelative(rest, (Element)next, cond))
							return true;
					if (combinator == Selector.Combinator.PRECEDING) {
						if (matchesRelative(selector, (Element)next, cond))
							return true; }}
				break;
			}
			return false;
		}
		
		public void computeSpecificity(Specificity specificity) {
			CombinedSelector mostSpecificSelector = null;
			Specificity highestSpecificity = null;
			for (CombinedSelector sel : relativeSelector) {
				Specificity spec = new SpecificityImpl();
				for (Selector s : sel)
					s.computeSpecificity(spec);
				if (highestSpecificity == null || spec.compareTo(highestSpecificity) > 0) {
					mostSpecificSelector = sel;
					highestSpecificity = spec; }}
			for (Selector s : mostSpecificSelector)
				s.computeSpecificity(specificity);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(":has(");
			OutputUtil.appendList(sb, relativeSelector, ", ");
			sb.append(")");
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			return prime + relativeSelector.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RelationalPseudoClassImpl other = (RelationalPseudoClassImpl) obj;
			if (!relativeSelector.equals(other.relativeSelector))
				return false;
			return true;
		}
	}
	
	public static class PseudoElementImpl implements PseudoElement {
		
		private static enum PseudoElementDef {
			BEFORE("before"),
			AFTER("after"),
			MARKER("marker"),
			DUPLICATE("duplicate"),
			ALTERNATE("alternate", 0, 1),
			LIST_ITEM("list-item"),
			LIST_HEADER("list-header"),
			TABLE_BY("table-by", 1),
			FOOTNOTE_CALL("footnote-call");
			
			private final String name;
			private final int minArgs;
			private final int maxArgs;
			
			private PseudoElementDef(String name) {
				this.name = name;
				this.minArgs = 0;
				this.maxArgs = 0;
			}
			
			private PseudoElementDef(String name, int args) {
				this(name, args, args);
			}
			
			private PseudoElementDef(String name, int minArgs, int maxArgs) {
				this.name = name;
				this.minArgs = minArgs;
				this.maxArgs = maxArgs;
			}
		}
		
		private final static HashMap<String,PseudoElementDef> PSEUDO_ELEMENT_DEFS;
		static {
			PSEUDO_ELEMENT_DEFS = new HashMap<String,PseudoElementDef>();
			for (PseudoElementDef d : PseudoElementDef.values())
				PSEUDO_ELEMENT_DEFS.put(d.name, d);
		}
		
		private final String name;
		private final List<String> args;
		private final List<PseudoClass> pseudoClasses = new ArrayList<PseudoClass>();
		private final List<Selector> combinedSelectors = new ArrayList<Selector>();
		private PseudoElementImpl stackedPseudoElement = null;
		private boolean specifiedAsClass = false;
		
		public PseudoElementImpl(String name, String... args) {
			if (name.startsWith(":")) {
				name = name.substring(1);
				if (name.startsWith(":"))
					name = name.substring(1);
				else
					specifiedAsClass = true;
			}
			name = name.toLowerCase(); // pseudo-element names are case-insensitive
			String prefix = "-daisy-"; // optional prefix for pseudo-elements that are not standard CSS and not extensions
			boolean normalized = false;
			if (name.startsWith(prefix)) {
				name = name.substring(prefix.length());
				normalized = true;
			}
			this.name = name;
			this.args = new ArrayList<String>();
			if ("top-of-page".equals(name) || name.startsWith("-"))
				for (String a : args)
					this.args.add(a);
			else {
				PseudoElementDef def;
				if (PSEUDO_ELEMENT_DEFS.containsKey(name))
					def = PSEUDO_ELEMENT_DEFS.get(name);
				else
					throw new IllegalArgumentException(name + " is not a valid pseudo-element name");
				if (normalized) {
					switch (def) {
					case DUPLICATE:
					case ALTERNATE:
					case LIST_ITEM:
					case LIST_HEADER:
					case TABLE_BY:
						// (non-standard) pseudo-elements that are allowed to have a prefix
						break;
					case FOOTNOTE_CALL:
						// pseudo-element from official WD spec that is not implemented in browsers
						log.warn("Unexpected prefix '{}' in '::{}{}', assuming '{}' was meant",
						         prefix, prefix, name, name);
						break;
					case BEFORE:
					case AFTER:
					case MARKER:
					default:
						throw new IllegalArgumentException(
							String.format("Unexpected prefix '%s' in '::%s%s'", prefix, prefix, name));
					}
				}
				if (args.length > 0 && def.maxArgs == 0)
					throw new IllegalArgumentException(name + " must not be a function");
				if (args.length == 0 && def.minArgs > 0)
					throw new IllegalArgumentException(name + " must be a function");
				if (args.length < def.minArgs || args.length > def.maxArgs)
					throw new IllegalArgumentException(name + " requires " + def.minArgs
					                                   + (def.maxArgs > def.minArgs ? ".." + def.maxArgs : "") + " "
					                                   + (def.minArgs == 1 && def.maxArgs == 1 ? "argument" : "arguments"));
				if (specifiedAsClass) {
					specifiedAsClass = false;
					log.warn("Use a double colon for pseudo element ::" + name);
				}
				
				// validate ::alternate(N) and normalize ::alternate(1) to ::alternate
				switch (def) {
				case ALTERNATE:
					if (args.length != 0) {
						Integer i = null;
						String a = args[0];
						try {
							i = Integer.parseInt(a);
							if (i > 1)
								this.args.add(a);
							else if (i < 1)
								i = null;
						} catch (NumberFormatException e) {}
						if (i == null)
							throw new IllegalArgumentException("Argument of ::alternate must be a number greater than zero, but got " + a);
					}
					break;
				default:
					for (String a : args)
						this.args.add(a);
				}
			}
		}
		
		public String getName() {
			return name;
		}
		
		public String[] getArguments() {
			return args.toArray(new String[args.size()]);
		}
		
		public void computeSpecificity(Specificity spec) {
			spec.add(Level.D);
		}
		
		public boolean matches(Element e, MatchCondition cond) {
			return true;
		}
		
		private boolean add(PseudoClass pseudoClass) {
			if (!combinedSelectors.isEmpty())
				throw new RuntimeException(); // should not happen
			if (stackedPseudoElement != null)
				return stackedPseudoElement.add(pseudoClass);
			else
				return pseudoClasses.add(pseudoClass);
		}
		
		private boolean add(PseudoElementImpl pseudoElement) {
			if (!combinedSelectors.isEmpty())
				throw new RuntimeException(); // should not happen
			if (stackedPseudoElement != null)
				return stackedPseudoElement.add(pseudoElement);
			else {
				stackedPseudoElement = pseudoElement;
				return true;
			 }
		}
		
		private boolean add(Selector selector) {
			if (stackedPseudoElement != null)
				return stackedPseudoElement.add(selector);
			else {
				if (selector.getCombinator() == null)
					throw new RuntimeException(); // should not happen
				return combinedSelectors.add(selector);
			}
		}
		
		public List<PseudoClass> getPseudoClasses() {
			return pseudoClasses;
		}
		
		public List<Selector> getCombinedSelectors() {
			return combinedSelectors;
		}
		
		public boolean hasStackedPseudoElement() {
			return stackedPseudoElement != null;
		}
		
		public PseudoElementImpl getStackedPseudoElement() {
			return stackedPseudoElement;
		}
		
		public boolean isSpecifiedAsClass() {
			return specifiedAsClass;
		}
		
		private boolean containsCustomPseudoClass() {
			if (specifiedAsClass)
				return true;
			else if (stackedPseudoElement != null)
				return stackedPseudoElement.containsCustomPseudoClass();
			else
				return false;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(OutputUtil.PAGE_OPENING);
			if (!specifiedAsClass)
				sb.append(OutputUtil.PAGE_OPENING);
			sb.append(name);
			if (args.size() > 0) {
				sb.append(OutputUtil.FUNCTION_OPENING);
				OutputUtil.appendList(sb, args, ", ");
				sb.append(OutputUtil.FUNCTION_CLOSING);
			}
			if (!combinedSelectors.isEmpty())
				sb = OutputUtil.appendList(sb, combinedSelectors, OutputUtil.EMPTY_DELIM);
			else {
				if (!pseudoClasses.isEmpty())
					for (PseudoClass p : pseudoClasses)
						sb.append(p);
				if (stackedPseudoElement != null)
					sb.append(stackedPseudoElement);
			}
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
			result = prime * result + args.hashCode();
			result = prime * result + pseudoClasses.hashCode();
			result = prime * result
					+ ((stackedPseudoElement == null) ? 0
							: stackedPseudoElement.hashCode());
			result = prime * result
					+ ((combinedSelectors == null) ? 0
							: combinedSelectors.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PseudoElementImpl other = (PseudoElementImpl) obj;
			if (!name.equals(other.name))
				return false;
			if (!args.equals(other.args))
				return false;
			if (!pseudoClasses.equals(other.pseudoClasses))
				return false;
			if (stackedPseudoElement == null) {
				if (other.stackedPseudoElement != null)
					return false;
				else if (combinedSelectors == null) {
					if (other.combinedSelectors != null)
						return false;
					else if (!combinedSelectors.equals(other.combinedSelectors))
						return false;
				}
			} else if (!stackedPseudoElement.equals(other.stackedPseudoElement))
				return false;
			return true;
		}
	}
	
	private static final Logger log = LoggerFactory.getLogger(SelectorImpl.class);
	
}
