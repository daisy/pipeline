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

import org.daisy.braille.tools.Length;

/**
 * Provides a paper format for paper in rolls.
 * @author Joel Håkansson
 */
public class RollPaperFormat extends AbstractPageFormat {
	private final Length across, along;
	
	/**
	 * Creates a new roll paper format
	 * @param paper the roll paper to use
	 * @param length the cut-length
	 */
	public RollPaperFormat(RollPaper paper, Length length) {
		this.across = paper.getLengthAcrossFeed();
		this.along = length;
	}
	
	/**
	 * Creates a new roll paper format
	 * @param acrossPaperFeed the height of the roll
	 * @param alongPaperFeed the cut-length
	 */
	public RollPaperFormat(Length acrossPaperFeed, Length alongPaperFeed) {
		this.across = acrossPaperFeed;
		this.along = alongPaperFeed;
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
	
	public Type getPageFormatType() {
		return Type.ROLL;
	}
	
	public RollPaperFormat asRollPaperFormat() {
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RollPaperFormat [across=" + across + ", along=" + along + "]";
	}

}
