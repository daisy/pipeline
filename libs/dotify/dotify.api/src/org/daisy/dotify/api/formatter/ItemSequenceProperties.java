package org.daisy.dotify.api.formatter;


/**
 * <p>Defines properties specific for an item sequence.</p>
 * 
 * @author Joel Håkansson
 */
//TODO:rename class, since it is not a sequence any more
public class ItemSequenceProperties {
	/**
	 * Defines  ranges.
	 */
	public enum Range {
		/**
		 * Defines the range to include the entire document
		 */
		DOCUMENT,
		/**
		 * Defines the range to include entries within the volume
		 */
		VOLUME}
		
	private final String collectionID;
	private final Range range;

	/**
	 * Provides a builder for creating ItemSequenceProperties instances.
	 * 
	 * @author Joel Håkansson
	 */
	public static class Builder {
		private final String collectionID;
		private final Range range;
		
		/**
		 * Creates a new builder with the supplied arguments.
		 * 
		 * @param collectionID the collection identifier
		 * @param range the range
		 */
		public Builder(String collectionID, Range range) {
			this.collectionID = collectionID;
			this.range = range;
		}
		
		/**
		 * Builds a new item sequence properties.
		 * @return returns a new item sequence properties
		 */
		public ItemSequenceProperties build() {
			return new ItemSequenceProperties(this);
		}
	}

	private ItemSequenceProperties(Builder builder) {
		this.collectionID = builder.collectionID;
		this.range = builder.range;
	}

	/**
	 * Gets the collection identifier.
	 * @return returns the collection identifier
	 */
	public String getCollectionID() {
		return collectionID;
	}

	/**
	 * Gets the range of this item sequence
	 * @return returns the range
	 */
	public Range getRange() {
		return range;
	}
	
}
