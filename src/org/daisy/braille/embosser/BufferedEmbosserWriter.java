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
import java.util.List;

import org.daisy.braille.embosser.EmbosserWriterEvent.CloseEvent;
import org.daisy.braille.embosser.EmbosserWriterEvent.NewLineEvent;
import org.daisy.braille.embosser.EmbosserWriterEvent.NewPageEvent;
import org.daisy.braille.embosser.EmbosserWriterEvent.NewSectionAndPageEvent;
import org.daisy.braille.embosser.EmbosserWriterEvent.NewVolumeSectionAndPageEvent;
import org.daisy.braille.embosser.EmbosserWriterEvent.OpenEvent;
import org.daisy.braille.embosser.EmbosserWriterEvent.SetRowGapEvent;
import org.daisy.braille.embosser.EmbosserWriterEvent.WriteEvent;

/**
 * Provides an easy way to add a communications contract to an EmbosserWriter.
 * All events sent to the BufferedEmbosserWriter are kept in memory while the
 * contract information is collected. When the BufferedEmbosserWriter
 * is closed, the underlying EmbosserWriter is opened with a contract
 * matching the events in the communication. The events are then sent 
 * to the underlying EmbosserWriter as they would have been if used directly.
 * @author Joel Håkansson
 *
 */
public class BufferedEmbosserWriter implements EmbosserWriter {
	private final EmbosserWriter writer;
	private List<EmbosserWriterEvent> events;
	private int rowgap;
	private boolean isOpen;
	private boolean isClosed;
	private Contract.Builder contractBuilder;
	
	public BufferedEmbosserWriter(EmbosserWriter writer) {
		this.writer = writer;
		this.isOpen = false;
		this.isClosed = false;
		this.contractBuilder = new Contract.Builder();
	}

	@Override
	public int getMaxWidth() {
		return writer.getMaxWidth();
	}

	@Override
	public int getMaxHeight() {
		return writer.getMaxHeight();
	}

	@Override
	public boolean supportsVolumes() {
		return writer.supportsVolumes();
	}

	@Override
	public boolean supports8dot() {
		return writer.supports8dot();
	}

	@Override
	public boolean supportsDuplex() {
		return writer.supportsDuplex();
	}

	@Override
	public boolean supportsAligning() {
		return writer.supportsAligning();
	}
	
	public boolean supportsZFolding() {
		return writer.supportsZFolding();
	}

	public boolean supportsPrintMode(PrintMode mode) {
		return writer.supportsPrintMode(mode);
	}

	@Override
	public void close() throws IOException {
		isClosed = true;
		isOpen = false;
		events.add(new CloseEvent());
		flush();
	}

	@Override
	public void write(String braille) throws IOException {
		events.add(new WriteEvent(braille));
	}

	@Override
	public void newLine() throws IOException {
		events.add(new NewLineEvent());

	}

	@Override
	public void newPage() throws IOException {
		events.add(new NewPageEvent());
	}

	@Override
	public void newSectionAndPage(boolean duplex) throws IOException {
		events.add(new NewSectionAndPageEvent(duplex));
	}

	@Override
	public void newVolumeSectionAndPage(boolean duplex) throws IOException {
		events.add(new NewVolumeSectionAndPageEvent(duplex));
	}

	@Override
	public void open(boolean duplex) throws IOException {
		try {
			open(duplex, new Contract.Builder().build());
		} catch (ContractNotSupportedException e) {
			// cannot happen
			throw new RuntimeException("Coding error");
		}
	}

	@Override
	public void open(boolean duplex, Contract contract) throws IOException, ContractNotSupportedException {
		rowgap = 0;
		isOpen = true;
		events.add(new OpenEvent(duplex));
		contractBuilder = new Contract.Builder(contract);
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
	public void setRowGap(int value) {
		if (value<0) {
			throw new IllegalArgumentException("Non negative integer expected.");
		} else {
			rowgap = value;
		}
		events.add(new SetRowGapEvent(value));
	}

	@Override
	public int getRowGap() {
		return rowgap;
	}

	private void flush() throws IOException {
		for (EmbosserWriterEvent event : events) {
			switch (event.getEventType()) {
				case OPEN_EVENT:
					try {
						writer.open(((OpenEvent)event).getDuplex(), contractBuilder.build());
					} catch (ContractNotSupportedException e) {
						IOException ex =new IOException("Contract not supported.");
						ex.initCause(e);
						throw ex;
					}
					break;
				case NEW_LINE_EVENT:
					writer.newLine();
					break;
				case NEW_PAGE_EVENT:
					writer.newPage();
					break;
				case NEW_SECTION_AND_PAGE_EVENT: 
					writer.newSectionAndPage(((NewSectionAndPageEvent)event).getDuplex());
					break;
				case NEW_VOLUME_SECTION_AND_PAGE_EVENT:
					writer.newVolumeSectionAndPage(((NewVolumeSectionAndPageEvent)event).getDuplex());
					break;
				case WRITE_EVENT:
					writer.write(((WriteEvent)event).getBraille());
					break;
				case SET_ROWGAP_EVENT:
					writer.setRowGap(((SetRowGapEvent)event).getRowGap());
					break;
				case CLOSE_EVENT:
					writer.close();
					break;
				default:
					throw new RuntimeException("Unknown event: " + event.getEventType());
			}
		}
	}

}
