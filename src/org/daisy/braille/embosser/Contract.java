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
/**
 * Provides a usage contract that allows an implementation of 
 * EmbosserWriter to optimize or configure communication based on actual
 * properties of the expected communication.
 * @author Joel Håkansson
 *
 */
public class Contract {
	/**
	 * Defines a braille range
	 */
	enum BrailleRange {
		/** 
		 * The braille range in this contract is undefined
		 */
		UNDEFINED,
		/**
		 * Only 6-dot braille characters will be sent to the embosser
		 */
		SIX_DOT,
		/**
		 * At least one 8-dot braille character will be sent to the embosser
		 */
		EIGHT_DOT
	};
	/**
	 * Defines a page mode
	 */
	enum PageMode {
		/**
		 * Undefined
		 */
		UNDEFINED,
		/**
		 * Only duplex will be used
		 */
		DUPLEX,
		/**
		 * Only simplex will be used
		 */
		SIMPLEX,
		/**
		 * Both duplex and simplex <em>will</em> be used
		 */
		BOTH
	}
	
	/**
	 * Provides a builder for Contract
	 */
	public static class Builder {
		// optional
		private BrailleRange range = BrailleRange.UNDEFINED;
		private Integer pages = null;
		
		/**
		 * Creates a new Builder with no specified Contract properties
		 */
		public Builder() { }
		
		/**
		 * Creates a new Builder using the specification in the supplied
		 * contract.
		 * @param contract the contract to use
		 */
		public Builder(Contract contract) {
			if (contract.getBrailleRange()!=BrailleRange.UNDEFINED) {
				this.range = contract.getBrailleRange();
			}
			if (contract.getPages()!=null) {
				this.pages = contract.getPages();
			}
		}
		/**
		 * Sets the braille range for Contracts created using this builder
		 * @param value the braille range
		 * @return returns this object
		 */
		public Builder setBrailleRange(BrailleRange value) {
			this.range = value;
			return this;
		}
		/**
		 * Sets the number of pages for Contracts created using this builder
		 * @param value the number of pages
		 * @return returns this object
		 */
		public Builder setPages(Integer value) {
			this.pages = value;
			return this;
		}
		/**
		 * Builds a new Contract based on this builders current configuration.
		 * @return returns a new Contract
		 */
		public Contract build() {
			return new Contract(this);
		}
	}

	private final BrailleRange range;
	private final Integer pages;

	private Contract(Builder builder) {
		this.range = builder.range;
		this.pages = builder.pages;
	}

	/**
	 * Gets the braille range in this Contract
	 * @return returns the braille range
	 */
	public BrailleRange getBrailleRange() {
		return range;
	}
	
	/**
	 * Gets the number of pages in this contract
	 * @return returns the number of pages, or null if not set
	 */
	public Integer getPages() {
		return pages;
	}

}
