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
 * Provides an interface for a collection of Factories.
 * @author Joel Håkansson
 *
 * @param <T> the type of Factories handled by this Provider
 */
public interface Provider<T extends FactoryProperties>{
	
	/**
	 * Lists all Factories
	 * @return returns a collection of Factories
	 */
	public Collection<T> list();

}
