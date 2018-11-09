package org.daisy.braille.utils.impl.provider.indexbraille;

import org.daisy.dotify.api.paper.Area;
import org.daisy.braille.utils.impl.tools.embosser.EmbosserTools;

class IndexV4Header implements IndexHeader {
	private final Area printableArea;
	private final boolean saddleStitch;
	private final boolean duplex;
	private final boolean zFolding;
	private final boolean transparentMode;
	private final int bindingMargin = 0;
	private final int marginTop;
	private final int numberOfCopies;
	private final double cellWidth;
	private final double cellHeight;
	
	static class Builder implements IndexHeader.Builder {
		private final Area printableArea;
		private boolean saddleStitch = false;
		private boolean duplex = false;
		private boolean zFolding = false;
		private boolean transparentMode = false;
		private int marginTop = 0;
		private int numberOfCopies = 1;
		private double cellWidth = 6;
		private double cellHeight = 10;
		Builder(Area printableArea) {
			this.printableArea = printableArea;
		}

		Builder duplex(boolean value) {
			this.duplex = value;
			return this;
		}
		
		Builder saddleStitch(boolean value) {
			this.saddleStitch = value;
			return this;
		}
		
		Builder zFolding(boolean value) {
			this.zFolding = value;
			return this;
		}
		
		Builder marginTop(int value) {
			this.marginTop = value;
			return this;
		}
		
		Builder numberOfCopies(int value) {
			this.numberOfCopies = value;
			return this;
		}
		
		Builder cellWidth(double value) {
			this.cellWidth = value;
			return this;
		}
		
		Builder cellHeight(double value) {
			this.cellHeight = value;
			return this;
		}

		@Override
		public IndexV4Header build() {
			return new IndexV4Header(this);
		}

		@Override
		public Builder transparentMode(boolean value) {
			this.transparentMode = value;
			return this;
		}
	}

	private IndexV4Header(Builder builder) {
		this.printableArea = builder.printableArea;
		this.saddleStitch = builder.saddleStitch;
		this.duplex = builder.duplex;
		this.zFolding = builder.zFolding;
		this.transparentMode = builder.transparentMode;
		this.marginTop = builder.marginTop;
		this.numberOfCopies = builder.numberOfCopies;
		this.cellHeight = builder.cellHeight;
		this.cellWidth = builder.cellWidth;
	}
	
	@Override
	public byte[] getIndexHeader() {
		StringBuilder header = new StringBuilder();

		header.append((char)0x1b);
		header.append("D");										// Activate temporary formatting properties of a document
		header.append("BT"+(transparentMode?"1":"0"));			// Default braille table
		header.append(",TD0");									// Text dot distance = 2.5 mm
		header.append(",LS50");									// Line spacing = 5 mm
		header.append(",DP");

		// Page mode
		if (saddleStitch && !duplex) {
			header.append('8');
		/*
		} else if (swZFoldingEnabled && !duplex) {
			header.append('7'); 
		} else if (swZFoldingEnabled) {
			header.append('6');
		*/
		} else if (zFolding && !duplex) {
			header.append('5'); 
		} else if (saddleStitch) {
			header.append('4');
		} else if (zFolding) {
			header.append('3');
		} else if (duplex) {
			header.append('2');
		} else {
			header.append('1');
		}
		if (numberOfCopies > 1) {
			header.append(",MC");
			header.append(String.valueOf(numberOfCopies));				// Multiple copies
		}
		//header.append(",MI1");										// Multiple impact = 1
		header.append(",PN0");											// No page number
		header.append(",CH");
		header.append(String.valueOf(getMaxWidth()));					// Characters per line
		header.append(",LP");
		header.append(String.valueOf(getMaxHeight(transparentMode))); // Lines per page
		header.append(",BI");
		header.append(String.valueOf(bindingMargin));					// Binding margin
		header.append(",TM");
		header.append(String.valueOf(marginTop));						// Top margin

		header.append(";");

		return header.toString().getBytes();
	}
	
	@Override
	public int getMaxWidth() {
		return EmbosserTools.getWidth(printableArea, cellWidth);
	}

	private int getMaxHeight(boolean transparentMode) {
		return EmbosserTools.getHeight(printableArea, cellHeight+(transparentMode?2.5:0));
	}

	@Override
	public boolean supportsDuplex() {
		return duplex;
	}

	@Override
	public boolean supportsAligning() {
		return true;
	}

	@Override
	public int getMaxRowCount() {
		return getMaxHeight(transparentMode);
	}

}
