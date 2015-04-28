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
package org.daisy.braille.embosser;

/**
 * Provides information about the embosser.
 * @author Joel Håkansson
 */
public interface EmbosserWriterProperties extends EmbosserProperties {

	/**
	 * Gets the maximum row width in the current configuration  
	 * @return returns the maximum row width, in characters
	 */
	public int getMaxWidth();
	
	/**
	 * Gets the maximum page height in the current configuration
	 * @return returns the maximum page height, in rows
	 */
	public int getMaxHeight();
	
}
