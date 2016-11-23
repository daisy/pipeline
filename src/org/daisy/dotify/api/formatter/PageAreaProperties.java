package org.daisy.dotify.api.formatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

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
		BOTTOM
	}
	private final Alignment align;
	private final String collectionId;
	private final int maxHeight;
	private final List<FallbackRule> fallbackRules;

	/**
	 * Creates a new page area properties builder
	 */
	public static class Builder {
		//required
		private final String collectionId;
		private final int maxHeight;
		
		//optional
		private Alignment align = Alignment.BOTTOM;
		private final List<FallbackRule> fallbackRules;
		
		/**
		 * Creates a new instance with the specified parameters
		 * @param collectionId an identifier of the collection used
		 * for this page area
		 * @param maxHeight the maximum height of this page area
		 */
		public Builder(String collectionId, int maxHeight) {
			this.collectionId = collectionId;
			this.maxHeight = maxHeight;
			this.fallbackRules = new ArrayList<>();
		}
		
		/**
		 * Sets the alignment of the page area on the page.
		 * @param value the alignment
		 * @return returns this builder
		 */
		public Builder align(Alignment value) {
			this.align = value;
			return this;
		}
		
		/**
		 * Adds a fallback rule.
		 * @param rule the rule
		 * @return returns this builder
		 */
		public Builder addFallback(FallbackRule rule) {
			this.fallbackRules.add(rule);
			return this;
		}
		
		/**
		 * Creates a new page area properties object
		 * @return returns a new page area properties instance
		 */
		public PageAreaProperties build() {
			return new PageAreaProperties(this);
		}
		
	}
	
	private PageAreaProperties(Builder builder) {
		this.align = builder.align;
		this.collectionId = builder.collectionId;
		this.maxHeight = builder.maxHeight;
		this.fallbackRules = builder.fallbackRules;
		validateFallbackRules();
	}
	
	private void validateFallbackRules() {
		if (!fallbackRules.isEmpty()) {
			boolean found = false;
			Set<String> str = new HashSet<>();
			for (FallbackRule r : fallbackRules) {
				if (!str.add(r.applyToCollection())) {
					Logger.getLogger(this.getClass().getCanonicalName()).warning("Multiple rules for the same collection: " + r.applyToCollection());
				}
				if (collectionId.equals(r.applyToCollection())) {
					found = true;
				} else if (r.mustBeContextCollection()) {
					throw new IllegalArgumentException("This rule (" + r + ") can only be applied to the collection with id: " + collectionId);
				}
			}
			if (!found) {
				throw new IllegalArgumentException("The fallback rules must include a rule for the collection that triggered the fallback action (" + collectionId + ")");
			}
		}
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
	 * Gets the fallback rules.
	 * @return returns the fallback rules
	 */
	public Iterable<FallbackRule> getFallbackRules() {
		return fallbackRules;
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
