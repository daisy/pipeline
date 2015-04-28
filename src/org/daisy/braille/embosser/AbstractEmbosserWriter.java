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

import java.io.IOException;

import org.daisy.braille.table.BrailleConverter;

/**
 * Provides an abstract base for embossers.
 * @author Joel Håkansson
 *
 */
public abstract class AbstractEmbosserWriter implements EmbosserWriter {
	/**
	 * Defines form feed padding style.
	 */
	public static enum Padding {
		/**
		 * Pad both before and after form feed.
		 */
		BOTH,
		/**
		 * Pad only before form feed.
		 */
		BEFORE,
		/**
		 * Pad only after form feed.
		 */
		AFTER, 
		/**
		 * Do not pad form feed.
		 */
		NONE
	};

	private int rowgap;
	private boolean isOpen;
	private boolean isClosed;
	private boolean currentDuplex;
	private int currentPage;
	private int charsOnRow;
	private int rowsOnPage;
	private EmbosserWriterProperties props;
    protected PageBreaks pagebreaks = new StandardPageBreaks();

	/**
	 * Gets the line break style for the EmbosserWriter
	 * @return returns the line break style for the EmbosserWriter
	 */
	public abstract LineBreaks getLinebreakStyle();
	/**
	 * Gets the form feed padding style for the EmbosserWriter
	 * @return returns the padding style for the EmbosserWriter
	 */
	public abstract Padding getPaddingStyle();
	/**
	 * Gets the table for the EmbosserWriter
	 * @return returns the table for the EmbosserWriter
	 */
	public abstract BrailleConverter getTable();
	/**
	 * Adds a byte to the EmbosserWriter output.
	 * @param b the byte to add
	 * @throws IOException if IO fails.
	 */
	protected abstract void add(byte b) throws IOException;
	/**
	 * Adds bytes to the EmbosserWriter output.
	 * @param b the bytes to add
	 * @throws IOException if IO fails
	 */
	protected abstract void addAll(byte[] b) throws IOException;

	protected void init(EmbosserWriterProperties props) {
		this.props = props;
		isOpen = false;
		isClosed = false;
	}

	public void newLine() throws IOException {
		for (int i=0; i<((rowgap / 4)+1); i++) {
			lineFeed();
		}
	}

	public void setRowGap(int value) {
		if (value<0) {
			throw new IllegalArgumentException("Non negative integer expected.");
		} else {
			rowgap = value;
		}
	}

	public int getRowGap() {
		return rowgap;
	}

	public void open(boolean duplex) throws IOException {
		try {
			open(duplex, new Contract.Builder().build());
		} catch (ContractNotSupportedException e) {
			IOException ex = new IOException("Could not open embosser.");
			ex.initCause(e);
			throw ex;
		}
	}

	public void open(boolean duplex, Contract contract) throws IOException, ContractNotSupportedException {
		charsOnRow = 0;
		rowsOnPage = 0;
		rowgap = 0;
		currentPage = 1;
		isOpen=true;
		currentDuplex = duplex;
		// Contract does not affect the implementation here, subclasses should override this method,
		// to make use of contract information
		
	}

	/**
	 * Gets the current page number, where the first page is 1.
	 * @return returns the current page number
	 */
	public int currentPage() {
		return currentPage;
	}

	/**
	 * Returns true if page is empty
	 * @return returns true if page is empty
	 */
	public boolean pageIsEmpty() {
		return (charsOnRow+rowsOnPage)==0;
	}

	public void close() throws IOException {
		isClosed=true;
		isOpen=false;
	}

	public void write(String braille) throws IOException {
		charsOnRow += braille.length();
		if (charsOnRow>props.getMaxWidth()) {
			throw new IOException("The maximum number of characters on a row was exceeded (page is too narrow).");
		}
		addAll(String.valueOf(getTable().toText(braille)).getBytes(getTable().getPreferredCharset().name()));
	}

	/**
	 * Performs a line feed on the EmbosserWriter
	 * @throws IOException if IO fails
	 */
	protected void lineFeed() throws IOException {
		rowsOnPage++;
		charsOnRow = 0;
		addAll(getLinebreakStyle().getString().getBytes());
	}

	/**
	 * Performs a form feed on the EmbosserWriter
	 * @throws IOException if IO fails
	 */
	protected void formFeed() throws IOException {
		rowsOnPage++;
		if (rowsOnPage>props.getMaxHeight()) {
			throw new IOException("The maximum number of rows on a page was exceeded (page is too short)");
		}
		switch (getPaddingStyle()) {
			case BEFORE:
				lineFeed();
			case NONE:
				addAll(getPagebreakStyle().getString().getBytes());
				break;
			case BOTH:
				lineFeed();
			case AFTER:
				addAll(getPagebreakStyle().getString().getBytes());
				lineFeed();
				break;

                

		}
		currentPage++;
		rowsOnPage = 0;
		charsOnRow = 0;
	}

        protected PageBreaks getPagebreakStyle() {
            return pagebreaks;
        }

	public void newPage() throws IOException {
		if (props.supportsDuplex() && !currentDuplex && (currentPage % 2)==1) {
			formFeed();
		}
		formFeed();
	}

	public void newSectionAndPage(boolean duplex) throws IOException {
		if (props.supportsDuplex() && (currentPage % 2)==1) {
			formFeed();
		}
		newPage();
		currentDuplex = duplex;
	}

	public void newVolumeSectionAndPage(boolean duplex) throws IOException {
		newSectionAndPage(duplex);
	}

	public boolean isOpen() {
		return isOpen;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public int getMaxHeight() {
		return props.getMaxHeight();
	}

	public int getMaxWidth() {
		return props.getMaxWidth();
	}

	public boolean supports8dot() {
		return props.supports8dot();
	}

	public boolean supportsAligning() {
		return props.supportsAligning();
	}

	public boolean supportsDuplex() {
		return props.supportsDuplex();
	}

	public boolean supportsVolumes() {
		return props.supportsVolumes();
	}
	
	public boolean supportsZFolding() {
		return props.supportsZFolding();
	}
	
	public boolean supportsPrintMode(PrintMode mode) {
		return props.supportsPrintMode(mode);
	}
	


}
