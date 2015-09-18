package org.daisy.dotify.impl.translator.sv_SE;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

import org.daisy.dotify.common.text.CombinationFilter;
import org.daisy.dotify.common.text.FilterLocale;
import org.daisy.dotify.common.text.RegexFilter;
import org.daisy.dotify.common.text.StringFilter;
import org.daisy.dotify.common.text.UCharFilter;

public class SwedishBrailleFilter implements StringFilter {
	private final static String sv_SE = "sv-SE";
	private final static HashMap<String, FilterLocale> locales;
	static {
		locales = new HashMap<String, FilterLocale>();
		//putLocale("sv");
		putLocale(sv_SE);
	}
	
	private static void putLocale(String str) {
		FilterLocale loc = FilterLocale.parse(str);
		locales.put(loc.toString(), loc);
	}

	private CombinationFilter filters;
	
	public SwedishBrailleFilter() { 
		filters = null;
		setLocale(sv_SE);
	}

	public String filter(String str) {
		return filters.filter(str);
	}

	public boolean supportsLocale(String target) {
		for (FilterLocale loc : locales.values()) {
			if (FilterLocale.parse(target).equals(loc)) {
				return true;
			}
		}
		return false;
	}

	public void setLocale(String t) {
		filters = new CombinationFilter();
		// Remove zero width space
		filters.add(new RegexFilter("\\u200B", ""));
		filters.add(new DigitFilter());
		// Add upper case marker to the beginning of any upper case sequence
		//filters.add(new RegexFilter("(\\p{Lu}[\\p{Lu}\\u00ad]*)", "\u2820$1"));
		// Add another upper case marker if the upper case sequence contains more than one character
		//filters.add(new RegexFilter("(\\u2820\\p{Lu}\\u00ad*\\p{Lu}[\\p{Lu}\\u00ad]*)", "\u2820$1"));
		filters.add(new CapitalizationMarkers());

		if (t.equals(sv_SE)) {
			Locale l = FilterLocale.parse(sv_SE).toLocale();
			// Text to braille, Pas 1
			filters.add(new UCharFilter(getResource("sv_SE-pas1.xml"), l));
			// Text to braille, Pas 2
			filters.add(new UCharFilter(getResource("sv_SE-pas2.xml"), l));
		}
		// Remove redundant whitespace
		filters.add(new RegexFilter("(\\s+)", " "));
	}
	
	/**
	 * Retrieve a URL of a resource associated with this class.
	 */
	final protected URL getResource(String subPath) throws IllegalArgumentException {
		//TODO check the viability of this method
		URL url;
	    url = this.getClass().getResource(subPath);
	    if(null==url) {
	    	String qualifiedPath = this.getClass().getPackage().getName().replace('.','/') + "/";	    	
	    	url = this.getClass().getClassLoader().getResource(qualifiedPath+subPath);
	    }
	    if(url==null) { throw new IllegalArgumentException(subPath); }
	    return url;
	}

}