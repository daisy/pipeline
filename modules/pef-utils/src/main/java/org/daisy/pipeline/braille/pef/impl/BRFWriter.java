package org.daisy.pipeline.braille.pef.impl;

import java.io.IOException;
import java.nio.charset.Charset;

import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.LineBreaks;
import org.daisy.dotify.api.table.BrailleConverter;

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
	public abstract Charset getCharset();
	public abstract BrailleConverter getTable();
	protected abstract void add(byte b) throws IOException;
	protected abstract void addAll(byte[] b) throws IOException;
	
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
		charsOnRow = 0;
		rowsOnPage = 0;
		rowgap = 0;
		currentPage = 1;
		isOpen=true;
		currentDuplex = duplex;
	}
	
	@Override
	public void close() throws IOException {
		isClosed=true;
		isOpen=false;
	}

	@Override
	public void write(String braille) throws IOException {
		charsOnRow += braille.length();
		addAll(String.valueOf(getTable().toText(braille)).getBytes(getCharset().name()));
	}
	
	private void lineFeed() throws IOException {
		rowsOnPage++;
		charsOnRow = 0;
		addAll(getLinebreakStyle().getString().getBytes());
	}
	
	private void formFeed() throws IOException {
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
	
	@Override
	public void newPage() throws IOException {
		if (!currentDuplex && (currentPage % 2)==1) {
			formFeed();
		}
		formFeed();
	}

	@Override
	public void newSectionAndPage(boolean duplex) throws IOException {
		if ((currentPage % 2)==1) {
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
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean supportsAligning() {
		return false;
	}
}
