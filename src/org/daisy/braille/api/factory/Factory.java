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
package org.daisy.braille.api.factory;

/**
 * Provides an interface for common properties of a Factory.
 * @author Joel Håkansson
 *
 */
public interface Factory extends FactoryProperties {

	/**
	 * Gets the value of a read-only property that applies to all objects returned
	 * by this Factory.
	 * @param key the name of the property to get
	 * @return returns the value associated with this property or null if none is found 
	 */
	public Object getProperty(String key);
	/**
	 * Gets the value of a feature used by this Factory
	 * @param key the key for the feature
	 * @return returns the current value of the feature
	 * @throws IllegalArgumentException if the underlying implementation does not recognize the feature
	 */
	public Object getFeature(String key);
	/**
	 * Sets a feature for new Objects returned by this Factory
	 * @param key the key for the feature
	 * @param value the value of the feature
	 * @throws IllegalArgumentException if the underlying implementation does not recognize the feature
	 */
	public void setFeature(String key, Object value);

}
