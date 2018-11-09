package org.daisy.dotify.translator.impl.sv_SE;

import java.net.URL;
import java.util.Locale;

import org.daisy.dotify.common.text.CombinationFilter;
import org.daisy.dotify.common.text.FilterLocale;
import org.daisy.dotify.common.text.RegexFilter;
import org.daisy.dotify.common.text.StringFilter;
import org.daisy.dotify.common.text.UCharFilter;

/**
 * Provides a braille filter for Swedish.
 * @author Joel Håkansson
 */
public class SwedishBrailleFilter implements StringFilter {
	private CombinationFilter filters;
	
	/**
	 * Creates a new Swedish braille filter.
	 * @param locale the locale
	 */
	public SwedishBrailleFilter(String locale) { 
		this(locale, false);
	}

	/**
	 * Creates a new Swedish braille filter with the specified mode.
	 * @param locale the locale
	 * @param strict if true the result is braille only, if false the result 
	 * 			contains break point characters such as space, dash and soft hyphen.
	 */
	public SwedishBrailleFilter(String locale, boolean strict) {
		filters = new CombinationFilter();
		// Remove zero width space
		filters.add(new RegexFilter("\\u200B", ""));
		filters.add(new DigitFilter());
		// Add upper case marker to the beginning of any upper case sequence
		//filters.add(new RegexFilter("(\\p{Lu}[\\p{Lu}\\u00ad]*)", "\u2820$1"));
		// Add another upper case marker if the upper case sequence contains more than one character
		//filters.add(new RegexFilter("(\\u2820\\p{Lu}\\u00ad*\\p{Lu}[\\p{Lu}\\u00ad]*)", "\u2820$1"));
		filters.add(new CapitalizationMarkers());

		Locale l = FilterLocale.parse(locale).toLocale();
		// Text to braille, Pas 1
		filters.add(new UCharFilter(getResource("sv_SE-pas1.xml"), l));
		// Text to braille, Pas 2
		filters.add(new UCharFilter(getResource("sv_SE-pas2.xml"), l));
		// Remove redundant whitespace
		filters.add(new RegexFilter("(\\s+)", " "));
		
		if (strict) {
			filters.add(new StrictFilter());
		}
	}

	@Override
	public String filter(String str) {
		return filters.filter(str);
	}
	
	/**
	 * Retrieve a URL of a resource associated with this class.
	 * @param subPath path to the resource
	 * @return returns the url of the resource
	 * @throws IllegalArgumentException if the path cannot be found
	 */
	protected final URL getResource(String subPath) {
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
	
	/**
	 * Processes some characters that are also filtered in the finalizer,
	 * but aren't allowed by the BrailleFilter interface.
	 * @author Joel Håkansson
	 *
	 */
	private static class StrictFilter implements StringFilter {

		@Override
		public String filter(String str) {
			StringBuilder sb = new StringBuilder();
			char[] ca = str.toCharArray();
			for (int i = 0; i<ca.length; i++) {
				switch (ca[i]) {
					case '\u00a0': //NO-BREAK SPACE
						sb.append('\u2800');
						break;
					case '-':
						sb.append('\u2824');
						if (i>0 && i<ca.length-1 &&
								!(ca[i-1]==' ' && ca[i+1]=='⠼')
								) {
							sb.append('\u200B');
						}
						break;
					default:
						sb.append(ca[i]);
				}
			}
			return sb.toString();
		}
		
	}

}