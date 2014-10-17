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
package org.daisy.factory;

import java.util.Collection;

/**
 * Provides an interface for factory catalogs.
 * @author Joel Håkansson
 *
 * @param <T> the type of factory objects that this catalog contains
 */
public interface FactoryCatalog<T extends Factory, S extends FactoryProperties> extends Provider<S> {
	
	/**
	 * Gets the Factory with this identifier
	 * @param identifier the identifier for the requested Factory
	 * @return returns the Factory with this identifier, or null if none is found
	 */
	public T get(String identifier);
	
	/**
	 * Lists the Factories available to this catalog that the
	 * supplied FactoryFilter accepts
	 * @param filter the FactoryFilter to use
	 * @return returns a collection of Factories
	 */
	public Collection<S> list(FactoryFilter filter);

}
