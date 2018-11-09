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
package org.daisy.braille.utils.pef;

/**
 * Provides a range data object.
 * @author  Joel Håkansson
 */
//May be value based in the future: https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html
public final class Range {
	private final int from;
	private final int to;

	/**
	 * Create a new range.
	 * @param from first page, inclusive
	 * @param to last page, inclusive
	 */
	public Range(int from, int to) {
		if (to<from || from<1 || to<1) {
			throw new IllegalArgumentException("Illegal range: " + from + "-" + to);
		}
		this.from = from;
		this.to = to;
	}

	/**
	 * Create a new range.
	 * @param from first page, inclusive
	 */
	public Range(int from) {
		this(from, Integer.MAX_VALUE);
	}

	/**
	 * Parses the string as a range
	 * @param range the range to parse
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
	 * @param value the value
	 * @return returns true if value is in range, false otherwise
	 */
	public boolean inRange(int value) {
		return value>=from && value<=to;
	}

	/**
	 * Gets the from value, inclusive.
	 * @return returns the from value
	 */
	public int getFrom() {
		return from;
	}

	/**
	 * Gets the to value, inclusive.
	 * @return returns the to value
	 */
	public int getTo() {
		return to;
	}

	@Override
	public String toString() {
		return from + "-" + (to==Integer.MAX_VALUE?"":to);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + to;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Range other = (Range) obj;
		if (from != other.from) {
			return false;
		}
		if (to != other.to) {
			return false;
		}
		return true;
	}

}
