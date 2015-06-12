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
package org.daisy.braille.api.embosser;

import java.io.IOException;
import java.io.OutputStream;

import org.daisy.braille.api.table.BrailleConverter;

/**
 * Provides a configurable embosser. Outputs to a single OutputStream. 
 * 
 * @author  Joel Håkansson
 * @version 22 okt 2008
 */
public class ConfigurableEmbosser extends AbstractEmbosserWriter {

	private final LineBreaks breaks;
	private final Padding padNewline;
	private final OutputStream os;
	private final BrailleConverter bf;
	private final byte[] header;
	private final byte[] footer;
	private final boolean fillSheet;
	private final boolean lineFeedOnEmptySheet;
	
	/**
	 * Provides a builder for ConfigurableEmbosser
	 * @author Joel Håkansson
	 */
	public static class Builder {
		// required params
		private OutputStream os;
		private BrailleConverter bt;
		
		// optional params
		private LineBreaks breaks = new StandardLineBreaks(StandardLineBreaks.Type.DEFAULT);
                private PageBreaks pagebreaks = new StandardPageBreaks();
		private Padding padNewline = Padding.values()[0];
		private byte[] header = new byte[0];
		private byte[] footer = new byte[0];
		private boolean fillSheet = false;
		private boolean lineFeedOnEmptySheet = false;
		EmbosserWriterProperties props = new SimpleEmbosserProperties(Integer.MAX_VALUE, Integer.MAX_VALUE);
		
		/**
		 * Creates a new builder with the suppled output stream and braille converter.
		 * @param os the output stream to use
		 * @param bt the braille converter to use
		 */
		public Builder(OutputStream os, BrailleConverter bt) {
			this.os = os;
			this.bt = bt;
		}
		
		/**
		 * Sets the embosser properties for this object.
		 * @param props the embosser properties to use
		 * @return returns this object
		 */
		public Builder embosserProperties(EmbosserWriterProperties props) {
			this.props = props;
			return this;
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
		 * Sets the page break style
		 * @param value the page break style to use
		 * @return returns this object
		 */
		public Builder pagebreaks(PageBreaks value) {
			pagebreaks = value; return this;
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
		 * Sets the communication header
		 * @param value the header to use
		 * @return returns this object
		 */
		public Builder header(byte[] value) { header = value; return this; }
		/**
		 * Sets the communication footer
		 * @param value the footer to use
		 * @return returns this object
		 */
		public Builder footer(byte[] value) { footer = value; return this; }
		/**
		 * Sets the fill sheet policy. Set to true to fill the last sheet by adding a form
		 * feed before closing the communication if the last page has an odd number.
		 * @param value the value
		 * @return returns this object
		 */
		public Builder fillSheet(boolean value) { fillSheet = value; return this; }
		/**
		 * Sets the auto line feed on empty page policy.
		 * @param value set to true, to add line feed on empty page, false otherwise.
		 * @return returns this object
		 */
		public Builder autoLineFeedOnEmptyPage(boolean value) { lineFeedOnEmptySheet = value; return this; }
		/**
		 * Builds a new ConfigurableEmbosser based on this builders current configuration.
		 * @return returns a new ConfigurableEmbosser
		 */
		public ConfigurableEmbosser build() {
			return new ConfigurableEmbosser(this);
		}
	}
	
	protected void formFeed() throws IOException {
		if (lineFeedOnEmptySheet && pageIsEmpty()) {
			lineFeed();
		}
		super.formFeed();
	}

	private ConfigurableEmbosser(Builder builder) {
		bf = builder.bt;
		breaks = builder.breaks;
                pagebreaks = builder.pagebreaks;
		padNewline = builder.padNewline;
		header = builder.header;
		footer = builder.footer;
		os = builder.os;
		fillSheet = builder.fillSheet;
		lineFeedOnEmptySheet = builder.lineFeedOnEmptySheet;
		init(builder.props);
	}

	protected void add(byte b) throws IOException {
		os.write(b);
	}
	
	protected void addAll(byte[] bytes)  throws IOException {
		os.write(bytes);
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
	
	public void open(boolean duplex) throws IOException {
		super.open(duplex);
		os.write(header);
	}
	
	public void close() throws IOException {
		if (fillSheet && supportsDuplex() && currentPage() % 2 == 0) {
			formFeed();
		}
		os.write(footer);
		os.close();
		super.close();
	}

}
