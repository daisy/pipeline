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

import org.daisy.factory.AbstractFactory;

/**
 * Provides an abstract base for Table factories.
 * @author Joel Håkansson
 *
 */
public abstract class AbstractTable extends AbstractFactory implements Table {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3604305793559653957L;

	/**
	 * Creates a new AbstractTable with the supplied settings.
	 * @param name the name of the Table 
	 * @param desc the description of the Table
	 * @param identifier the Table identifier
	 */
	public AbstractTable(String name, String desc, String identifier) {
		super(name, desc, identifier);
	}
	
	public AbstractTable(String name, String desc) {
		this(name, desc, null);
	}
	
	public AbstractTable(String name) {
		this(name, "", null);
	}

}
