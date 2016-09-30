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
 * Provides a paper object for cut-sheet paper.
 * @author Joel Håkansson
 */
public class SheetPaper extends AbstractPaper {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4081258552777860442L;
	protected final Length pageWidth, pageHeight;

	/**
	 * Creates a new cut-sheet paper.
	 * @param name a name
	 * @param desc a description
	 * @param identifier an identifier
	 * @param pageWidth the width of the paper in the default orientation
	 * @param pageHeight the height of the paper in the default orientation
	 */
	public SheetPaper(String name, String desc, Enum<? extends Enum<?>> identifier, Length pageWidth, Length pageHeight) {
		super(name, desc, identifier);
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
	}

	SheetPaper(String name, String desc, String identifier, Length pageWidth, Length pageHeight) {
		super(name, desc, identifier);
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
	}

        @Override
	public Type getType() {
		return Type.SHEET;
	}

	/**
	 * Gets the width of the paper in the default orientation
	 * @return returns the width
	 */
	public Length getPageWidth() {
		return pageWidth;
	}

	/**
	 * Gets the height of the paper in default orientation
	 * @return returns the height
	 */
	public Length getPageHeight() {
		return pageHeight;
	}

        @Override
	public SheetPaper asSheetPaper() {
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SheetPaper [pageWidth=" + getPageWidth() + ", pageHeight="
				+ getPageHeight() + "]";
	}

}
