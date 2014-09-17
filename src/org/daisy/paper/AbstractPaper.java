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
package org.daisy.paper;

import org.daisy.factory.AbstractFactory;

/**
 * Provides a default implementation for Paper.
 * @author Joel Håkansson
 */
public abstract class AbstractPaper extends AbstractFactory implements Paper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3698214577768375057L;

	/**
	 * Creates a new paper.
	 * @param name the name of the paper
	 * @param desc the description of the paper
	 * @param identifier the identifier
	 */
	public AbstractPaper(String name, String desc, Enum<? extends Enum<?>> identifier) {
		super(name, desc, identifier);
	}
	
	/**
	 * Creates a new paper.
	 * @param name the name of the paper
	 * @param desc the description of the paper
	 * @param identifier the identifier
	 */
	public AbstractPaper(String name, String desc, String identifier) {
		super(name, desc, identifier);
	}

	//jvm1.6@Override
	public Object getFeature(String key) {
		throw new IllegalArgumentException("Unknown feature: " + key);
	}

	//jvm1.6@Override
	public Object getProperty(String key) {
		return null;
	}

	//jvm1.6@Override
	public void setFeature(String key, Object value) {
		throw new IllegalArgumentException("Unknown feature: " + key);
	}

	/* (non-Javadoc)
	 * @see org.daisy.paper.Paper#asSheetPaper()
	 */
	public SheetPaper asSheetPaper() {
		throw new ClassCastException();
	}

	/* (non-Javadoc)
	 * @see org.daisy.paper.Paper#asTractorPaper()
	 */
	public TractorPaper asTractorPaper() {
		throw new ClassCastException();
	}

	/* (non-Javadoc)
	 * @see org.daisy.paper.Paper#asRollPaper()
	 */
	public RollPaper asRollPaper() {
		throw new ClassCastException();
	}

}
