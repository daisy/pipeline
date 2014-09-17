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
package org.daisy.braille;

/**
 * Provides constants for braille.
 * @author Joel Håkansson
 */
public final class BrailleConstants {

	/**
	 * Private constructor to prevent instantiation
	 */
	private BrailleConstants() { }

	/**
	 * String containing the 64 braille patterns in 6 dot braille in Unicode order 						
	 */
	public final static String BRAILLE_PATTERNS_64;
	/**
	 * String containing all 256 braille patterns in Unicode order
	 */
	public final static String BRAILLE_PATTERNS_256;
	
	static {
		StringBuilder tmp = new StringBuilder();
		for (int i=0; i<256; i++) {
			tmp.append((char)(0x2800+i));
		}
		BRAILLE_PATTERNS_64 = tmp.substring(0, 64);
		BRAILLE_PATTERNS_256 = tmp.toString();
	}

}
