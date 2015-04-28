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




/**
 * PageFormat extends a Paper with options selected by a user (if applicable).
 * @author Joel Håkansson
 *
 */
public interface PageFormat {
	/**
	 * Defines page format types
	 */
	public enum Type {
		/**
		 * Defines a cut-sheet paper format. Implementation must extend SheetPaperFormat 
		 */
		SHEET,
		/**
		 * Defines a tractor paper format. Implementation must extend TractorPaperFormat
		 */
		TRACTOR, 
		/**
		 * Defines a roll paper format. Implementation must extend RollPaperFormat
		 */
		ROLL};
	
	/**
	 * Gets the paper in this paper format
	 * @return returns the type
	 */
	public Type getPageFormatType();
	
	/**
	 * Returns this PageFormat as a SheetPaperFormat
	 * @return returns the SheetPaperFormat
	 * @throws ClassCastException if the instance is not SheetPaperFormat
	 */
	public SheetPaperFormat asSheetPaperFormat();
	
	/**
	 * Returns this PageFormat as a TractorPaperFormat
	 * @return returns the TractorPaperFormat
	 * @throws ClassCastException if the instance is not TractorPaperFormat
	 */
	public TractorPaperFormat asTractorPaperFormat();
	
	/**
	 * Returns this PageFormat as a RollPaperFormat
	 * @return returns the RollPaperFormat
	 * @throws ClassCastException if the instance is not RollPaperFormat
	 */
	public RollPaperFormat asRollPaperFormat();

}
