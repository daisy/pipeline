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

import java.io.Serializable;

/**
 * Provides an abstract class for Factories.
 * @author Joel Håkansson
 *
 */
public abstract class AbstractFactory implements Factory, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1845398376520121442L;
	private final String name;
	private final String desc;
	private final String identifier;
	
	/**
	 * Creates a new AbstractFactory with the supplied values
	 * @param name the factory name
	 * @param desc the factory description
	 * @param identifier the factory identifier
	 */
	public AbstractFactory(String name, String desc, String identifier) {
		this.name = name;
		this.desc = desc;
		if (identifier==null) {
			this.identifier = this.toString();
		} else {
			this.identifier = identifier;
		}
	}
	
	/**
	 * Creates a new AbstractFactory with the supplied values
	 * @param name the factory name
	 * @param desc the factory description
	 * @param identifier the factory identifier
	 */
	public AbstractFactory(String name, String desc, Enum<? extends Enum<?>> identifier) {
		this(name, desc, identifier.getClass().getCanonicalName() + "." + identifier.toString());
	}
	
	//jvm1.6@Override
	public String getDescription() {
		return desc;
	}

	//jvm1.6@Override
	public String getDisplayName() {
		return name;
	}

	//jvm1.6@Override
	public String getIdentifier() {
		return identifier;
	}
	
	public int compareTo(Factory o) {
		if (this.equals(o)) {
			return 0;
		} else {
			return this.getDisplayName().compareTo(o.getDisplayName());
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractFactory [name=" + name + ", desc=" + desc
				+ ", identifier=" + identifier + "]";
	}

}