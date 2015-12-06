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
 * Provides a paper object for perforated paper with paper guides.
 * @author Joel Håkansson
 */
public class TractorPaper extends AbstractPaper {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7988082460666139365L;
	private final Length across, along;
	
	/**
	 * Creates a new tractor paper
	 * @param name a name
	 * @param desc a description
	 * @param identifier an identifier
	 * @param across the width of the paper
	 * @param along the height of the paper
	 */
	public TractorPaper(String name, String desc, Enum<? extends Enum<?>> identifier, Length across, Length along) {
		super(name, desc, identifier);
		this.across = across;
		this.along = along;
	}
	
	TractorPaper(String name, String desc, String identifier, Length across, Length along) {
		super(name, desc, identifier);
		this.across = across;
		this.along = along;
	}

	/**
	 * Gets the length of the paper perpendicular to the direction of the paper feed
	 * @return returns the length.
	 */
	public Length getLengthAcrossFeed() {
		return across;
	}
	
	/**
	 * Gets the length of the paper along the direction of the paper feed
	 * @return returns the length.
	 */
	public Length getLengthAlongFeed() {
		return along;
	}

        @Override
	public Type getType() {
		return Type.TRACTOR;
	}

        @Override
	public TractorPaper asTractorPaper() {
		return this;
	}

	@Override
	public String toString() {
		return "TractorPaper [lengthAcrossFeed=" + getLengthAcrossFeed() +
                                   ", lengthAlongFeed=" + getLengthAlongFeed() + "]";
	}
}
