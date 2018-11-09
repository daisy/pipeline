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
package org.daisy.braille.utils.impl.tools.embosser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.LineBreaks;

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
	protected InternalEmbosserWriterProperties props;
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
	 * Adds bytes to the EmbosserWriter output.
	 * @param b the bytes to add
	 * @throws IOException if IO fails
	 */
	protected abstract void addAll(byte[] b) throws IOException;

	protected void init(InternalEmbosserWriterProperties props) {
		this.props = props;
		isOpen = false;
		isClosed = false;
	}

	@Override
	public void newLine() throws IOException {
		for (int i=0; i<((rowgap / 4)+1); i++) {
			lineFeed();
		}
	}

	@Override
	public void setRowGap(int value) {
		if (value<0) {
			throw new IllegalArgumentException("Non negative integer expected.");
		} else {
			rowgap = value;
		}
	}

	@Override
	public int getRowGap() {
		return rowgap;
	}

	@Override
	public void open(boolean duplex) throws IOException {
		init(duplex);
	}
	
	private void init(boolean duplex) {
		charsOnRow = 0;
		rowsOnPage = 0;
		rowgap = 0;
		currentPage = 1;
		isOpen=true;
		currentDuplex = duplex;
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

	@Override
	public void close() throws IOException {
		isClosed=true;
		isOpen=false;
	}

	@Override
	public void write(String braille) throws IOException {
		charsOnRow += braille.length();
		if (charsOnRow>props.getMaxWidth()) {
			throw new IOException("The maximum number of characters on a row was exceeded (page is too narrow).");
		}
		addAll(getBytes(braille));
	}
	
	/**
	 * Translates a string of braille into bytes that should be transfered to the embosser.
	 * @param braille the braille (characters in the Braille Patterns unicode block 0x2800-0x28FF).
	 * @return the bytes
	 * @throws UnsupportedEncodingException if the string could not be encoded.
	 */
	//FIXME: don't throw UnsupportedEncodingException, throw something more appropriate.
	public abstract byte[] getBytes(String braille) throws UnsupportedEncodingException;

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
		if (rowsOnPage>props.getMaxRowCount()) {
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

	@Override
	public void newPage() throws IOException {
		if (props.supportsDuplex() && !currentDuplex && (currentPage % 2)==1) {
			formFeed();
		}
		formFeed();
	}

	@Override
	public void newSectionAndPage(boolean duplex) throws IOException {
		if (props.supportsDuplex() && (currentPage % 2)==1) {
			formFeed();
		}
		newPage();
		currentDuplex = duplex;
	}

	@Override
	public void newVolumeSectionAndPage(boolean duplex) throws IOException {
		newSectionAndPage(duplex);
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public boolean isClosed() {
		return isClosed;
	}

	@Override
	public int getMaxWidth() {
		return props.getMaxWidth();
	}

	@Override
	public boolean supportsAligning() {
		return props.supportsAligning();
	}

}
