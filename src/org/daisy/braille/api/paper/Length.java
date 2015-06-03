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
package org.daisy.braille.api.paper;

import java.io.Serializable;

/**
 * Provides a length measurement that can be expressed using the
 * preferred units of length.
 * @author Joel Håkansson
 */
public class Length implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6483122895979990975L;

	/**
	 * Defines possible units to be used when expressing a length value 
	 */
	public enum UnitsOfLength {
		/**
		 * Millimeter units
		 */
		MILLIMETER,
		/**
		 * Centimeter units
		 */
		CENTIMETER, 
		/**
		 * Inch units
		 */
		INCH};
	public final static double INCH_IN_MM = 25.4;
	
	private final double originalValue;
	private final double mmValue;
	private final UnitsOfLength unit;
	
	private Length(double originalValue, UnitsOfLength unit) {
		this.originalValue = originalValue;
		this.unit = unit;
		switch (unit) {
		case INCH:
			mmValue = originalValue * INCH_IN_MM;
			break;
		case CENTIMETER:
			mmValue = originalValue * 10;
			break;
		case MILLIMETER: default:
			mmValue = originalValue;
			break;
		}
	}

	/**
	 * Gets the length, expressed in the original units of length.
	 * @return returns the length
	 */
	public double getLength() {
		return originalValue;
	}

	/**
	 * Gets the original units of length.
	 * @return returns the original units of length
	 */
	public UnitsOfLength getUnitsOfLength() {
		return unit;
	}

	/**
	 * Gets the value of this length, expressed in millimeter units
	 * @return returns the value
	 */
	public double asMillimeter() {
		return mmValue;
	}

	/**
	 * Gets the value of this length, expressed in inch units
	 * @return returns the value
	 */
	public double asInches() {
		return mmValue / INCH_IN_MM;
	}

	/**
	 * Creates a new Length object with the specified value, 
	 * expressed in millimeter units
	 * @param value the length in millimeters 
	 * @return returns a new Length object
	 */
	public static Length newMillimeterValue(double value) {
		return new Length(value, UnitsOfLength.MILLIMETER);
	}

	/**
	 * Creates a new Length object with the specified value, 
	 * expressed in centimeter units
	 * @param value the length in centimeter 
	 * @return returns a new Length object
	 */
	public static Length newCentimeterValue(double value) {
		return new Length(value, UnitsOfLength.CENTIMETER);
	}
	
	/**
	 * Creates a new Length object with the specified value, 
	 * expressed in inch units
	 * @param value the length in inches 
	 * @return returns a new Length object
	 */
	public static Length newInchValue(double value) {
		return new Length(value, UnitsOfLength.INCH);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return originalValue + " " + unit.toString().toLowerCase();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) { return true; }
		try {
			Length that = (Length)object;
			return this.mmValue == that.mmValue;
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return 71 * 3 + (int)(Double.doubleToLongBits(mmValue));
	}
}
