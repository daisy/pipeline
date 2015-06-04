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
package org.daisy.braille.api.table;

import java.nio.charset.Charset;
import java.util.HashMap;

import org.daisy.braille.api.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.daisy.braille.tools.StringTranslator;
import org.daisy.braille.tools.StringTranslator.MatchMode;

/**
 * Provides an advanced Braille converter mapping each braille character to a string.
 * @author Joel Håkansson
 * @author Bert Frees
 */
public class AdvancedBrailleConverter implements BrailleConverter {
	
	private HashMap<Character, String> b2t;
	private StringTranslator t2b;
	private final Charset charset;
	private final EightDotFallbackMethod fallback;
	private final Character replacement;
	private final boolean ignoreCase;
	private final boolean supports8dot;
	
	/**
	 * Creates a new 6-dot table. Each string in the table array represents a braille character.
	 * The string at index <tt>i</tt> in the array is interpreted as the 
	 * translation for the braille character with Unicode value 0x2800+i.
	 * @param table the table data, 64 entries
	 * @param charset the preferred charset as defined in the BrailleConverter interface
	 * @param fallback the fallback method to use when encountering a character in the range 0x2840-0x28FF
	 * @param replacement the replacement character, must be in the range 0x2800-0x283F
	 * @param ignoreCase set to true to ignore character case
	 * @param mode the match mode to use
	 * @throws throws IllegalArgumentException if the table length isn't equal to 64
	 */
	public AdvancedBrailleConverter(String[] table, Charset charset, EightDotFallbackMethod fallback, char replacement, boolean ignoreCase, MatchMode mode) {
		this(table, charset, fallback, replacement, ignoreCase, false, mode);
	}
	
	/**
	 * Creates a new 8-dot table. Each string in the table array represents a braille character.
	 * The string at index <tt>i</tt> in the array is interpreted as the 
	 * translation for the braille character with Unicode value 0x2800+i.
	 * @param table the table data, 256 entries
	 * @param charset the preferred charset as defined in the BrailleConverter interface
	 * @param ignoreCase set to true to ignore character case
	 * @param mode the match mode to use
	 * @throws throws IllegalArgumentException if the table length isn't equal to 256
	 */
	public AdvancedBrailleConverter(String[] table, Charset charset, boolean ignoreCase, MatchMode mode) {
		this(table, charset, EightDotFallbackMethod.MASK, null, ignoreCase, true, mode);
	}

	private AdvancedBrailleConverter(String[] table, Charset charset, EightDotFallbackMethod fallback, Character replacement, boolean ignoreCase, boolean dot, MatchMode mode) {
		if (table.length!=64 && !dot) {
			throw new IllegalArgumentException("Unsupported table length: " + table.length);
		}
		if (table.length!=256 && dot) {
			throw new IllegalArgumentException("Unsupported table length: " + table.length);
		}
		this.charset = charset;
		this.fallback = fallback;
		this.replacement = replacement;
		this.ignoreCase = ignoreCase;
		this.supports8dot = table.length == 256;
		b2t = new HashMap<Character, String>();
		t2b = new StringTranslator(mode);
		int i = 0;
		char b;
		for (String t : table) {
			b = (char) (0x2800 + i);
			put(b, t);
			i++;
		}
	}

	private void put(char braille, String glyphs) {
		if (ignoreCase) {
			t2b.addToken(glyphs.toLowerCase(), ""+braille);
		} else {
			t2b.addToken(glyphs, ""+braille);
		}
		b2t.put(braille, glyphs);
	}

	public Charset getPreferredCharset() {
		return charset;
	}

	public boolean supportsEightDot() {
		return supports8dot;
	}

	public String toBraille(String text) {
		if (ignoreCase) {
			text = text.toLowerCase();
		}
		String ret = t2b.translate(text);
		return ret;
	}

	private String toText(char braillePattern) {
		if (b2t.get(braillePattern) == null) {
			int val = (braillePattern + "").codePointAt(0);
			if (val >= 0x2840 && val <= 0x28FF) {
				switch (fallback) {
					case MASK:
						return toText((char) (val & 0x283F));
					case REPLACE:
						if (b2t.get(replacement) != null) {
							return toText(replacement);
						} else {
							throw new IllegalArgumentException("Replacement char not found.");
						}
					case REMOVE:
						return null;
				}
			} else {
				throw new IllegalArgumentException("Braille pattern '" + braillePattern + "' not found.");
			}
		}
		return (b2t.get(braillePattern));
	}

	public String toText(String braille) {
		StringBuffer sb = new StringBuffer();
		String t;
		for (char c : braille.toCharArray()) {
			t = toText(c);
			if (t != null) {
				sb.append(t);
			}
		}
		return sb.toString();
	}

}
