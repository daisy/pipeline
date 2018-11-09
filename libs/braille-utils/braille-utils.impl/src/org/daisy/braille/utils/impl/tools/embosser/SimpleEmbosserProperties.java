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

import org.daisy.dotify.api.embosser.EmbosserWriterProperties;

/**
 * Provides an immutable implementation of {@link EmbosserWriterProperties}.
 * @author Joel Håkansson
 */
public final class SimpleEmbosserProperties implements InternalEmbosserWriterProperties {
	private final double cellWidth;
	private final double cellHeight;
	private final boolean supportsDuplex;
	private final boolean supportsAligning;
	private final int maxHeight;
	private final int maxWidth;

	/**
	 * Provides a builder for simple embosser properties.
	 */
	public static class Builder {
		private final int maxWidth;
		private final int maxHeight;
		
		private double cellWidth = 6;
		private double cellHeight = 10;
		private boolean supportsDuplex=false;
		private boolean supportsAligning=false;
		
		private Builder(int maxWidth, int maxHeight) {
			this.maxWidth = maxWidth;
			this.maxHeight = maxHeight;
		}

		/**
		 * Sets the value of duplex support
		 * @param val the new value
		 * @return returns this object
		 */
		public Builder supportsDuplex(boolean val) {
			supportsDuplex = val;
			return this;
		}

		/**
		 * Sets the value of aligning support
		 * @param val the new value
		 * @return returns this object
		 */
		public Builder supportsAligning(boolean val) {
			supportsAligning = val;
			return this;
		}

		/**
		 * Sets the value of cell width
		 * @param val the new value
		 * @return returns this object
		 */
		public Builder cellWidth(double val) {
			cellWidth = val;
			return this;
		}

		/**
		 * Sets the value of cell height
		 * @param val the new value
		 * @return returns this object
		 */
		public Builder cellHeight(double val) {
			cellHeight = val;
			return this;
		}

		/**
		 * Creates a new instance of {@link SimpleEmbosserProperties} based on the
		 * current state of the builder.
		 * @return returns a new {@link SimpleEmbosserProperties} instance
		 */
		public SimpleEmbosserProperties build() {
			return new SimpleEmbosserProperties(this);
		}
	}

	private SimpleEmbosserProperties(Builder builder) {
		this.maxWidth = builder.maxWidth;
		this.maxHeight = builder.maxHeight;
		this.cellWidth = builder.cellWidth;
		this.cellHeight = builder.cellHeight;
		this.supportsAligning = builder.supportsAligning;
		this.supportsDuplex = builder.supportsDuplex;
	}

	/**
	 * Creates a new SimpleEmbosserProperties builder with all "supports" properties set to false and cell width = 6
	 * and cell height = 10
	 * @param maxWidth the maximum width, in characters
	 * @param maxHeight the maximum height, in rows
	 * @return returns a new builder
	 */
	public static SimpleEmbosserProperties.Builder with(int maxWidth, int maxHeight) {
		return new Builder(maxWidth, maxHeight);
	}

	@Override
	public int getMaxWidth() {
		return maxWidth;
	}

	@Override
	public boolean supportsAligning() {
		return supportsAligning;
	}

	@Override
	public boolean supportsDuplex() {
		return supportsDuplex;
	}

	/**
	 * Gets the cell width, in millimeters
	 * @return returns the cell width, in millimeters
	 */
	public double getCellWidth() {
		return cellWidth;
	}

	/**
	 * Gets the cell height (4 x the vertical dot-to-dot distance), in millimeters
	 * @return returns the cell height, in millimeters
	 */
	public double getCellHeight() {
		return cellHeight;
	}

	@Override
	public int getMaxRowCount() {
		return maxHeight;
	}
}
