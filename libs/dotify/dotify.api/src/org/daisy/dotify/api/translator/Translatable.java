package org.daisy.dotify.api.translator;

/**
 * Provides a specification for a text to translate
 * @author Joel Håkansson
 *
 */
public class Translatable {
	private final String text;
	private final String locale;
	private final TextAttribute attributes;
	private final Boolean hyphenate;
	
	/**
	 * Provides a builder for translatable objects
	 * 
	 * @author Joel Håkansson
	 */
	public static class Builder {
		private final String text;
		private String locale = null;
		private TextAttribute attributes = null;
		private boolean hyphenate = false;
		private Builder(String text) {
			this.text = text;
		}
		/**
		 * Sets the locale for this builder
		 * @param value the locale
		 * @return returns this object
		 */
		public Builder locale(String value) {
			this.locale = value;
			return this;
		}
		
		/**
		 * Sets the text attributes for this builder
		 * @param value the text attributes
		 * @return returns this object
		 */
		public Builder attributes(TextAttribute value) {
			this.attributes = value;
			return this;
		}
		
		/**
		 * Sets the hyphenate property for this builder
		 * @param value the hyphenate policy
		 * @return returns this object
		 */
		public Builder hyphenate(boolean value) {
			this.hyphenate = value;
			return this;
		}
		
		/**
		 * Builds a new Translatable object using the current
		 * status of this builder.
		 * @return returns a Translatable instance
		 */
		public Translatable build() {
			return new Translatable(this);
		}

	}

	private Translatable(Builder builder) {
		this.text = builder.text;
		this.locale = builder.locale;
		this.attributes = builder.attributes;
		this.hyphenate = builder.hyphenate;
	}

	/**
	 * Creates a new Translatable.Builder with the specified text.
	 * @param text the text to translate
	 * @return returns a new Translatable.Builder
	 */
	public static Builder text(String text) {
		return new Builder(text);
	}

	/**
	 * Gets the text
	 * @return returns the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets the locale for the text
	 * @return returns the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * Gets the text attributes for the text
	 * @return returns the text attributes for the text
	 */
	public TextAttribute getAttributes() {
		return attributes;
	}

	/**
	 * Returns true if the text should be hyphenated.
	 * @return returns true if the text should be hyphenated, false otherwise
	 */
	public Boolean isHyphenating() {
		return hyphenate;
	}
}
