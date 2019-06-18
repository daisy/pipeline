package org.daisy.dotify.api.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.daisy.dotify.api.factory.FactoryProperties;

/**
 * Provides information about a translator mode. This information could, for example, be used to
 * find a translator that matches a given specification. The specification could be based 
 * on some translator mode properties or on its identifier.
 * @author Joel Håkansson
 *
 */
public final class TranslatorMode implements FactoryProperties {
	/**
	 * Provides a definition of the number of dots in a braille cell.
	 */
	public enum DotsPerCell {
		/**
		 * Defines 6-dot braille.
		 */
		SIX("6-dot"),
		/**
		 * Defines 8-dot braille. 
		 */
		EIGHT("8-dot");
		
		private final String str;
		private DotsPerCell(String str) {
			this.str = str;
		}
		
		@Override
		public String toString() {
			return str;
		}
	}
	private static final String GRADE_PREFIX = "grade:";
	private final String identifier;
	private final String displayName;
	private final String description;
	private final Optional<TranslatorType> type;
	private final Optional<Double> grade;
	private final Optional<DotsPerCell> dotsPerCell;
	
	/**
	 * Provides a translator mode builder.
	 */
	public static class Builder {
		private String identifier = null;
		private String displayName = "";
		private String description = "";
		private Optional<TranslatorType> type = Optional.empty();
		private Optional<Double> grade = Optional.empty();
		private Optional<DotsPerCell> dotsPerCell = Optional.empty();

		private Builder() {
		}
		
		/**
		 * Creates a new translator mode builder with the specified identifier.
		 * @param identifier the identifier
		 * @return a new builder
		 */
		public static Builder withIdentifier(String identifier) {
			return new Builder().identifier(identifier);
		}
		
		/**
		 * Creates a new translator mode builder with the specified type.
		 * @param type the translator type
		 * @return a new builder
		 */
		public static Builder withType(TranslatorType type) {
			return new Builder().type(type);
		}
		
		/**
		 * Creates a new translator mode builder with the specified contraction grade.
		 * 
		 * <p>Note that setting the contraction grade implies that the translator type is 
		 * <code>contracted</code> even though in some locales a particular contraction grade
		 * could have another meaning, for example that the braille is not contracted.</p>
		 *
		 * @param grade the contraction grade
		 * @return a new builder
		 */
		public static Builder withGrade(double grade) {
			return new Builder().grade(grade);
		}
		
		/**
		 * Parses the string as translator mode segments and sets the builder accordingly.
		 * @param str the input string
		 * @return a new builder
		 * @throws IllegalArgumentException if parsing fails
		 */
		public static Builder parse(String str) {
			Builder builder = new Builder();
			for (String s : str.split(Pattern.quote("/"))) {
				builder.parseSegment(s);
			}
			return builder;
		}
		
		/**
		 * Sets the identifier. If an identifier is not set, it will be generated from the
		 * supplied properties.
		 * @param value the identifier
		 * @return this instance
		 */
		public Builder identifier(String value) {
			this.identifier = value;
			return this;
		}

		/**
		 * Sets the contraction grade.
		 * 
		 * @param grade the contraction grade
		 * @return this instance
		 */
		public Builder grade(double grade) {
			this.grade = Optional.of(grade);
			return this;
		}

		/**
		 * Sets the translator type.
		 * 
		 * @param type the type
		 * @return this instance
		 */
		public Builder type(TranslatorType type) {
			this.type = Optional.of(type);
			return this;
		}
		
		private Builder parseSegment(String str) {
			if (str.contains("/")) {
				throw new IllegalArgumentException("Invalid segment:" + str);
			}
			String strLC = str.toLowerCase(Locale.ROOT);
			if (strLC.startsWith(GRADE_PREFIX)) {
				grade(Double.parseDouble(str.substring(GRADE_PREFIX.length())));
				return this;
			} else {
				Optional<DotsPerCell> dpc = Arrays.asList(DotsPerCell.values()).stream()
						.filter(v->strLC.equals(v.toString()))
						.findFirst();
				if (dpc.isPresent()) {
					dotsPerCell(dpc);
				} else {
					type(TranslatorType.parse(str));
				}
				return this; 
			}
		}
		
		/**
		 * Set the display name for this mode. The display name should be localized.
		 * @param value the display name
		 * @return this instance
		 */
		public Builder displayName(String value) {
			this.displayName = value;
			return this;
		}
		
		/**
		 * Set the description for this mode. The description should be localized.
		 * @param value the description
		 * @return this instance
		 */
		public Builder description(String value) {
			this.description = value;
			return this;
		}
		
		/**
		 * Set the number of dots per cell.
		 * @param value dots per cell
		 * @return this instance
		 */
		public Builder dotsPerCell(Optional<DotsPerCell> value) {
			this.dotsPerCell = value;
			return this;
		}
		
		/**
		 * Set the number of dots per cell.
		 * @param value dots per cell
		 * @return this instance
		 */
		public Builder dotsPerCell(DotsPerCell value) {
			this.dotsPerCell = Optional.of(value);
			return this;
		}
		
		/**
		 * Builds a new {@link TranslatorMode} instance based on the current configuration
		 * of this builder.
		 * @return a new {@link TranslatorMode} instance
		 */
		public TranslatorMode build() {
			return new TranslatorMode(this);
		}
	}
	
	private TranslatorMode(Builder builder) {
		this.type = builder.type;
		this.grade = builder.grade;
		this.dotsPerCell = builder.dotsPerCell;
		this.displayName = builder.displayName;
		this.description = builder.description;
		if (builder.identifier==null) {
			// Generate an identifier, note that this should mirror the parse method
			List<String> segments = new ArrayList<>();
			if (builder.dotsPerCell.isPresent()) {
				segments.add(""+builder.dotsPerCell.get());
			}
			if (builder.type.isPresent()) {
				segments.add(builder.type.get().toString());
			}
			if (builder.grade.isPresent()) {
				double grade = builder.grade.get();
				segments.add(GRADE_PREFIX + (Math.round(grade)==grade?(int)grade:Double.toString(grade)));
			}
			this.identifier = segments.stream().collect(Collectors.joining("/"));
		} else {
			this.identifier = builder.identifier;
		}
	}
	
	/**
	 * Parses the string as a translator mode.
	 * @param str the input string
	 * @return a translator mode
	 * @throws IllegalArgumentException if parsing fails
	 */
	public static TranslatorMode parse(String str) {
		return Builder.parse(str).build();
	}
	
	/**
	 * Creates a new mode with the specified type.
	 * @param type the type
	 * @return a new instance
	 */
	public static TranslatorMode withType(TranslatorType type) {
		return TranslatorMode.Builder.withType(type).build();
	}
	
	/**
	 * Creates a new mode with the specified translation grade
	 * @param grade the translation grade
	 * @return a new instance
	 */
	public static TranslatorMode withGrade(double grade) {
		return TranslatorMode.Builder.withGrade(grade).build();
	}
	
	/**
	 * Creates a new mode with the specified identifier.
	 * 
	 * @param id the identifier
	 * @return a new instance
	 */
	static TranslatorMode withIdentifier(String id) {
		return TranslatorMode.Builder.withIdentifier(id).build();
	}
	
	/**
	 * Gets the contraction grade, if set.
	 * @return the contraction grade.
	 */
	public Optional<Double> getContractionGrade() {
		return grade;
	}
	
	/**
	 * Gets the mode type, if set.
	 * @return the type of mode
	 */
	public Optional<TranslatorType> getType() {
		return type;
	}
	
	/**
	 * Gets the number of dots per cell, if set.
	 * @return the dots per cell
	 */
	public Optional<DotsPerCell> getDotsPerCell() {
		return dotsPerCell;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return identifier;
	}

}
