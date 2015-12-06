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

import java.util.ArrayList;

/**
 * Provides a sorting algorithm that splits groups of digits and sorts these
 * segments as numbers, for example "sample-1, sample-2, sample-10" will be
 * sorted in that order. String sorting would sort this "sample-1, sample-10, sample-2". 
 * @author Joel Håkansson
 *
 */
public class NumeralSortString implements Comparable<NumeralSortString> {
	private ArrayList<Part> parts;
	private String str;
	
	private static class Part implements Comparable<Part> {
		enum Type {STRING, NUMBER}
		Type type;
		Integer intValue;
		String strValue;

		public Part(String str) {
			try {
				this.intValue = Integer.parseInt(str);
				this.strValue = null;
				type = Type.NUMBER;
			} catch (NumberFormatException e) {
				this.intValue = null;
				this.strValue = str;
				type = Type.STRING;
			}
		}
		
		public Type getType() {
			return type;
		}
		
		public Integer asNumber() {
			return intValue;
		}

		public String asString() {
			return strValue;
		}

                @Override
		public int compareTo(Part otherObj) {
			if (otherObj==null) {
				throw new NullPointerException();
			}
			if (this.getType()==otherObj.getType()) {
				switch (this.getType()) {
					case NUMBER:
						return this.asNumber().compareTo(otherObj.asNumber());
					case STRING:
						return this.asString().compareTo(otherObj.asString());
				}
				return 0;
			} else if (this.getType()==Type.NUMBER) {
				return -1;
			} else {
				return 1;
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((intValue == null) ? 0 : intValue.hashCode());
			result = prime * result
					+ ((strValue == null) ? 0 : strValue.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Part other = (Part) obj;
			if (intValue == null) {
				if (other.intValue != null)
					return false;
			} else if (!intValue.equals(other.intValue))
				return false;
			if (strValue == null) {
				if (other.strValue != null)
					return false;
			} else if (!strValue.equals(other.strValue))
				return false;
			if (type != other.type)
				return false;
			return true;
		}

	}

	/**
	 * Creates a new NumeralSortString for the supplied string
	 * @param str the string to apply numeral sorting on
	 */
	public NumeralSortString(String str) {
		parts = new ArrayList<Part>();
		this.str = str;
		String[] partStr = str.split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");
		for (String part : partStr) {
			parts.add(new Part(part));
		}
	}
	
	/**
	 * Gets the part of the string with the specified index.
	 * @param index index of the part to return
	 * @return returns the part
	 */
	public Part getPart(int index) {
		return parts.get(index);
	}
	
	/**
	 * Gets the number of parts
	 * @return returns the number of parts
	 */
	public int getPartCount() {
		return parts.size();
	}
	
	/**
	 * Gets the value for this object.
	 * @return returns the value
	 */
	public String getValue() {
		return str;
	}

        @Override
	public int compareTo(NumeralSortString otherObj) {
		if (otherObj==null) {
			throw new NullPointerException();
		}
		int thisLen = this.getPartCount();
		int otherLen = otherObj.getPartCount();
		int len = Math.min(thisLen, otherLen);
		for (int i=0; i<len; i++) {
			int c = this.getPart(i).compareTo(otherObj.getPart(i));
			if (c!=0) {
				return c;
			}
		}
		if (thisLen==otherLen) {
			return 0;
		} else if (thisLen < otherLen) {
			return -1;
		} else { 
			return 1;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parts == null) ? 0 : parts.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NumeralSortString other = (NumeralSortString) obj;
		if (parts == null) {
			if (other.parts != null)
				return false;
		} else if (!parts.equals(other.parts))
			return false;
		return true;
	}

}
