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

/**
 * Provides common table features. 
 * @author Joel Håkansson
 */
public class TableProperties {
	/**
	 * Defines that table has a one-to-one mapping between character and braille pattern
	 */
	public static final String IS_ONE_TO_ONE = "is one-to-one";
	/**
	 * Defines that the table is meant for screen use, that is to say, 
	 * does not contain binary type characters.
	 */
	public static final String IS_DISPLAY_FORMAT = "is display format";
}
