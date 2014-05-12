package org.daisy.dotify.text;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;



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
	 * @param autoComplete adds upper/lower case entries with the same value, where missing according to the specified locale
	 */
	public CharFilter(URL table, Locale autoComplete) {
		this.ucr = new SimpleCharReplacer();
		try {
			this.ucr.addSubstitutionTable(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (autoComplete!=null) {
			Map<Integer, String> add = new HashMap<Integer, String>();
			String substitute;
			String codePointStr;
			String newStr;
			for (Integer codePoint : ucr.keySet()) {
				substitute = ucr.get(codePoint);
				if (substitute!=null) {
					codePointStr = new String(Character.toChars(codePoint));
					if (codePointStr.equals((newStr = codePointStr.toUpperCase(autoComplete)).toLowerCase(autoComplete))) {
						if (newStr.codePointCount(0, newStr.length()) == 1) {
							int uppercase = newStr.codePointAt(0);
							if (!ucr.containsKey(uppercase)) {
								add.put(uppercase, substitute);
							}
						}
					} else if (codePointStr.equals((newStr = codePointStr.toLowerCase(autoComplete)).toUpperCase(autoComplete))) {
						if (newStr.codePointCount(0, newStr.length()) == 1) {
							int lowercase = newStr.codePointAt(0);
							if (!ucr.containsKey(lowercase)) {
								add.put(lowercase, substitute);
							}
						}
					}
				}
			}
			ucr.putAll(add);
		}
	}

	public CharFilter(SimpleCharReplacer replacer) {
		this.ucr = replacer;
	}

	public String filter(String str) {
		return ucr.replace(str).toString();
	}

}
