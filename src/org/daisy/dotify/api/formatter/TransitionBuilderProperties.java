package org.daisy.dotify.api.formatter;

import java.util.Locale;

/**
 * Provides properties for a transition builder.
 * @author Joel Håkansson
 */
public final class TransitionBuilderProperties {
	private final ApplicationRange range;

	/**
	 * Defines the application range starting from the end of the volume. Within this
	 * range, pages may be treated differently from pages elsewhere.
	 */
	public enum ApplicationRange {
		/**
		 * All pages are treated the same
		 */
		NONE,
		/**
		 * The last page may be treated differently 
		 */
		PAGE,
		/**
		 * The last sheet may be treated differently
		 */
		SHEET;
		
		private ApplicationRange() {
			if (!this.name().toUpperCase(Locale.ROOT).equals(this.name())) {
				// This can only happen when someone changes the constants above,
				// therefore it is an error and should not be caught.
				throw new AssertionError("Error in code. Only upper case enum members are allowed.");
			}
		}

		/**
		 * Parses a string as a {@link ApplicationRange}. There is a small difference of this
		 * method compared to calling {@link #valueOf(String)} is that it is
		 * case insensitive (since all enum members contain upper case letters only).
		 * 
		 * @param value the string value
		 * @return returns the range
		 * @throws IllegalArgumentException if this enum type has no
		 * constant with the specified name
		 */
		public static ApplicationRange parse(String value) {
			return ApplicationRange.valueOf(value.toUpperCase(Locale.ROOT));
		}
	}
	
	/**
	 * Provides a builder for transition builder properties.
	 */
	public static class Builder {
		private ApplicationRange range = ApplicationRange.NONE;
		
		/**
		 * Creates a new builder based on the supplied configuration.
		 * @param properties the current configuration.
		 * @return returns a new builder
		 */
		public static Builder with(TransitionBuilderProperties properties) {
			Builder ret = new Builder();
			ret.range = properties.range;
			return ret;
		}

		/**
		 * Sets the application range for this builder.
		 * @param value the range
		 * @return returns this builder
		 */
		public Builder applicationRange(ApplicationRange value) {
			this.range = value;
			return this;
		}

		/**
		 * Builds {@link TransitionBuilderProperties} based on the current state of
		 * this builder.
		 * @return returns a new {@link TransitionBuilderProperties} instance
		 */
		public TransitionBuilderProperties build() {
			return new TransitionBuilderProperties(this);
		}
	}

	private TransitionBuilderProperties(Builder builder) {
		this.range = builder.range;
	}

	/**
	 * Gets the application range starting from the end of the volume. Within the range,
	 * pages can be treated differently from other pages.
	 * 
	 * @return returns the application range
	 */
	public ApplicationRange getApplicationRange() {
		return range;
	}

}
