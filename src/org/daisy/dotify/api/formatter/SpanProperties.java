package org.daisy.dotify.api.formatter;

import java.util.Optional;

/**
 * Provides span properties.
 * @author Joel Håkansson
 *
 */
public final class SpanProperties {
	private final Optional<String> identifier;

	/**
	 * Provides a builder for span properties.
	 */
	public static class Builder {
		private Optional<String> identifier = Optional.empty();

		/**
		 * Creates a new empty builder. 
		 */
		public Builder() {
		}

		/**
		 * Sets an identifier.
		 * @param value the identifier
		 * @return returns this object
		 * @throws NullPointerException if value is null
		 */
		public Builder identifier(String value) {
			this.identifier = Optional.of(value);
			return this;
		}

		/**
		 * Builds span properties based on the current state of the builder.
		 * @return returns new span properties
		 */
		public SpanProperties build() {
			return new SpanProperties(this);
		}
	}

	private SpanProperties(Builder builder) {
		this.identifier = builder.identifier;
	}

	/**
	 * Gets the identifier
	 * @return the identifier
	 */
	public Optional<String> getIdentifier() {
		return identifier;
	}
}
