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
package org.daisy.braille.pef;

/**
 * Provides a range data object.
 * @author  Joel Håkansson
 */
public class Range {
	private int from;
	private int to;
	
	/**
	 * Create a new range.
	 * @param from first page, inclusive
	 * @param to last page, inclusive
	 */
	public Range(int from, int to) {
		init(from, to);
	}
	
	/**
	 * Create a new range.
	 * @param from first page, inclusive
	 */
	public Range(int from) {
		init(from, Integer.MAX_VALUE);
	}
	
	private void init(int from, int to) {
		if (to<from || from<1 || to<1) {
			throw new IllegalArgumentException("Illegal range: " + from + "-" + to);
		}
		this.from = from;
		this.to = to;
	}
	
	/**
	 * Parses the string as a range
	 * @param range
	 * @return returns a Range object
	 * @throws NumberFormatException if the range cannot be parsed
	 */
	public static Range parseRange(String range) {
		String[] str = range.split("-");
		if (str.length==1) {
			if (range.indexOf("-")>0){
				return new Range(Integer.parseInt(str[0]));
			} else {
				return new Range(Integer.parseInt(str[0]), Integer.parseInt(str[0]));
			}
		} else {
			if ("".equals(str[0])) {
				return new Range(1, Integer.parseInt(str[1]));
			} else {
				return new Range(Integer.parseInt(str[0]), Integer.parseInt(str[1]));
			}
		}
	}
	
	/**
	 * Test if a value is in range
	 * @param value
	 * @return returns true if value is in range, false otherwise
	 */
	public boolean inRange(int value) {
		if (value>=from && value<=to) return true;
		return false;
	}
}
