package org.daisy.dotify.api.formatter;

import java.util.Optional;

/**
 * <p>SequenceProperties stores properties that are specific for a sequence
 * of blocks</p>
 * 
 * <p>The constructor is private, use SequenceProperties.Builder
 * to create new instances.</p>
 * 
 * @author Joel Håkansson 
 */
public class SequenceProperties {
	/**
	 * Provides types of "break before" rules for sequences
	 */
	public enum SequenceBreakBefore {
		/**
		 * Defines that volume breaks may or may not happen before this
		 * sequence begins, depending on its location in the volume
		 */
		AUTO,
		/**
		 * Specifies that the sequence should start in a new volume
		 */
		VOLUME
	}
	private final String masterName;
	private final Integer initialPageNumber;
	private final SequenceBreakBefore breakBefore;
	private final Optional<String> pageCounterName;
	/**
	 * The Builder is used when creating a SequenceProperites instance 
	 * @author Joel Håkansson
	 */
	public static class Builder {
		//Required parameters
		String masterName;
		
		//Optional parameters
		Integer initialPageNumber = null;
		SequenceBreakBefore breakBefore = SequenceBreakBefore.AUTO;
		private String pageCounterName = null;
		
		/**
		 * Create a new Builder
		 * @param masterName the master name for SequenceProperties instances 
		 * created using this Builder
		 */
		public Builder(String masterName) {
			this.masterName = masterName;
		}
		
		/**
		 * Set the initialPageNumber for the SequenceProperties instances
		 * created using this Builder
		 * @param value the value
		 * @return returns the Builder
		 */
		public Builder initialPageNumber(int value) {
			initialPageNumber = value;
			return this;
		}
		
		/**
		 * Set the break before property for the sequence.
		 * @param value the break before type
		 * @return returns "this" object
		 */
		public Builder breakBefore(SequenceBreakBefore value) {
			this.breakBefore = value;
			return this;
		}
		
		/**
		 * Sets the page counter name for the sequence. When this value is
		 * set, pages in the sequence are counted separately (in other words,
		 * not using the default page counter). Instead, pages are counted
		 * together with other sequences having the same page counter name.
		 * 
		 * @param value the identifier
		 * @return returns "this" object
		 */
		public Builder pageCounterName(String value) {
			this.pageCounterName = value;
			return this;
		}
		
		/**
		 * Build SequenceProperties using the current state of the Builder
		 * @return returns a new SequenceProperties instance
		 */
		public SequenceProperties build() {
			return new SequenceProperties(this);
		}
		
	}

	protected SequenceProperties(Builder builder) {
		this.masterName = builder.masterName;
		this.initialPageNumber = builder.initialPageNumber;
		this.breakBefore = builder.breakBefore;
		this.pageCounterName = Optional.ofNullable(builder.pageCounterName);
	}

	/**
	 * Get the name for the LayoutMaster
	 * @return returns the name of the LayoutMaster
	 */
	public String getMasterName() {
		return masterName;
	}
	
	/**
	 * Get the initial page number, i.e. the number that the first page in the sequence should have
	 * @return returns the initial page number, or null if no initial page number has been specified
	 */
	public Integer getInitialPageNumber() {
		return initialPageNumber;
	}
	
	/**
	 * Get break before type
	 * @return returns the break before type
	 */
	public SequenceBreakBefore getBreakBeforeType() {
		return breakBefore;
	}

	/**
	 * Gets the page counter name. If a value is present, page numbers
	 * within this sequence should be counted separately (in other words, 
	 * not using the default page counter). Instead, pages are counted
	 * together with other sequences having the same page counter name.
	 * @return returns the page counter name
	 */
	public Optional<String> getPageCounterName() {
		return pageCounterName;
	}
}