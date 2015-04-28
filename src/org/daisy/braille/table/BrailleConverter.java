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
package org.daisy.braille.table;

import java.nio.charset.Charset;

/**
 * Provides an interface for converting from text to braille and vice verca.
 * @author Joel Håkansson
 *
 */
public interface BrailleConverter {
	/**
	 * Transcodes the given text string as braille. This may be a one-to-one mapping or
	 * a many-to-one depending on the table implementation.
	 * @param text
	 * @return returns a Unicode string of braille
	 */
	public String toBraille(String text);
	
	/**
	 * Transcodes the given braille into text.
	 * 
	 * In most cases this will reverse the effect of
	 * toBraille(String text), i.e. text.equals(toText(toBraille(text))), however
	 * an implementation cannot rely on it.
	 * 
	 * Values must be between 0x2800 and 0x28FF.
	 * @param braille
	 * @return returns a text string
	 */
	public String toText(String braille);
	
	/**
	 * Gets the preferred charset for this braille format when reading/writing as text from/to file
 	 * @return returns the preferred charset
	 */
	public Charset getPreferredCharset();

	/**
	 * Returns true if 8-dot braille is supported, false otherwise
	 * @return returns true if 8-dot braille is supported, false otherwise
	 */
	public boolean supportsEightDot();
}
