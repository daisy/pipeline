package org.daisy.dotify.api.formatter;

/**
 * Specifies an area of the page where collection items can be placed. The
 * page area can be located before the text body, below the header or after
 * the text body, above the footer.
 * 
 * @author Joel Håkansson
 */
public class PageAreaProperties {
	/**
	 * Specifies the alignment of the page area.
	 */
	public enum Alignment {
		/**
		 * Aligns toward the top of the page, below the header
		 */
		TOP,
		/**
		 * Aligns toward the bottom of the page, above the footer
		 */
		BOTTOM};
	
	/**
	 * Specifies the scope of the fallback action. 
	 *
	 */
	public enum FallbackScope {
		/**
		 * Specifies that all items in the collection should be reassigned,
		 * if at least one item cannot be rendered in its designated page area.
		 */
		ALL,
		/**
		 * Specified that items on the same page in the collection should be
		 * reassigned, if at least one item on the page cannot be rendered in
		 * the page area.
		 */
		PAGE};
		
	private final Alignment align;
	private final FallbackScope scope;
	private final String collectionId;
	private final int maxHeight;
	private final String fallbackId;

	public static class Builder {
		//required
		private final String collectionId;
		private final int maxHeight;
		
		//optional
		private Alignment align = Alignment.BOTTOM;
		private FallbackScope scope = FallbackScope.PAGE;
		private String fallbackId = null;
		
		public Builder(String collectionId, int maxHeight) {
			this.collectionId = collectionId;
			this.maxHeight = maxHeight;
		}
		
		public Builder align(Alignment value) {
			this.align = value;
			return this;
		}
		
		public Builder scope(FallbackScope value) {
			this.scope = value;
			return this;
		}
		
		public Builder fallbackId(String value) {
			this.fallbackId = value;
			return this;
		}
		
		public PageAreaProperties build() {
			return new PageAreaProperties(this);
		}
		
	}
	
	private PageAreaProperties(Builder builder) {
		this.align = builder.align;
		this.collectionId = builder.collectionId;
		this.maxHeight = builder.maxHeight;
		this.fallbackId = builder.fallbackId;
		this.scope = builder.scope;
	}
	/**
	 * Gets the alignment of the page area.
	 * @return returns the alignment
	 */
	public Alignment getAlignment() {
		return align;
	}
	
	/**
	 * Gets the id of the associated collection.
	 * @return returns the id of the associated collection
	 */
	public String getCollectionId() {
		return collectionId;
	}
	
	/**
	 * Gets the id of a fallback collection. Note that the
	 * collection id need not refer to an existing collection.
	 * 
	 * @return returns the id of the fallback collection
	 */
	public String getFallbackId() {
		return fallbackId;
	}
	
	/**
	 * Gets the scope of the fallback action.
	 * @return returns the scope of the fallback action
	 */
	public FallbackScope getFallbackScope() {
		return scope;
	}
	
	/**
	 * Gets the maximum height allowed to be used by the page
	 * area, in rows.
	 * @return returns the maximum height of the page area in rows
	 */
	public int getMaxHeight() {
		return maxHeight;
	}
}
