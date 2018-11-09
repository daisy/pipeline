package org.daisy.dotify.api.embosser;

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
	 * @throws IOException if an I/O error occurs
	 */
	public void write(String braille) throws IOException;
	/**
	 * Starts a new line
	 * @throws IOException if an I/O error occurs
	 */
	public void newLine() throws IOException;
	/**
	 * Starts a new page
	 * @throws IOException if an I/O error occurs
	 */
	public void newPage() throws IOException;
	/**
	 * Starts a new page on a blank sheet of paper
	 * with the specified duplex settings.
	 * @param duplex if both sides of sheets should be used, false otherwise
	 * @throws IOException if an I/O error occurs
	 */
	public void newSectionAndPage(boolean duplex) throws IOException;
	/**
	 * Starts a new page on a blank sheet of paper in a new volume
	 * with the specified duplex settings.
	 * @param duplex if both sides of sheets should be used, false otherwise
	 * @throws IOException if an I/O error occurs
	 */
	public void newVolumeSectionAndPage(boolean duplex) throws IOException;
	/**
	 * Opens for writing using the default contract.
	 * @param duplex true if both sides of sheets should be used, false otherwise
	 * @throws IOException if an I/O error occurs
	 */
	public void open(boolean duplex) throws IOException;

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
	 * @param value the row gap
	 */
	public void setRowGap(int value);
	/**
	 * Gets the current row gap, measured as an integer
	 * multiple of the dot-to-dot height.
	 * @return returns the current row gap
	 */
	public int getRowGap();
}
