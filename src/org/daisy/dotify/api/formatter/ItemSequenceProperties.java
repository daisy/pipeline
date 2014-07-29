package org.daisy.dotify.api.formatter;

/**
 * <p>Defines properties specific for an item sequence.</p>
 * 
 * @author Joel Håkansson
 */
public class ItemSequenceProperties extends SequenceProperties {
	private final String collectionID;

	/**
	 * Provides a builder for creating ItemSequenceProperties instances.
	 * 
	 * @author Joel Håkansson
	 */
	public static class Builder extends SequenceProperties.Builder {
		private final String collectionID;
		
		/**
		 * Creates a new builder with the supplied arguments.
		 * 
		 * @param masterName the master identifier
		 * @param collectionID the collection identifier
		 */
		public Builder(String masterName, String collectionID) {
			super(masterName);
			this.collectionID = collectionID;
		}
		
		public ItemSequenceProperties build() {
			return new ItemSequenceProperties(this);
		}
	}

	private ItemSequenceProperties(Builder builder) {
		super(builder);
		this.collectionID = builder.collectionID;
	}

	/**
	 * Gets the collection identifier.
	 * @return returns the collection identifier
	 */
	public String getCollectionID() {
		return collectionID;
	}

}
