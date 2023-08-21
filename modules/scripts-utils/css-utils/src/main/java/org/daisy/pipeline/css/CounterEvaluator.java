package org.daisy.pipeline.css;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.css.TermString;

import org.daisy.braille.css.BrailleCSSProperty.Display;

/**
 * Traverses a document while keeping track of <a
 * href="https://www.w3.org/TR/css-lists-3/#counter">counter</a> values and providing the ability to
 * generate counter representations.
 */
public abstract class CounterEvaluator<E> {

	/**
	 * Get {@code counter-reset} property.
	 */
	protected abstract Collection<? extends TermPair<String,Integer>> getCounterReset(E elementStyle);

	/**
	 * Get {@code counter-set} property.
	 */
	protected abstract Collection<? extends TermPair<String,Integer>> getCounterSet(E elementStyle);

	/**
	 * Get {@code counter-increment} property.
	 */
	protected abstract Collection<? extends TermPair<String,Integer>> getCounterIncrement(E elementStyle);

	/**
	 * Get {@code display} property.
	 */
	protected abstract Display getDisplay(E elementStyle);

	/**
	 * Get {@code content} property of {@code ::marker} pseudo-element.
	 *
	 * {@code null} means there is no {@code ::marker} pseudo-element. An empty list means the
	 * pseudo-element has no content.
	 */
	protected abstract Collection<Term<?>> getMarkerContent(E elementStyle);

	/**
	 * Get list counter style.
	 */
	protected abstract CounterStyle getListStyleType(E elementStyle, CounterStyle parentListStyleType);

	/**
	 * Get counter style for given name
	 */
	protected abstract CounterStyle getNamedCounterStyle(String name);

	private final LinkedList<CounterStyle> listCounterStyle = new LinkedList<>();
	protected final LinkedList<Map<String,Integer>> counterValues = new LinkedList<>();
	private boolean previousEventWasStartElement = true; // false = endElement

	/**
	 * This method must be called at the start of every element while traversing the document tree
	 * in depth-first order.
	 *
	 * New counter values are determined based on the given style.
	 *
	 * @param elementStyle The style of the current element, possibly containing {@code
	 *                     counter-reset}, {@code counter-set} and {@code counter-increment}
	 *                     properties.
	 */
	public void startElement(E elementStyle) {
		if (listCounterStyle.isEmpty())
			listCounterStyle.push(CounterStyle.DISC);
		if (previousEventWasStartElement)
			counterValues.push(null);
		previousEventWasStartElement = true;
		updateCounterValues(elementStyle);
		updateCurrentListCounterStyle(elementStyle);
	}

	/**
	 * This method must be called at the end of every element while traversing the document tree in
	 * depth-first order.
	 */
	public void endElement() {
		listCounterStyle.pop();
		if (!previousEventWasStartElement)
			counterValues.pop();
		previousEventWasStartElement = false;
	}

	/**
	 * Determine current counter values (see https://www.w3.org/TR/css-lists-3/#creating-counters)
	 */
	private void updateCounterValues(E elementStyle) {
		// counter-reset
		{
			Collection<? extends TermPair<String,Integer>> list = getCounterReset(elementStyle);
			if (list != null) {
				for (TermPair<String,Integer> p : list) {
					String name = p.getKey();
					Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
					if (createdBySiblingOrSelf == null) {
						createdBySiblingOrSelf = new HashMap<>();
						counterValues.set(0, createdBySiblingOrSelf);
					}
					createdBySiblingOrSelf.put(name, p.getValue());
				}
			}
		}
		// counter-set or counter-increment work in addition to counter-reset
		Set<String> done = null;
		{
			Collection<? extends TermPair<String,Integer>> list = getCounterSet(elementStyle);
			if (list != null) {
				for (TermPair<String,Integer> p : list) {
					String name = p.getKey();
					boolean exists = false;
					for (Map<String,Integer> c : counterValues) {
						if (c != null) {
							c.put(name, p.getValue());
							exists = true;
							break;
						}
					}
					if (!exists) {
						Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
						if (createdBySiblingOrSelf == null) {
							createdBySiblingOrSelf = new HashMap<>();
							counterValues.set(0, createdBySiblingOrSelf);
						}
						createdBySiblingOrSelf.put(name, p.getValue());
					}
					if (done == null)
						done = new HashSet<>();
					done.add(name);
				}
			}
		}
		{
			Collection<? extends TermPair<String,Integer>> list = getCounterIncrement(elementStyle);
			if (list != null) {
				for (TermPair<String,Integer> p : list) {
					String name = p.getKey();
					if (done == null || !done.contains(name)) {
						boolean exists = false;
						for (Map<String,Integer> c : counterValues) {
							if (c != null) {
								c.put(name, c.getOrDefault(name, 0) + p.getValue());
								exists = true;
								break;
							}
						}
						if (!exists) {
							Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
							if (createdBySiblingOrSelf == null) {
								createdBySiblingOrSelf = new HashMap<>();
								counterValues.set(0, createdBySiblingOrSelf);
							}
							createdBySiblingOrSelf.put(name, p.getValue());
						}
						if (done == null)
							done = new HashSet<>();
						done.add(name);
					}
				}
			}
		}
		// by default list items increment the 'list-item' counter
		if ((done == null || !done.contains("list-item")) && getDisplay(elementStyle) == Display.LIST_ITEM) {
			boolean exists = false;
			for (Map<String,Integer> c : counterValues) {
				if (c != null) {
					c.put("list-item", c.getOrDefault("list-item", 0) + 1);
					exists = true;
					break;
				}
			}
			if (!exists) {
				Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
				if (createdBySiblingOrSelf == null) {
					createdBySiblingOrSelf = new HashMap<>();
					counterValues.set(0, createdBySiblingOrSelf);
				}
				createdBySiblingOrSelf.put("list-item", 1);
			}
		}
	}

	/**
	 * Determine current list counter style
	 */
	private void updateCurrentListCounterStyle(E elementStyle) {
		listCounterStyle.push(getListStyleType(elementStyle, listCounterStyle.peek()));
	}

	/**
	 * Evaluate marker contents (see https://www.w3.org/TR/css-lists-3/#content-property)
	 */
	public String generateMarkerContents(E elementStyle) throws IllegalArgumentException {
		if (getDisplay(elementStyle) == Display.LIST_ITEM) {
			Collection<Term<?>> content = getMarkerContent(elementStyle);
			if (content != null) {
				if (!content.isEmpty())
					return evaluateContent(content);
				else
					return null;
			}
			CounterStyle listCounterStyle = this.listCounterStyle.peek();
			if (listCounterStyle != null)
				return evaluateCounter("list-item", listCounterStyle, true);
		}
		return null;
	}

	private String evaluateContent(Collection<Term<?>> content) throws IllegalArgumentException {
		StringBuilder s = new StringBuilder();
		for (Term<?> t : content) {
			if (t instanceof TermString) {
				s.append(((TermString)t).getValue());
			} else if (t instanceof TermFunction) {
				TermFunction f = (TermFunction)t;
				if ("counter".equalsIgnoreCase(f.getFunctionName())) {
					String name = null;
					CounterStyle style = null;
					for (Term<?> arg : f)
						if (name == null)
							if (arg instanceof TermIdent)
								name = ((TermIdent)arg).getValue();
							else
								throw new IllegalArgumentException(
									"invalid first argument of counter() function: should be the counter name");
						else if (style == null) {
							if (arg instanceof TermIdent)
								style = getNamedCounterStyle(((TermIdent)arg).getValue());
							else if (arg instanceof TermFunction)
								try {
									style = CounterStyle.fromSymbolsFunction((TermFunction)arg);
								} catch (IllegalArgumentException e) {
									throw new IllegalArgumentException(
										"invalid second argument of counter() function: should be a counter style", e);
								}
							else
								throw new IllegalArgumentException(
									"invalid second argument of counter() function: should be a counter style"); }
						else
							throw new IllegalArgumentException(
								"unexpected argument of counter() function: function takes at most two arguments");
					if (name == null)
						throw new IllegalArgumentException("counter() function requires at least one argument");
					if (style == null)
						style = CounterStyle.DECIMAL;
					s.append(evaluateCounter(name, style, false));
				} else if ("counters".equalsIgnoreCase(f.getFunctionName())) {
					String name = null;
					String separator = null;
					CounterStyle style = null;
					for (Term<?> arg : f)
						if (name == null)
							if (arg instanceof TermIdent)
								name = ((TermIdent)arg).getValue();
							else
								throw new IllegalArgumentException(
									"invalid first argument of counters() function: should be the counter name");
						else if (separator == null) {
							if (arg instanceof TermString)
								separator = ((TermString)arg).getValue();
							else
								throw new IllegalArgumentException(
									"invalid second argument of counters() function: should be a string"); }
						else if (style == null) {
							if (arg instanceof TermIdent)
								style = getNamedCounterStyle(((TermIdent)arg).getValue());
							else if (arg instanceof TermFunction)
								try {
									style = CounterStyle.fromSymbolsFunction((TermFunction)arg);
								} catch (IllegalArgumentException e) {
									throw new IllegalArgumentException(
										"invalid third argument of counters() function: should be a counter style", e);
								}
							else
								throw new IllegalArgumentException(
									"invalid third argument of counters() function: should be a counter style"); }
						else
							throw new IllegalArgumentException(
								"unexpected argument of counters() function: function takes at most three arguments");
					if (name == null || separator == null)
						throw new IllegalArgumentException("counters() function requires at least two arguments");
					if (style == null)
						style = CounterStyle.DECIMAL;
					s.append(evaluateCounters(name, style, separator));
				} else
					throw new RuntimeException(f.getFunctionName() + "() function not supported in content list"); // FIXME
			} else
				throw new IllegalStateException(); // cannot happen
		}
		return s.toString();
	}

	/**
	 * Generate a counter representation of the innermost counter in the counter set with the given
	 * name (see https://www.w3.org/TR/css-lists-3/#counter-functions)
	 */
	public String evaluateCounter(String name, CounterStyle style, boolean withPrefixAndSuffix) {
		int value = evaluateCounter(name);
		return style.format(value, withPrefixAndSuffix);
	}

	/**
	 * Get the value of the innermost counter in the counter set with the given name, or instaniate
	 * a counter with value {@code 0} (see https://www.w3.org/TR/css-lists-3/#counter-functions)
	 */
	public int evaluateCounter(String name) {
		Integer value = null;
		boolean exists = false;
		for (Map<String,Integer> c : counterValues)
			if (c != null && c.containsKey(name)) {
				value = c.get(name);
				break;
			}
		if (value == null) {
			value = 0;
			Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
			if (createdBySiblingOrSelf == null) {
				createdBySiblingOrSelf = new HashMap<>();
				counterValues.set(0, createdBySiblingOrSelf);
			}
			createdBySiblingOrSelf.put(name, value);
		}
		return value;
	}

	/**
	 * Generate a counter representation of all the counters in the counter set with the given name
	 * (see https://www.w3.org/TR/css-lists-3/#counter-functions)
	 */
	public String evaluateCounters(String name, CounterStyle style, String separator) {
		StringBuilder s = new StringBuilder();
		for (Map<String,Integer> c : counterValues)
			if (c != null && c.containsKey(name)) {
				if (s.length() > 0)
					s.insert(0, separator);
				s.insert(0, style.format(c.get(name)));
			}
		if (s.length() == 0) {
			Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
			if (createdBySiblingOrSelf == null) {
				createdBySiblingOrSelf = new HashMap<>();
				counterValues.set(0, createdBySiblingOrSelf);
			}
			createdBySiblingOrSelf.put(name, 0);
			s.append(style.format(0));
		}
		return s.toString();
	}
}
