package org.daisy.dotify.api.formatter;

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
	public enum SequenceBreakBefore {
		/**
		 * No break
		 */
		AUTO,
		/**
		 * Start block in a new volume
		 */
		VOLUME};
	private final String masterName;
	private final Integer initialPageNumber;
	private final SequenceBreakBefore breakBefore;
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

}