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
 * Provides a paper format for cut-sheet paper.
 * @author Joel Håkansson
 */
public class SheetPaperFormat extends AbstractPageFormat {
	/**
	 * The width/height orientation of the page
	 */
	public enum Orientation {
		/**
		 *  Represents default orientation as defined by the Paper
		 */
		DEFAULT,
		/**
		 *  Represents reversed orientation as defined by the Paper 
		 */
		REVERSED
	}
	private final Orientation orientation;
	private final Length pageWidth, pageHeight;

	/**
	 * Creates a new cut-sheet paper.
	 * @param paper the paper to use
	 * @param orientation the orientation of the paper
	 */
	public SheetPaperFormat(SheetPaper paper, Orientation orientation) {
		this.pageWidth = paper.getPageWidth();
		this.pageHeight = paper.getPageHeight();
		this.orientation = orientation;
	}
	
	/**
	 * Creates a new cut-sheet paper.
	 * @param pageWidth the paper width in the default orientation
	 * @param pageHeight the paper height in the default orientation
	 */
	public SheetPaperFormat(Length pageWidth, Length pageHeight) {
		this(pageWidth, pageHeight, Orientation.DEFAULT);
	}
	
	/**
	 * Creates a new cut-sheet paper.
	 * @param pageWidth the paper width in the default orientation
	 * @param pageHeight the paper height in the default orientation
	 * @param orientation the orientation
	 */
	public SheetPaperFormat(Length pageWidth, Length pageHeight, Orientation orientation) {
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.orientation = orientation;
	}

	/**
	 * Gets the orientation of this paper format.
	 * @return returns the orientation 
	 */
	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * Gets the page width with respect to the orientation of the paper format
	 * @return returns the width.
	 */
	public Length getPageWidth() {
		switch (orientation) {
			case REVERSED:
				return pageHeight;
			case DEFAULT: default:
				return pageWidth;
		}
	}

	/**
	 * Gets the page height with respect to the orientation of the paper format
	 * @return returns the height.
	 */
	public Length getPageHeight() {
		switch (orientation) {
			case REVERSED:
				return pageWidth;
			case DEFAULT: default:
				return pageHeight;
		}
	}

	public Type getPageFormatType() {
		return Type.SHEET;
	}

	public SheetPaperFormat asSheetPaperFormat() {
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SheetPaperFormat [orientation=" + orientation + ", pageWidth=" + pageWidth
				+ ", pageHeight=" + pageHeight + "]";
	}

}
