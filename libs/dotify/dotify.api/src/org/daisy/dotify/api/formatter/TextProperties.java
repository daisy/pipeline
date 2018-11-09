package org.daisy.dotify.api.formatter;


/**
 * <p>Defines properties for a chunk of text.</p>
 *  
 *  <p>A note to developers of this API: These properties must 
 *  not have any meaning or effect beyond rendering of the 
 *  text to which the properties belong. In an XML-context, 
 *  these properties are typically inherited from higher levels
 *  down to all text nodes. Therefore, properties that cannot be
 *  inherited or do not pertain to the text itself, should be
 *  added elsewhere.</p>
 * 
 * @author Joel Håkansson
 */
public class TextProperties {
	private final String locale;
	private final String translationMode;
	private final boolean hyphenate;
	
	/**
	 * Provides a builder for creating text properties instances.
	 * 
	 * @author Joel Håkansson
	 */
	public static class Builder {
		private final String locale;
		private String translationMode = null;
		private boolean hyphenate = true;

		/**
		 * Creates a new builder with the specified locale
		 * @param locale the locale for the builder
		 */
		public Builder(String locale) {
			this.locale = locale;
		}

		/**
		 * Sets the hyphenate value for this builder
		 * @param value the value
		 * @return returns this object
		 */
		public Builder hyphenate(boolean value) {
			this.hyphenate = value;
			return this;
		}
		
		/**
		 * Sets the translation mode for the builder
		 * @param mode the translation mode
		 * @return returns this object
		 */
		public Builder translationMode(String mode) {
			this.translationMode = mode;
			return this;
		}
		/**
		 * Builds a new TextProperties object using the current
		 * status of this builder.
		 * @return returns a TextProperties instance
		 */
		public TextProperties build() {
			return new TextProperties(this);
		}
	}
	
	private TextProperties(Builder builder) {
		this.locale = builder.locale;
		this.translationMode = builder.translationMode;
		this.hyphenate = builder.hyphenate;
	}

	/**
	 * Gets the locale of this text properties
	 * @return returns the locale
	 */
	public String getLocale() {
		return locale;
	}
	
	/**
	 * Gets the translation mode
	 * @return the translation mode
	 */
	public String getTranslationMode() {
		return translationMode;
	}

	/**
	 * Returns true if the hyphenating property is true, false otherwise
	 * @return returns true if the hyphenating property is true
	 */
	public boolean isHyphenating() {
		return hyphenate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hyphenate ? 1231 : 1237);
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((translationMode == null) ? 0 : translationMode.hashCode());
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
		TextProperties other = (TextProperties) obj;
		if (hyphenate != other.hyphenate) {
			return false;
		}
		if (locale == null) {
			if (other.locale != null) {
				return false;
			}
		} else if (!locale.equals(other.locale)) {
			return false;
		}
		if (translationMode == null) {
			if (other.translationMode != null) {
				return false;
			}
		} else if (!translationMode.equals(other.translationMode)) {
			return false;
		}
		return true;
	}

}
