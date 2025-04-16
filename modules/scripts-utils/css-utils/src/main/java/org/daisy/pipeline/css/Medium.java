package org.daisy.pipeline.css;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.MediaExpression;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.domassign.Analyzer;

public class Medium {

	public enum Type {
		EMBOSSED,
		BRAILLE,
		SPEECH,
		SCREEN,
		PRINT;
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	private final Type type;
	private final Integer width;
	private final Integer height;
	private final Map<String,String> nonStandardFeatures;

	// see https://drafts.csswg.org/mediaqueries/#media-descriptor-table
	private final static Set<String> knownFeatures = new HashSet<>(
		Arrays.asList(
			"any-hover", "any-pointer", "aspect-ratio", "color", "color-gamut", "color-index",
			"device-aspect-ratio", "device-height", "device-width", "grid", "height", "hover",
			"monochrome", "orientation", "overflow-block", "overflow-inline", "pointer", "resolution",
			"scan", "update", "width"));

	/**
	 * @param otherFeatures can be used to pass additional properties of the target medium. Must not
	 *                      include any <a
	 *                      href="https://drafts.csswg.org/mediaqueries/#media-descriptor-table">known
	 *                      features</a>. It is recommended that non-standard features are vendor
	 *                      prefixed.
	 */
	private Medium(Type type, Integer width, Integer height, Map<String,String> otherFeatures) {
		switch (type) {
		case EMBOSSED:
			this.type = type;
			this.width = width;
			this.height = height;
			break;
		case BRAILLE:
		case SPEECH:
		case SCREEN:
		case PRINT:
			if (width != null) {
				throw new IllegalArgumentException("Unexpected 'width' argument for medium '" + type + "'");
			}
			if (height != null) {
				throw new IllegalArgumentException("Unexpected 'height' argument for medium '" + type + "'");
			}
			this.type = type;
			this.width = this.height = null;
			break;
		default:
			throw new IllegalArgumentException("Unexpected medium: " + type);
		}
		this.nonStandardFeatures = otherFeatures != null
			? Collections.unmodifiableMap(otherFeatures)
			: Collections.emptyMap();
	}

	/**
	 * The media type: "embossed", "braille", "speech", "screen" or "print".
	 */
	public Type getType() {
		return type;
	}

	/**
	 * The width of the page, in braille cells, or null if undetermined or not applicable. Only
	 * applicable on type 'embossed'.
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * The height of the page, in braille cells, or null if undetermined or not applicable. Only
	 * applicable on type 'embossed'.
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * Additional properties of the target medium.
	 */
	public Map<String,String> getNonStandardFeatures() {
		return nonStandardFeatures;
	}

	/**
	 * Additional properties of the target medium.
	 *
	 * @deprecated Use the {@link #getNonStandardFeatures()} method instead. "Custom" is terminology
	 * that is used in <a href="https://www.w3.org/TR/mediaqueries-5/#custom-mq">Media Queries Level
	 * 5</a> for something different.
	 */
	@Deprecated
	public Map<String,String> getCustomFeatures() {
		return getNonStandardFeatures();
	}


	public boolean matches(String mediaQuery) {
		if (mediaQuery != null)
			return asMediaSpec().matches(CSSParserFactory.getInstance().parseMediaQuery(mediaQuery.trim()));
		else
			return asMediaSpec().matchesEmpty();
	}

	public String toString() {
		StringBuilder s  = new StringBuilder();
		s.append(type.toString());
		if (width != null)
			s.append(" AND (width: ").append(width).append(")");
		if (height != null)
			s.append(" AND (height: ").append(height).append(")");
		for (String f : nonStandardFeatures.keySet())
			s.append(" AND (").append(f).append(": ").append(nonStandardFeatures.get(f)).append(")");
		return s.toString();
	}

	public boolean equals(Object o) {
		if (!(o instanceof Medium))
			return false;
		Medium that = (Medium)o;
		return this.type == that.type
			&& this.width == that.width
			&& this.height == that.height;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + type.hashCode();
		result = prime * result + width;
		result = prime * result + height;
		return result;
	}

	public static Medium parse(String medium) throws IllegalArgumentException {
		List<MediaQuery> query = CSSParserFactory.getInstance().parseMediaQuery(medium);
		if (query.size() != 1)
			throw new IllegalArgumentException("Unexpected medium: " + medium);
		return fromMediaQuery(query.get(0));
	}

	/**
	 * Parse comma-separated list of media
	 */
	public static List<Medium> parseMultiple(String media) throws IllegalArgumentException {
		return Lists.transform(
			CSSParserFactory.getInstance().parseMediaQuery(media),
			Medium::fromMediaQuery);
	}

	private static Medium fromMediaQuery(MediaQuery query) throws IllegalArgumentException {
		Type type = null;
		try {
			type = Type.valueOf(query.getType().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unexpected medium type: " + query.getType());
		}
		if (query.isNegative())
			throw new IllegalArgumentException("Unexpected medium: contains NOT: " + query);
		Integer width = null;
		Integer height = null;
		Map<String,String> nonStandardFeatures = null;
		for (MediaExpression e : query) {
			String feature = e.getFeature();
			if ("width".equals(feature) || "height".equals(feature)) {
				if (e.size() != 1)
					throw new IllegalArgumentException("Unexpected value for medium feature: " + e);
				Term<?> v = e.get(0);
				if (!(v instanceof TermInteger))
					throw new IllegalArgumentException("Unexpected value for medium feature: " + e);
				Integer i = ((TermInteger)v).getIntValue();
				if ("width".equals(feature))
					width = i;
				else
					height = i;
			} else if (knownFeatures.contains(feature)) {
				throw new IllegalArgumentException("Unsupported medium feature: " + feature);
			} else {
				if (e.size() != 1)
					throw new IllegalArgumentException("Unexpected value for medium feature: " + e);
				Term<?> v = e.get(0);
				if (!(v instanceof TermIdent || v instanceof TermInteger))
					throw new IllegalArgumentException("Unexpected value for medium feature: " + e);
				if (nonStandardFeatures == null)
					nonStandardFeatures = new HashMap<>();
				nonStandardFeatures.put(feature, v.toString());
			}
		}
		return new Medium(type, width, height, nonStandardFeatures);
	}

	private MediaSpec mediaSpec;

	/**
	 * The medium as a jStyleParser {@link MediaSpec} object, used by {@link
	 * JStyleParserCssCascader} for passing to {@link CSSFactory#getUsedStyles} and {@link
	 * Analyzer#evaluateDOM}.
	 */
	public MediaSpec asMediaSpec() {
		if (mediaSpec == null)
			switch (type) {
			case EMBOSSED:
				mediaSpec = new MediaSpec(type.toString()) {
						// overriding getExpressionLengthPx() because MediaSpec expects width and height
						// to be in pixels but width and height are specified in braille cells
						@Override
						protected Float getExpressionLengthPx(MediaExpression e) {
							return (float)getExpressionInteger(e);
						}
						@Override
						public boolean matches(MediaExpression e) {
							String f = e.getFeature();
							if (knownFeatures.contains(f) || knownFeatures.contains(f.replaceAll("^(min|max)-", "")))
								return super.matches(e);
							else {
								if (e.size() != 1)
									return false;
								Term<?> v = e.get(0);
								if (!(v instanceof TermIdent || v instanceof TermInteger))
									return false;
								if (!nonStandardFeatures.isEmpty())
									for (String ff : nonStandardFeatures.keySet())
										if (ff.equals(f))
											return nonStandardFeatures.get(ff).equals(v.toString());
								return false;
							}
						}
					};
				if (width != null)
					mediaSpec.setWidth((float)width);
				if (height != null)
					mediaSpec.setWidth((float)height);
				break;
			default:
				mediaSpec = new MediaSpec(type.toString()) {
						@Override
						public boolean matches(MediaExpression e) {
							String f = e.getFeature();
							if (knownFeatures.contains(f) || knownFeatures.contains(f.replaceAll("^(min|max)-", "")))
								return super.matches(e);
							else {
								if (e.size() != 1)
									return false;
								Term<?> v = e.get(0);
								if (!(v instanceof TermIdent || v instanceof TermInteger))
									return false;
								if (!nonStandardFeatures.isEmpty())
									for (String ff : nonStandardFeatures.keySet())
										if (ff.equals(f))
											return nonStandardFeatures.get(ff).equals(v.toString());
								return false;
							}
						}
					};
				break;
			}
		return mediaSpec;
	}
}
