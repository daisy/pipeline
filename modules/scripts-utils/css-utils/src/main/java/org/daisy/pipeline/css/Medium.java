package org.daisy.pipeline.css;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private final Map<String,String> customFeatures;

	// see https://drafts.csswg.org/mediaqueries/#media-descriptor-table
	private final static Set<String> knownFeatures = new HashSet<>(
		Arrays.asList(
			"any-hover", "any-pointer", "aspect-ratio", "color", "color-gamut", "color-index",
			"device-aspect-ratio", "device-height", "device-width", "grid", "height", "hover",
			"monochrome", "orientation", "overflow-block", "overflow-inline", "pointer", "resolution",
			"scan", "update", "width"));

	/**
	 * @param customFeatures can be used to pass additional properties of the target medium. Must
	 *                       not include any <a
	 *                       href="https://drafts.csswg.org/mediaqueries/#media-descriptor-table">known
	 *                       features</a>
	 */
	private Medium(Type type, Integer width, Integer height, Map<String,String> customFeatures) {
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
		this.customFeatures = customFeatures != null
			? Collections.unmodifiableMap(customFeatures)
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
	public Map<String,String> getCustomFeatures() {
		return customFeatures;
	}

	public boolean matches(String mediaQuery) {
		return asMediaSpec().matches(CSSParserFactory.getInstance().parseMediaQuery(mediaQuery.trim()));
	}

	public String toString() {
		StringBuilder s  = new StringBuilder();
		s.append(type.toString());
		if (width != null)
			s.append(" AND (width: ").append(width).append(")");
		if (height != null)
			s.append(" AND (height: ").append(height).append(")");
		for (String f : customFeatures.keySet())
			s.append(" AND (").append(f).append(": ").append(customFeatures.get(f)).append(")");
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
		List<MediaQuery> q = CSSParserFactory.getInstance().parseMediaQuery(medium);
		if (q.size() != 1)
			throw new IllegalArgumentException("Unexpected medium: " + medium);
		Type type = null;
		try {
			type = Type.valueOf(q.get(0).getType().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unexpected medium type: " + q.get(0).getType());
		}
		if (q.get(0).isNegative())
			throw new IllegalArgumentException("Unexpected medium: contains NOT: " + medium);
		Integer width = null;
		Integer height = null;
		Map<String,String> customFeatures = null;
		for (MediaExpression e : q.get(0)) {
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
				if (customFeatures == null)
					customFeatures = new HashMap<>();
				customFeatures.put(feature, v.toString());
			}
		}
		return new Medium(type, width, height, customFeatures);
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
								if (!customFeatures.isEmpty())
									for (String ff : customFeatures.keySet())
										if (ff.equals(f))
											return customFeatures.get(ff).equals(v.toString());
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
								if (!customFeatures.isEmpty())
									for (String ff : customFeatures.keySet())
										if (ff.equals(f))
											return customFeatures.get(ff).equals(v.toString());
								return false;
							}
						}
					};
				break;
			}
		return mediaSpec;
	}
}
