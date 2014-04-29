package org.daisy.dotify.api.formatter;

import java.util.logging.Logger;

import org.daisy.dotify.api.translator.TextBorderStyle;

/**
 * Provides properties for a LayoutMaster.
 * 
 * @author Joel Håkansson
 */
public class LayoutMasterProperties {
	private final int flowWidth;
	private final int pageWidth;
	private final int pageHeight;
	private final int innerMargin;
	private final int outerMargin;
	private final float rowSpacing;
	private final boolean duplex;
	private final TextBorderStyle border;

	/**
	 * Configuration class for a ConfigurableLayoutMaster
	 * @author Joel Håkansson
	 *
	 */
	public static class Builder {
		private final int pageWidth;
		private final int pageHeight;

		// optional
		private int innerMargin = 0;
		private int outerMargin = 0;
		private float rowSpacing = 1;
		private boolean duplex = true;
		private TextBorderStyle border;

		public Builder(int pageWidth, int pageHeight) {
			this.pageWidth = pageWidth;
			this.pageHeight = pageHeight;
			border = null;
		}

		public Builder innerMargin(int value) {
			this.innerMargin = value;
			return this;
		}
		
		public Builder outerMargin(int value) {
			this.outerMargin = value;
			return this;
		}
		
		public Builder rowSpacing(float value) {
			if (value < 1) {
				Logger.getLogger(this.getClass().getCanonicalName()).warning("Can't set  rowSpacing < 1: " + value);
			} else {
				this.rowSpacing = value;
			}
			return this;
		}
		
		public Builder duplex(boolean value) {
			this.duplex = value;
			return this;
		}
		
		public Builder border(TextBorderStyle border) {
			this.border = border;
			return this;
		}
		
		public LayoutMasterProperties build() {
			return new LayoutMasterProperties(this);
		}
	}
	
	private LayoutMasterProperties(Builder config) {
		int fsize = 0;
		if (config.border != null) {
			fsize = config.border.getLeftBorder().length() + config.border.getRightBorder().length();
		}
		this.flowWidth = config.pageWidth - config.innerMargin - config.outerMargin - fsize;
		this.pageWidth = config.pageWidth;
		this.pageHeight = config.pageHeight;
		this.innerMargin = config.innerMargin;
		this.outerMargin = config.outerMargin;
		this.rowSpacing = config.rowSpacing;
		this.duplex = config.duplex;
		this.border = config.border;
	}

	public int getPageWidth() {
		return pageWidth;
	}

	public int getPageHeight() {
		return pageHeight;
	}

	public int getFlowWidth() {
		return flowWidth;
	}

	public int getInnerMargin() {
		return innerMargin;
	}

	public int getOuterMargin() {
		return outerMargin;
	}
	
	public float getRowSpacing() {
		return rowSpacing;
	}
	
	public boolean duplex() {
		return duplex;
	}

	public TextBorderStyle getBorder() {
		return border;
	}
}