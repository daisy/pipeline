package org.daisy.dotify.translator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.daisy.dotify.api.translator.TextAttribute;


/**
 * Provides a regular expressions marker dictionary that
 * provides returns markers based on regular expressions.
 * Both a matching and a non-matching marker can be defined for
 * for the same regular expression.
 *   
 * @author Joel Håkansson
 *
 */
public class RegexMarkerDictionary implements MarkerDictionary {
	private final Map<Pattern, MarkerPair> patterns;
	private final TextAttributeFilter filter;

	private static class MarkerPair {
		private final Marker matching, nonMatching;

		public MarkerPair(Marker matching, Marker nonMatching) {
			this.matching = matching;
			this.nonMatching = nonMatching;
		}

		public Marker getMatching() {
			return matching;
		}

		public Marker getNonMatching() {
			return nonMatching;
		}

	}

	/**
	 * Provides a builder for creating RegexMarkerDictionary objects.
	 * 
	 * @author Joel Håkansson
	 * 
	 */
	public static class Builder {
		private final Map<Pattern, MarkerPair> patterns;
		private TextAttributeFilter filter = new TextAttributeFilter() {
			public boolean appliesTo(TextAttribute atts) {
				return true;
			}
		};

		/**
		 * Creates a new builder. Note that, for the resulting object
		 * to function, at least one pattern must be added.
		 */
		public Builder() {
			patterns = new LinkedHashMap<Pattern, MarkerPair>();
		}

		/**
		 * Adds a pattern to the builder. The regular expression provided is
		 * tested using find(), meaning that if the string must match exactly,
		 * \A and \z must be used at the beginning and end of the expression
		 * respectively.
		 * 
		 * @param regex
		 *            the regular expression
		 * @param matching
		 *            the matching marker, that is to say the marker to return
		 *            when the regex matches.
		 * @return returns this object
		 */
		public Builder addPattern(String regex, Marker matching) {
			patterns.put(Pattern.compile(regex), new MarkerPair(matching, null));
			return this;
		}

		/**
		 * Adds a pattern to the builder. The regular expression provided is
		 * tested using find(), meaning that if the string must match exactly,
		 * \A and \z must be used at the beginning and end of the expression
		 * respectively.
		 * 
		 * @param regex
		 *            the regular expression
		 * @param matching
		 *            the matching marker, that is to say the marker to return
		 *            when the regex matches.
		 * @param nonMatching
		 *            the non-matching marker, that is to say the marker to
		 *            return if the regex doesn't match
		 * @return returns this object
		 */
		public Builder addPattern(String regex, Marker matching, Marker nonMatching) {
			patterns.put(Pattern.compile(regex), new MarkerPair(matching, nonMatching));
			return this;
		}

		public Builder filter(TextAttributeFilter filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Builds this builder.
		 * 
		 * @return returns a new RegexMarkerDictionary
		 */
		public RegexMarkerDictionary build() {
			return new RegexMarkerDictionary(this);
		}
	}

	private RegexMarkerDictionary(Builder builder) {
		this.patterns = builder.patterns;
		this.filter = builder.filter;
	}

	public Marker getMarkersFor(String str, TextAttribute attribute) throws MarkerNotFoundException, MarkerNotCompatibleException {
		if (!filter.appliesTo(attribute)) {
			throw new MarkerNotCompatibleException("Cannot apply marker to " + attribute);
		}
		for (Pattern p : patterns.keySet()) {
			if (p.matcher(str).find()) {
				return patterns.get(p).getMatching();
			} else {
				Marker r = patterns.get(p).getNonMatching();
				if (r != null) {
					return r;
				}
			}
		}
		throw new MarkerNotFoundException("No definition applies to: " + str);
	}

}