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
 * Provides a default implementation for Paper.
 * @author Joel Håkansson
 */
public abstract class AbstractPaper implements Paper, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1622983592321625679L;
	private final String name;
	private final String desc;
	private final String identifier;
	
	/**
	 * Creates a new paper.
	 * @param name the name of the paper
	 * @param desc the description of the paper
	 * @param identifier the identifier
	 */
	public AbstractPaper(String name, String desc, Enum<? extends Enum<?>> identifier) {
		this(name, desc, identifier.getClass().getCanonicalName() + "." + identifier.toString());
	}
	
	/**
	 * Creates a new paper.
	 * @param name the name of the paper
	 * @param desc the description of the paper
	 * @param identifier the identifier
	 */
	public AbstractPaper(String name, String desc, String identifier) {
		if (identifier==null) {
			throw new NullPointerException("Null identifier.");
		}
		this.name = name;
		this.desc = desc;
		this.identifier = identifier;
	}


	@Override
	public String getDescription() {
		return desc;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	/* (non-Javadoc)
	 * @see org.daisy.braille.api.paper.Paper#asSheetPaper()
	 */
	public SheetPaper asSheetPaper() {
		throw new ClassCastException();
	}

	/* (non-Javadoc)
	 * @see org.daisy.braille.api.paper.Paper#asTractorPaper()
	 */
	public TractorPaper asTractorPaper() {
		throw new ClassCastException();
	}

	/* (non-Javadoc)
	 * @see org.daisy.braille.api.paper.Paper#asRollPaper()
	 */
	public RollPaper asRollPaper() {
		throw new ClassCastException();
	}

}
