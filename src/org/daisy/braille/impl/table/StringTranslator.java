/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.impl.table;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Provides a string to string translator.
 * @author Joel Håkansson
 *
 */
public class StringTranslator {
	/**
	 * Defines the mode to use when matching patterns in the StringTranslator.
	 */
	public enum MatchMode {
		/**
		 * Defines the greedy approach to matching, where as much of the input as possible is used
		 * to complete the translation
		 */
		GREEDY,
		/**
		 * Defines the reluctant approach to matching, where as little of the input as possible is used
		 * to complete the translation
		 */
		RELUCTANT
	};
	private final ArrayList<HashMap<String, String>> tokens;
	private MatchMode mode;
	private int currentMax;
	private int tokenCount;

	/**
	 * Creates a new Translator with the match mode set to the supplied value.
	 * @param mode the mode to use
	 */
	public StringTranslator(MatchMode mode) {
		this.tokens = new ArrayList<HashMap<String, String>>();
		this.mode = mode;
		this.currentMax = 0;
		this.tokenCount = 0;
	}
	
	/**
	 * Creates a new Translator with match mode set to "greedy"
	 */
	public StringTranslator() {
		this(MatchMode.GREEDY);
	}

	/**
	 * Adds a token to the list of translations
	 * @param token a token in the string to translate
	 * @param translation the translation
	 * @throws IllegalArgumentException if the token is already in the list 
	 */
	public void addToken(String token, String translation) {
		int len = token.length();
		if (len>currentMax) {
			for (int i=currentMax; i<len; i++) {
				// initialize
				tokens.add(new HashMap<String, String>());
			}
			currentMax = len;
		}
		if (tokens.get(len-1).put(token, translation)!=null) {
			// previous value exists
			throw new IllegalArgumentException("Token already in list: " + token);
		}
		tokenCount++;
	}

	/**
	 * Gets the current match mode
	 * @return returns the current match mode
	 */
	public MatchMode getMatchMode() {
		return mode;
	}

	/**
	 * Sets the current match mode
	 * @param mode the mode to use in subsequent calls to translate()
	 */
	public void setMatchMode(MatchMode mode) {
		this.mode = mode;
	}
	
	/**
	 * Gets the number of tokens in the translator
	 * @return returns the number of tokens
	 */
	public int getTokenCount() {
		return tokenCount;
	}
	
	
	/**
	 * Translates the given string with the specified match mode
	 * @param str the string to translate
	 * @param mode the mode to use
	 * @return returns the translated string
	 * @throws IllegalArgumentException if the entire string could not be translated
	 */
	public String translate(String str, MatchMode mode) {
		String ret;
		if ("".equals(str)) {
			return "";
		}
		switch (mode) {
			case GREEDY:
				ret = translateGreedy(str);
				break;
			case RELUCTANT:
				ret = translateReluctant(str);
				break;
			default:
				throw new IllegalArgumentException("Unknown mode: " + mode);
		}
		if (ret==null) {
			throw new IllegalArgumentException("String could not be translated: '" + str + "'"); 
		}
		return ret;
	}

	/**
	 * Translates the given string with the current match mode. The whole string must be translatable,
	 * or the operation will fail.
	 * @param str the string to translate
	 * @return returns the translated string
	 * @throws IllegalArgumentException if the entire string could not be translated
	 */
	public String translate(String str) {
		return translate(str, mode);
	}

	private String translateReluctant(String str) {
		String val;
		String ret;
		StringBuilder key = new StringBuilder();
		int i = 0;
		for (HashMap<String, String> m : tokens) {
			if (i>=str.length()) {
				// found nothing, stop processing
				break;
			}
			key.append(str.charAt(i));
			val = m.get(key.toString());
			//System.out.println(key + " " + val + " " + i + " " + str);
			if (val!=null) {
				if (str.length()>i+1) {
					ret = translateReluctant(str.substring(i+1));
					if (ret != null) {
						return (val + ret);
					}
				} else {
					return val;
				}
			}
			i++;
		}
		return null;
	}

	private String translateGreedy(String str) {
		String val;
		String ret;
		String key;
		HashMap<String, String> m;
		for (int i=tokens.size()-1; i>=0; i--) {
			if (i>=str.length()) {
				// cannot find anything here, try next map
				continue;
			}
			m = tokens.get(i);
			key = str.substring(0, i+1);
			val = m.get(key.toString());
			//System.out.println(key + " " + val + " " + i + " " + str);
			if (val!=null) {
				if (str.length()>i+1) {
					ret = translateGreedy(str.substring(i+1));
					if (ret != null) {
						return (val + ret);
					}
				} else {
					return val;
				}
			}
		}
		return null;
	}

}
