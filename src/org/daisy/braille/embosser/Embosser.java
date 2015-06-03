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

import java.io.OutputStream;

import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableFilter;
import org.daisy.factory.Factory;
import org.daisy.paper.Area;
import org.daisy.paper.PageFormat;
import org.daisy.paper.Paper;

/**
 * Provides an interface for common properties of an Embosser.
 * @author Joel Håkansson
 */
public interface Embosser extends Factory, EmbosserProperties {

	/**
	 * Returns true if dimension is supported
	 * @param printPage the dimension to test
	 * @return returns true if dimension is supported
	 */
        //TODO: check whether this method needs to be public / is needed altogether
	public boolean supportsPrintPage(PrintPage printPage);
	
	public boolean supportsPageFormat(PageFormat pageFormat);
	
	public boolean supportsPaper(Paper paper);
	
	/**
	 * Returns true if table is supported
	 * @param table the table to test
	 * @return returns true if table is supported
	 */
	public boolean supportsTable(Table table);
	
	/**
	 * Gets a table filter that returns true if supportsTable returns true
	 * @return returns a table filter
	 */
	public TableFilter getTableFilter();

	/**
	 * Gets a new EmbosserWriter that writes to the supplied OutputStream
	 * @param os the OutputStream that the EmbosserWriter should use
	 * @return returns a new EmbosserWriter
	 */
	public EmbosserWriter newEmbosserWriter(OutputStream os);
	
	/**
	 * Gets a new EmbosserWriter that writes to the supplied Device
	 * @param device the device that the EmbosserWriter should use
	 * @return returns a new EmbosserWriter
	 */
	public EmbosserWriter newEmbosserWriter(Device device);

	/**
	 * Gets the max width for the specified page format
	 * @param pageFormat the page format
	 * @return returns the max width for the specified page format
	 */
	public int getMaxWidth(PageFormat pageFormat);
	
	/**
	 * Gets the max height for the specified page format
	 * @param pageFormat the page format
	 * @return returns the max height for the specified page format
	 */
	public int getMaxHeight(PageFormat pageFormat);

        /**
	 * Gets the dimensions of one print page for the specified page format
         * @param pageFormat the page format
         * @return returns the dimensions of one print page for the specified page format
	 */
        public PrintPage getPrintPage(PageFormat pageFormat);

        /**
	 * Gets the printable area for the specified page format
         * @param pageFormat the page format
         * @return returns the printable area for the specified page format
	 */
        public Area getPrintableArea(PageFormat pageFormat);

}
