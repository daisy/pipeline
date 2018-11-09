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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides a usage contract that allows an implementation of
 * EmbosserWriter to optimize or configure the communication based on
 * properties of the actual data.
 * @author Joel Håkansson
 *
 */
public class InternalContract {
	/**
	 * Defines a braille range
	 */
	public enum BrailleRange {
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
	public enum PageMode {
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
		private Set<Integer> rowGaps = new HashSet<>();

		/**
		 * Creates a new Builder with no specified Contract properties
		 */
		public Builder() { }

		/**
		 * Creates a new Builder using the specification in the supplied
		 * contract.
		 * @param contract the contract to use
		 */
		public Builder(InternalContract contract) {
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
		 * Adds a row gap value to the builder.
		 * @param value the value
		 * @return returns the value
		 */
		public Builder addRowGap(Integer value) {
			this.rowGaps.add(value);
			return this;
		}
		/**
		 * Builds a new Contract based on this builders current configuration.
		 * @return returns a new Contract
		 */
		public InternalContract build() {
			return new InternalContract(this);
		}
	}

	private final BrailleRange range;
	private final Integer pages;
	private final Set<Integer> rowGaps;
	private final boolean simpleRowGaps;

	private InternalContract(Builder builder) {
		this.range = builder.range;
		this.pages = builder.pages;
		this.rowGaps = Collections.unmodifiableSet(new HashSet<>(builder.rowGaps));
		this.simpleRowGaps = (range==BrailleRange.SIX_DOT) ?
				rowGaps.stream().allMatch(v -> v%4==0) :
				((range==BrailleRange.EIGHT_DOT) ? rowGaps.stream().allMatch(v -> (v-1)%5==0) : false);
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
	
	/**
	 * Gets the row gaps in this contract
	 * @return returns the row gaps that will be used
	 */
	public Set<Integer> getRowGaps() {
		return rowGaps;
	}
	
	/**
	 * Returns true if all row gaps in this contract are equal to full lines,
	 * In other words, row gaps that can be replaced with new lines.
	 * @return true if all row gaps are full lines, false otherwise
	 */
	public boolean onlySimpleRowgaps() {
		return simpleRowGaps;
	}

}
