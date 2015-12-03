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
	private final String masterName;
	private final Integer initialPageNumber;
	
	/**
	 * The Builder is used when creating a SequenceProperites instance 
	 * @author Joel Håkansson
	 */
	public static class Builder {
		//Required parameters
		String masterName;
		
		//Optional parameters
		Integer initialPageNumber = null;
		
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

}