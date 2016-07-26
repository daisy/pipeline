package org.daisy.dotify.formatter.impl;

import java.io.Closeable;
import java.io.IOException;

import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.Row;

/**
 * Provides a method for writing pages to a PagedMediaWriter.
 * @author Joel Håkansson
 */
class WriterHandler implements Closeable {
	private final PagedMediaWriter writer;
	
	WriterHandler(PagedMediaWriter writer) {
		this.writer = writer;
	}
	/**
	 * Writes this structure to the suppled PagedMediaWriter.
	 * @param writer the PagedMediaWriter to write to
	 * @throws IOException if IO fails
	 */
	void write(Iterable<? extends Volume> volumes) {
		for (Volume v : volumes) {
			writeVolume(v);
		}
	}

	private void writeVolume(Volume v) {
		boolean firstInVolume = true;
		for (Section s : v.getSections()) {
			if (firstInVolume) {
				firstInVolume = false;
				writer.newVolume(s.getSectionProperties());
			}
			writeSection(s);
		}
	}

	private void writeSection(Section s) {
		writer.newSection(s.getSectionProperties());
		for (Page p : s.getPages()) {
			writePage(p);
		}
	}

	private void writePage(Page p) {
		writer.newPage();
		for (Row r : p.getRows()) {
			writer.newRow(r);
		}
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

}