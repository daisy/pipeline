package org.daisy.dotify.text;

import java.util.ArrayList;



/**
 * A CombinationFilter is a StringFilter that combines several other
 * StringFilters into one.
 * @author joha
 *
 */
public class CombinationFilter extends ArrayList<StringFilter> implements StringFilter {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5284947865448511026L;

	public String filter(String str) {
    	for (StringFilter e : this) {
    		str = e.filter(str);
    	}
    	return str;
    }

}
