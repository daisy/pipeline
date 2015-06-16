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

import java.nio.charset.Charset;
import java.util.HashMap;

import org.daisy.braille.api.table.BrailleConverter;

/**
 * Provides a simple TableConverter implementation. It 
 * provides a one-to-one mapping between braille and
 * text and vice versa.
 * @author Joel Håkansson
 */
public class EmbosserBrailleConverter implements BrailleConverter {
	/**
	 * Defines the fallback action when a character in the range 0x2840-0x28FF is
	 * encountered.
	 */
	public enum EightDotFallbackMethod {
		/**
		 * Mask the character. Treat it as if dots 7 and 8 were off
		 */
		MASK,
		/**
		 * Replace the character with a fixed replacement character 
		 */
		REPLACE,
		/**
		 * Remove the character from output
		 */
		REMOVE
	}; // , FAIL
	private HashMap<Character, Character> b2t;
	private HashMap<Character, Character> t2b;
	private Charset charset;
	private EightDotFallbackMethod fallback;
	private char replacement;
	private boolean ignoreCase;
	private boolean supports8dot;
	
	/**
	 * Creates a new EmbosserBrailleConverter
	 * @param table the characters in the table, in Unicode order. Must contain 64 or 256 characters.
	 * @param charset the preferred charset as defined in the BrailleConverter interface
	 * @param fallback the fallback method to use when encountering a character in the range 0x2840-0x28FF
	 * @param replacement the replacement character, must be in the range 0x2800-0x283F
	 * @param ignoreCase set to true to ignore character case
	 * @throws throws IllegalArgumentException if the table length isn't equal to 64 or 256.
	 */
	public EmbosserBrailleConverter(String table, Charset charset, EightDotFallbackMethod fallback, char replacement, boolean ignoreCase) {
		char[] tableDef = table.toCharArray();
		this.charset = charset;
		this.fallback = fallback;
		this.replacement = replacement;
		this.ignoreCase = ignoreCase;
		this.supports8dot = tableDef.length==256;
		if (tableDef.length!=64 && tableDef.length!=256) {
			throw new IllegalArgumentException("Unsupported table length: " + table.length());
		}
		b2t = new HashMap<Character, Character>();
		t2b = new HashMap<Character, Character>();
		//lower case def.
		int i = 0;
		char b;
		for (char t : tableDef) {
			b = (char)(0x2800+i);
			put(b, t);
			i++;
		}
	}

	private void put(char braille, char glyph) {
		if (ignoreCase) {
			t2b.put(Character.toLowerCase(glyph), braille);
		} else {
			t2b.put(glyph, braille);
		}
		b2t.put(braille, glyph);
	}

	public Charset getPreferredCharset() {
		return charset;
	}
	
	public boolean supportsEightDot() {
		return supports8dot;
	}

	private char toBraille(char c) {
		if (ignoreCase) {
			c = Character.toLowerCase(c);
		}
		if (t2b.get(c)==null) throw new IllegalArgumentException("Character '" + c + "' (0x" + Integer.toHexString((int)(c)) + ") not found.");
		return (t2b.get(c));
	}

	public String toBraille(String text) {
		StringBuffer sb = new StringBuffer();
		for (char c : text.toCharArray()) {
			sb.append(toBraille(c));
		}
		return sb.toString();
	}

	private Character toText(char braillePattern) {
		if (b2t.get(braillePattern)==null) {
			int val = (braillePattern+"").codePointAt(0);
			if (val>=0x2840 && val<=0x28FF) {
				switch (fallback) {
					case MASK:
						return toText((char)(val&0x283F));
					case REPLACE:
						if (b2t.get(replacement)!=null) {
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
		Character t;
		for (char c : braille.toCharArray()) {
			t = toText(c);
			if (t!=null) {
				sb.append(t);
			}
		}
		return sb.toString();
	}

}
