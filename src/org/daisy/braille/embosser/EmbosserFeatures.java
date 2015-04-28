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
package org.daisy.braille.embosser;

import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.daisy.paper.PageFormat;

/**
 * Provides common embosser features to be used when configuring an Embosser factory.
 * @author Joel Håkansson
 *
 */
public class EmbosserFeatures {
	/**
	 * Embosser feature key,
	 * corresponding value should be a number, in millimeters
	 */
	public static final String CELL_WIDTH = "cellWidth";
	/**
	 * Embosser feature key,
	 * corresponding value should be a number, in millimeters
	 */
	public static final String CELL_HEIGHT = "cellHeight";
	/**
	 * Embosser feature key,
	 * corresponding value should match a value in {@link EightDotFallbackMethod}
	 */
	public static final String UNSUPPORTED_CELL_FALLBACK_METHOD = "fallback";
	/**
	 * Embosser feature key,
	 * corresponding value should be a character in the range 0x2800-0x283F
	 */
	public static final String UNSUPPORTED_CELL_REPLACEMENT = "replacement";
	/**
	 * Embosser feature key,
	 * corresponding value should match a table identifier
	 */
	public static final String TABLE = "table";
	/**
	 * Embosser feature key,
	 * corresponding value should be a {@link PageFormat} object
	 */
	public static final String PAGE_FORMAT = "pageFormat";

        public static final String NUMBER_OF_COPIES = "copies";
        public static final String SADDLE_STITCH = "saddleStitch";
        public static final String Z_FOLDING = "zFolding";
        public static final String DUPLEX = "duplex";
        public static final String PAGES_IN_QUIRE = "pagesInQuire";
}
