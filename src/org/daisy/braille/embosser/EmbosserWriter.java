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

import java.io.Closeable;
import java.io.IOException;

/**
 * Provides an embosser communication interface. Communication is 
 * flat. For example, only one of these should be called when
 * starting a new page:
 * <ul>
 * <li>newPage</li>
 * <li>newSectionAndPage</li>
 * <li>newVolumeSectionAndPage</li>
 * </ul>
 * 
 * @author  Joel Håkansson
 */
public interface EmbosserWriter extends EmbosserWriterProperties, Closeable {
	/**
	 * Writes a string of braille to the embosser.
	 * Values must be between 0x2800 and 0x28FF. An implementation may supply
	 * a complete row of braille in a single chunk. However, an implementation
	 * may also call this method repeatedly without any other calls in between.
	 * @param braille characters in the range 0x2800 to 0x28FF 
	 * @throws IOException
	 */
	public void write(String braille) throws IOException;
	/**
	 * Starts a new line
	 * @throws IOException
	 */
	public void newLine() throws IOException;
	/**
	 * Starts a new page
	 * @throws IOException
	 */
	public void newPage() throws IOException;
	/** 
	 * Starts a new page on a blank sheet of paper 
	 * with the specified duplex settings.
	 * @param duplex
	 * @throws IOException
	 */
	public void newSectionAndPage(boolean duplex) throws IOException;
	/**
	 * Starts a new page on a blank sheet of paper in a new volume
	 * with the specified duplex settings.
	 * @param duplex
	 * @throws IOException
	 */
	public void newVolumeSectionAndPage(boolean duplex) throws IOException;
	/**
	 * Opens for writing using the default contract
	 * @throws IOException if an I/O exception of some sort has occurred
	 */
	public void open(boolean duplex) throws IOException;
	
	/**
	 * Opens for writing
	 * @param duplex
	 * @param contract
	 * @throws IOException if an I/O exception of some sort has occurred
	 * @throws ContractNotSupportedException if the supplied contract is not supported, that is to say
	 * if the contract does not contain information required by the implementation
	 */
	public void open(boolean duplex, Contract contract) throws IOException, ContractNotSupportedException;

	/**
	 * Returns true if embosser is open
	 * @return returns true if embosser is open, false otherwise
	 */
	public boolean isOpen();
	/**
	 * Tests if embosser has been closed
	 * @return returns true if the embosser has been open, but is now closed, false otherwise
	 */
	public boolean isClosed();
	/**
	 * Sets the row gap for following calls to newLine
	 * to the specified value, measured as an 
	 * integer multiple of the dot-to-dot height.
	 * @param value
	 */
	public void setRowGap(int value);
	/**
	 * Gets the current row gap, measured as an integer
	 * multiple of the dot-to-dot height.
	 * @return returns the current row gap
	 */
	public int getRowGap();
}
