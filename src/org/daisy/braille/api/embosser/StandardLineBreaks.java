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
package org.daisy.braille.api.embosser;


/**
 * Provides standard line break definitions
 * @author Joel Håkansson
 */
public class StandardLineBreaks implements LineBreaks {
	/**
	 * Defines standard line break types
	 */
	public static enum Type {
		/**
		 * Indicates windows/dos line breaks
		 */
		DOS,
		/**
		 * Indicates unix line breaks
		 */
		UNIX,
		/**
		 * Indicates classic mac line breaks
		 */
		MAC,
		/**
		 * Indicates system default line breaks
		 */
		DEFAULT
	};
	private final String newline;
	
	/**
	 * Creates a new object with the system's default line break style.
	 */
	public StandardLineBreaks() {
		this(Type.DEFAULT);
	}

	/**
	 * Creates a new object with the specified line break style
	 * @param t the type of line break
	 */
	public StandardLineBreaks(Type t) {
		newline = getString(t);
	}

	public String getString() {
		return newline;
	}
	/**
	 * Gets the string used to represent line breaks
	 * @param t the type of line breaks
	 * @return returns the string used to represent line breaks
	 */
	public static String getString(Type t) {
        switch (t) {
	    	case UNIX: return "\n";
	    	case DOS: return "\r\n";
	    	case MAC: return "\r";
	    	default: return System.getProperty("line.separator", "\r\n");
        }
	}
}
