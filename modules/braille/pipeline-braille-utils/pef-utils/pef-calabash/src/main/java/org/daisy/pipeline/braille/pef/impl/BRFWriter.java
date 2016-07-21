package org.daisy.pipeline.braille.pef.impl;

import java.io.IOException;

import org.daisy.braille.api.embosser.Contract;
import org.daisy.braille.api.embosser.ContractNotSupportedException;
import org.daisy.braille.api.embosser.EmbosserWriter;
import org.daisy.braille.api.embosser.LineBreaks;
import org.daisy.braille.api.table.BrailleConverter;

/*
 * Initially copied from AbstractEmbosserWriter because it is in a private package
 */
public abstract class BRFWriter implements EmbosserWriter {
	
	public static enum Padding {
		BOTH,
		BEFORE,
		AFTER,
		NONE
	};
	
	public interface PageBreaks {
		public String getString();
	}
	
	private int rowgap;
	private boolean isOpen = false;
	private boolean isClosed = false;
	private boolean currentDuplex;
	private int currentPage;
	private int charsOnRow;
	private int rowsOnPage;
	
	public abstract LineBreaks getLinebreakStyle();
	public abstract PageBreaks getPagebreakStyle();
	public abstract Padding getPaddingStyle();
	public abstract BrailleConverter getTable();
	protected abstract void add(byte b) throws IOException;
	protected abstract void addAll(byte[] b) throws IOException;
	
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
	}
	
	public int currentPage() {
		return currentPage;
	}
	
	public boolean pageIsEmpty() {
		return (charsOnRow+rowsOnPage)==0;
	}
	
	public void close() throws IOException {
		isClosed=true;
		isOpen=false;
	}

	public void write(String braille) throws IOException {
		charsOnRow += braille.length();
		addAll(String.valueOf(getTable().toText(braille)).getBytes(getTable().getPreferredCharset().name()));
	}
	
	protected void lineFeed() throws IOException {
		rowsOnPage++;
		charsOnRow = 0;
		addAll(getLinebreakStyle().getString().getBytes());
	}
	
	protected void formFeed() throws IOException {
		rowsOnPage++;
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
	
	public void newPage() throws IOException {
		if (supportsDuplex() && !currentDuplex && (currentPage % 2)==1) {
			formFeed();
		}
		formFeed();
	}

	public void newSectionAndPage(boolean duplex) throws IOException {
		if (supportsDuplex() && (currentPage % 2)==1) {
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
		return Integer.MAX_VALUE;
	}

	public int getMaxWidth() {
		return Integer.MAX_VALUE;
	}

	public boolean supports8dot() {
		return false;
	}

	public boolean supportsAligning() {
		return false;
	}

	public boolean supportsDuplex() {
		return true;
	}

	public boolean supportsVolumes() {
		return false;
	}
	
	public boolean supportsZFolding() {
		return false;
	}
	
	public boolean supportsPrintMode(PrintMode mode) {
		return false;
	}
}
