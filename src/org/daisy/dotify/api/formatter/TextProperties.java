package org.daisy.dotify.api.formatter;


/**
 * <p>Defines properties specific for a span of text.</p>
 * 
 * @author Joel Håkansson
 */
public class TextProperties {
	private final String locale;
	private final boolean hyphenate;
	
	/**
	 * Provides a builder for creating text properties instances.
	 * 
	 * @author Joel Håkansson
	 */
	public static class Builder {
		private final String locale;
		private boolean hyphenate = true;
		/**
		 * Creates a new builder with the specified locale
		 * @param locale the locale for the builder
		 */
		public Builder(String locale) {
			this.locale = locale;
		}
		/**
		 * Sets the hyphenate value for thie builder
		 * @param value the value
		 * @return returns this object
		 */
		public Builder hyphenate(boolean value) {
			this.hyphenate = value;
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
		return true;
	}

}
