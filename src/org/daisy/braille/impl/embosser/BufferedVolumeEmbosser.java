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
package org.daisy.braille.impl.embosser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import javax.print.PrintException;

import org.daisy.braille.api.embosser.Device;
import org.daisy.braille.api.embosser.EmbosserWriterProperties;
import org.daisy.braille.api.embosser.LineBreaks;
import org.daisy.braille.api.embosser.StandardLineBreaks;
import org.daisy.braille.api.table.BrailleConverter;

/**
 * Provides a buffered volume embossers. This is similar to {@link ConfigurableEmbosser},
 * except that it supports writing each volume separately to a PrinterDevice rather than
 * to an OutputStream. 
 * 
 * @author  Joel Håkansson
 */
public class BufferedVolumeEmbosser extends AbstractEmbosserWriter {

	private LineBreaks breaks;
	private Padding padNewline;
	private Device pd;
	private BrailleConverter bf;
	private Stack<ArrayList<Byte>> pages;
	private VolumeWriter vw;
	private final boolean lineFeedOnEmptySheet;
	
	/**
	 * Provides a builder for a BufferedVolumeEmbosser
	 * @author Joel Håkansson
	 */
	public static class Builder {
		// required params
		private final Device pd;
		private final BrailleConverter bt;
		private final VolumeWriter vw;
		private final EmbosserWriterProperties ep;
		
		// optional params
		private LineBreaks breaks = new StandardLineBreaks();
		private Padding padNewline = Padding.values()[0];
		private boolean lineFeedOnEmptySheet = false;

		/**
		 * Creates a new Builder
		 * @param pd the Device to use
		 * @param bt the BrailleConverter to use
		 * @param vw the VolumeWriter to use
		 * @param ep the EmbosserWriterProperties to use
		 */
		public Builder(Device pd, BrailleConverter bt, VolumeWriter vw, EmbosserWriterProperties ep) {
			this.pd = pd;
			this.bt = bt;
			this.vw = vw;
			this.ep = ep;
		}
		/**
		 * Sets the line break style
		 * @param value one of dos, unix, mac or default
		 * @return returns this object
		 */
		public Builder breaks(String value) { 
			if (value!=null && !"".equals(value)) {
				return breaks(new StandardLineBreaks(StandardLineBreaks.Type.valueOf(value.toUpperCase())));
			}
			return this;
		}
		/**
		 * Sets the line break style
		 * @param value the line break style to use
		 * @return returns this object
		 */
		public Builder breaks(LineBreaks value) {
			breaks = value; return this;
		}
		/**
		 * Sets the padding style
		 * @param value a padding style
		 * @return returns this object
		 */
		public Builder padNewline(String value) {
			if (value!=null && !"".equals(value)) {
				return padNewline(Padding.valueOf(value.toUpperCase()));
			}
			return this;
		}
		/**
		 * Sets the padding style
		 * @param value the padding style to use
		 * @return returns this object
		 */
		public Builder padNewline(Padding value) { padNewline = value; return this; }
		/**
		 * Sets the auto line feed on empty page policy.
		 * @param value set to true, to add line feed on empty page, false otherwise.
		 * @return returns this object
		 */
		public Builder autoLineFeedOnEmptyPage(boolean value) { lineFeedOnEmptySheet = value; return this; }
		
		/**
		 * Builds a new BufferedVolumeEmbosser based on this builders current configuration.
		 * @return returns a new BufferedVolumeEmbosser
		 */
		public BufferedVolumeEmbosser build() {
			return new BufferedVolumeEmbosser(this);
		}
	}
	
	private BufferedVolumeEmbosser(Builder builder) {
		vw = builder.vw;
		bf = builder.bt;
		breaks = builder.breaks;
		padNewline = builder.padNewline;
		lineFeedOnEmptySheet = builder.lineFeedOnEmptySheet;
		pd = builder.pd;
		init(builder.ep);
	}
	
	public void open(boolean duplex) throws IOException {
		super.open(duplex);
		initVolume();
	}
	
	private void initVolume() {
		pages = new Stack<ArrayList<Byte>>();
		pages.add(new ArrayList<Byte>());
	}
	
	public BrailleConverter getTable() {
		return bf;
	}
	
	public LineBreaks getLinebreakStyle() {
		return breaks;
	}
	
	public Padding getPaddingStyle() {
		return padNewline;
	}

	protected void add(byte b) {
		pages.peek().add(b);
	}
	
	protected void addAll(byte[] bytes) {
		ArrayList<Byte> page = pages.peek();
		for (byte b : bytes) {
			page.add(b);
		}
	}

	protected void formFeed() throws IOException {
		if (lineFeedOnEmptySheet && pageIsEmpty()) {
			lineFeed();
		}
		super.formFeed(); // form feed characters belong to the current page
        pages.add(new ArrayList<Byte>()); // start a new page
	}

	public void newVolumeSectionAndPage(boolean duplex) throws IOException {
		super.newVolumeSectionAndPage(duplex);
		finalizeVolume();
		initVolume();
	}
	
	private void finalizeVolume() throws IOException {
		File out = File.createTempFile("emboss", ".tmp");
		pages.pop();
		vw.write(pages, out);
		try {
			pd.transmit(out);
		} catch (PrintException e) {
			IOException e2 = new IOException();
			e2.initCause(e);
			throw e2;
		} finally {
			out.deleteOnExit();
		}
	}
	
	public void close() throws IOException {
		finalizeVolume();
		super.close();
	}

}
