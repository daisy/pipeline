package org.daisy.pipeline.css;

import java.util.List;
import java.util.Map;

import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;

/**
 * Media query parser for media queries that are specifically intended for selecting a medium
 * (i.e. for passing to {@link MediumProvider}).
 */
public class MediaQueryParser {
	private MediaQueryParser() {}

	// these are all immutable
	public static final MediaQuery EMBOSSED = Medium.EMBOSSED.toMediaQuery();
	public static final MediaQuery BRAILLE = Medium.BRAILLE.toMediaQuery();
	public static final MediaQuery SPEECH = Medium.SPEECH.toMediaQuery();
	public static final MediaQuery SCREEN = Medium.SCREEN.toMediaQuery();
	public static final MediaQuery PRINT = Medium.PRINT.toMediaQuery();

	/**
	 * Parse a media query
	 *
	 * @return the parsed media query, validated and normalized
	 * @throws IllegalArgumentException if the media query can not be parsed due to a wrong syntax,
	 *                                  contains NOT operators, "min-" or "max-" prefixes, or
	 *                                  contains unsupported, invalid or conflicting media features
	 *                                  or types
	 */
	public static MediaQuery parse(String query) throws IllegalArgumentException {
		List<MediaQuery> q = parseList(query);
		if (q.size() != 1)
			throw new IllegalArgumentException("Media query is a comma-separated list: " + query.trim());
		return validate(q.get(0));
	}

	public static List<MediaQuery> parseList(String query) throws IllegalArgumentException {
		List<MediaQuery> q = CSSParserFactory.getInstance().parseMediaQuery(query.trim());
		if (q == null)
			throw new IllegalArgumentException("Media query could not be parsed: " + query.trim());
		return q;
	}

	/**
	 * Parse a list of media features
	 *
	 * @throws IllegalArgumentException if the input contains unsupported, invalid, or conflicting
	 *                                  media features.
	 */
	public static MediaQuery parse(Map<String,Object> query) throws IllegalArgumentException {
		return new Medium.MediumBuilder().parse(query).build().toMediaQuery();
	}

	// validate and normalize
	private static MediaQuery validate(MediaQuery q) {
		if ("all".equals(q.getType()) && q.isNegative() && q.size() == 0)
			throw new IllegalArgumentException("Media query matches nothing: " + q);
		return Medium.fromMediaQuery(q).toMediaQuery();
	}
}
