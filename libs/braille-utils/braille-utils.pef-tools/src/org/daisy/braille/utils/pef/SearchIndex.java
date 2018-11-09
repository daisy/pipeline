package org.daisy.braille.utils.pef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a search index.
 * @author Joel Håkansson
 *
 * @param <E> the type of index
 */
public class SearchIndex<E> {
	private static final boolean debug = false;
	private final Map<String, Set<E>> index;
	private static final String REGEX = "[\\s\\.,:/-]";
	private final int exclude;

	/**
	 * Creates a new search index with the default sub-word limit (3).
	 */
	public SearchIndex() {
		this(3);
	}

	/**
	 * Creates a new search index with the specified sub-word limit.
	 * @param subwordLimit the smallest part of a word longer than limit
	 * 				to be added to the index. For example, if the limit is
	 * 				 3, a search for "li" will not match entries containing
	 * 				"limit"
	 * @throws IllegalArgumentException if subwordLimit is &lt; 1
	 */
	public SearchIndex(int subwordLimit) {
		index = new Hashtable<>();
		if (subwordLimit<1) {
			throw new IllegalArgumentException("Value must be 1 or greater.");
		}
		this.exclude = subwordLimit;
	}

	/**
	 * Associates a string and (if the string length exceeds the sub word limit) its 
	 * substrings with an object.
	 * @param val the string
	 * @param obj the object
	 */
	public void add(String val, E obj) {
		add(val, obj, false);
	}

	/**
	 * Associates a string with an object.
	 * @param val the string
	 * @param obj the object
	 * @param strict when true, only the specified string will match the
	 * 		  object. When false and the string length exceeds the sub word
	 * 		  limit, substrings of the specified string will also
	 * 		  match this object. 
	 */
	public void add(String val, E obj, boolean strict) {
		for (String ind : val.toLowerCase().split(REGEX)) {
			if (ind!=null && ind.length()>0) {
				if (strict || ind.length()<=exclude) {
					addToIndex(ind, obj);
				} else {
					for (int i=exclude; i<=ind.length(); i++) {
						String indx = ind.substring(0, i);
						addToIndex(indx, obj);
					}
				}
			}
		}
	}

	void addToIndex(String indx, E obj) {
		if (debug)  System.err.println("Adding index: " + indx);
		Set<E> c = index.get(indx);
		if (c==null) {
			c = new HashSet<>();
			index.put(indx, c);
		}
		c.add(obj);
	}

	/**
	 * Gets all objects with an entry for the specified string.
	 * @param str
	 * @return the objects
	 */
	private Set<E> matches(String str) {
		str = str.toLowerCase().replaceAll(REGEX, "");
		if (debug)  System.err.println("Search for: " + str);
		Set<E> books = index.get(str);

		if (books==null) {
			return new HashSet<>();
		}
		return books;
	}

	/**
	 * Returns the set of objects that matches all the specified strings.
	 * @param strs the strings
	 * @return returns a set of matching objects
	 */
	public Set<E> containsAll(Iterable<String> strs) {
		Set<E> result = new HashSet<>();
		boolean first = true;
		for (String s : strs) {
			if (first) {
				result.addAll(matches(s));
				first = false;
			} else {
				Set<E> r = matches(s);
				Iterator<E> i = result.iterator();
				while (i.hasNext()) {
					E f = i.next();
					if (!r.contains(f)) {
						i.remove();
					}
				}
			}
		}
		return result;
	}

	private List<String> normalizeString(String str) {
		String[] t = str.replaceAll(REGEX, " ").split("\\s");
		ArrayList<String> ret = new ArrayList<>();
		for (String s : t) {
			if (!"".equals(s) && s!=null) {
				ret.add(s);
			}
		}
		return ret;
	}

	/**
	 * Returns the set of objects that matches all the character units found
	 * in the input string.
	 * @param str the string
	 * @return returns a set of matching objects
	 */
	public Set<E> containsAll(String str) {
		return containsAll(normalizeString(str));
	}

	/**
	 * Returns the set of objects that matches all the specified strings.
	 * @param strs the strings
	 * @return returns a set of matching objects
	 */
	public Set<E> containsAll(String ... strs) {
		return containsAll(Arrays.asList(strs));
	}
}
