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


/**
 * Provides a TableProvider which creates tables a specific type of
 * identifier
 * @author Joel Håkansson
 *
 * @param <T> Identifier type used when creating a new Table
 */
public interface ConfigurableTableProvider<T> extends TableProvider {

	/**
	 * Gets a feature for the provider
	 * @param key the key
	 * @return returns the value
	 * @throws IllegalArgumentException if the feature is unknown 
	 */
	public Object getFeature(String key);
	
	/**
	 * Sets a feature for the provider.
	 * @param key the key
	 * @param value the value
	 * @throws IllegalArgumentException if the feature is unknown
	 */
	public void setFeature(String key, Object value);

	/**
	 * Gets a new BrailleConverter based on the factory's current settings.
	 * @param t the type to return
	 * @return returns a new BrailleConverter of type t
	 * @throws IllegalArgumentException if the type is unknown
	 */
	public BrailleConverter newTable(T t);

}
