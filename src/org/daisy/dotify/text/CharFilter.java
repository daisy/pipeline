package org.daisy.dotify.text;

import java.net.URL;



/**
 * Implements StringFilter using UCharReplacer.
 * 
 * @author  Joel Hakansson
 * @version 4 maj 2009
 * @since 1.0
 */
public class CharFilter implements StringFilter {
	private final SimpleCharReplacer ucr;
	
	/**
	 * Create a new CharFilter
	 * @param table relative path to replacement table, see UCharReplacement for more information
	 */
	public CharFilter(URL table) {
		this.ucr = new SimpleCharReplacer();
		try {
			this.ucr.addSubstitutionTable(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CharFilter(SimpleCharReplacer replacer) {
		this.ucr = replacer;
	}

	public String filter(String str) {
		return ucr.replace(str).toString();
	}

}
