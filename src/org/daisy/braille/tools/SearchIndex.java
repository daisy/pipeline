package org.daisy.braille.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchIndex<E> {
	private final static boolean debug = false;
	private final Map<String, Set<E>> index;
    private final static String REGEX = "[\\s\\.,:/-]";

	public SearchIndex() {
		index = new Hashtable<String, Set<E>>();
	}
	
	public void add(String val, E obj) {
		add(val, obj, false);
	}
	
	public void add(String val, E obj, boolean strict) {
		for (String ind : val.toLowerCase().split(REGEX)) {
			if (ind!=null && ind.length()>0) {
				if (strict || ind.length()<=3) {
					addToIndex(ind, obj);
				} else {
					for (int i=3; i<=ind.length(); i++) {
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
			c = new HashSet<E>();
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
    		return new HashSet<E>();
    	}
    	return books;
    }
        
    public Set<E> containsAll(Iterable<String> strs) {
    	Set<E> result = new HashSet<E>();
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
    	ArrayList<String> ret = new ArrayList<String>();
    	for (String s : t) {
    		if (!"".equals(s) && s!=null) {
    			ret.add(s);
    		}
    	}
    	return ret;
    }
    
    public Set<E> containsAll(String str) {
    	return containsAll(normalizeString(str));
    }
    
    public Set<E> containsAll(String ... strs) {
    	return containsAll(Arrays.asList(strs));
    }
}
