package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.formatter.FormattingTypes.Alignment;

/**
 * Provides text block properties. This class is immutable.
 * @author Joel Håkansson
 *
 */
public final class TextBlockProperties {
	private final int textIndent;
	private final int firstLineIndent;
	private final Float rowSpacing;
	private final Alignment align;
	private final String identifier;
	
	/**
	 * Provides a builder for text block properties
	 */
	public static class Builder {
		private int textIndent = 0;
		private int firstLineIndent = 0;
		private Float rowSpacing = null;
		private Alignment align = Alignment.LEFT;
		private String identifier = "";
		
		/**
		 * Set the text indent for the block, in characters.
		 * The text indent controls the indent of all text rows except 
		 * the first one, see {@link #firstLineIndent(int)}.
		 * The text indent is applied to text directly within 
		 * the block, but is not inherited to block children.
		 * @param textIndent the indent, in characters
		 * @return returns "this" object
		 */
		public Builder textIndent(int textIndent) {
			this.textIndent = textIndent;
			return this;
		}
		
		/**
		 * Set the first line indent for the block, in characters.
		 * The first line indent controls the indent of the first text 
		 * row in a block.
		 * The first line indent is applied to text directly within
		 * the block, but is not inherited to block children.
		 * @param firstLineIndent the indent, in characters.
		 * @return returns "this" object
		 */
		public Builder firstLineIndent(int firstLineIndent) {
			this.firstLineIndent = firstLineIndent;
			return this;
		}
		
		/**
		 * Sets the alignment for the block.
		 * @param align the alignment
		 * @return returns "this" object
		 */
		public Builder align(FormattingTypes.Alignment align) {
			this.align = align;
			return this;
		}
		
		/**
		 * Sets the row spacing for the block.
		 * @param value the row spacing
		 * @return returns this builder
		 */
		public Builder rowSpacing(float value) {
			this.rowSpacing = value;
			return this;
		}
		
		/**
		 * Sets the identifier for the block.
		 * @param identifier the identifier
		 * @return returns this builder
		 */
		public Builder identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}
		
		/**
		 * Creates a new text block properties object
		 * @return returns a new instance
		 */
		public TextBlockProperties build() {
			return new TextBlockProperties(this);
		}
	}

	private TextBlockProperties(Builder builder) {
		this.textIndent = builder.textIndent;
		this.firstLineIndent = builder.firstLineIndent;
		this.rowSpacing = builder.rowSpacing;
		this.align = builder.align;
		this.identifier = builder.identifier;
	}
	
	/**
	 * Get text indent, in characters
	 * @return returns the text indent
	 */
	public int getTextIndent() {
		return textIndent;
	}
	
	/**
	 * Get first line indent, in characters
	 * @return returns the first line indent
	 */
	public int getFirstLineIndent() {
		return firstLineIndent;
	}
	
	/**
	 * Gets the alignment
	 * @return returns the alignment
	 */
	public FormattingTypes.Alignment getAlignment() {
		return align;
	}
	
	
	/**
	 * Gets the identifier
	 * @return returns the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Gets the row spacing, or null if not set
	 * @return returns the row spacing
	 */
	public Float getRowSpacing() {
		return rowSpacing;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((align == null) ? 0 : align.hashCode());
		result = prime * result + firstLineIndent;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((rowSpacing == null) ? 0 : rowSpacing.hashCode());
		result = prime * result + textIndent;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TextBlockProperties other = (TextBlockProperties) obj;
		if (align != other.align) {
			return false;
		}
		if (firstLineIndent != other.firstLineIndent) {
			return false;
		}
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
		if (rowSpacing == null) {
			if (other.rowSpacing != null) {
				return false;
			}
		} else if (!rowSpacing.equals(other.rowSpacing)) {
			return false;
		}
		if (textIndent != other.textIndent) {
			return false;
		}
		return true;
	}

}
