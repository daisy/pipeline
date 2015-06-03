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


/**
 * Provides a paper object for paper in rolls.
 * @author Joel Håkansson
 */
public class RollPaper extends AbstractPaper {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6739289682441153310L;
	private final Length across;
	
	/**
	 * Creates a new roll paper
	 * @param name a name for the paper
	 * @param desc a description
	 * @param identifier an identifier
	 * @param across the height of the roll
	 */
	public RollPaper(String name, String desc, Enum<? extends Enum<?>> identifier, Length across) {
		super(name, desc, identifier);
		this.across = across;
	}
	
	RollPaper(String name, String desc, String identifier, Length across) {
		super(name, desc, identifier);
		this.across = across;
	}

	/**
	 * Gets the length of the paper perpendicular to the direction of the paper feed
	 * @return returns the length, in mm.
	 */
	public Length getLengthAcrossFeed() {
		return across;
	}

	public Type getType() {
		return Type.ROLL;
	}
	
	public RollPaper asRollPaper() {
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RollPaper [lengthAcrossFeed=" + getLengthAcrossFeed() + "]";
	}
}
