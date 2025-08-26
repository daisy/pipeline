package org.daisy.pipeline.css;

import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.MediaExpression;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermNumeric;
import cz.vutbr.web.domassign.Analyzer;

import org.daisy.pipeline.css.impl.UnmodifiableTerm;

import org.unbescape.css.CssEscape;

public class Medium implements Dimension.RelativeDimensionBase {

	public static final Medium EMBOSSED = new Medium(Type.EMBOSSED);
	public static final Medium BRAILLE = new Medium(Type.BRAILLE);
	public static final Medium SPEECH = new Medium(Type.SPEECH);
	public static final Medium SCREEN = new Medium(Type.SCREEN);
	public static final Medium PRINT = new Medium(Type.PRINT);

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
	private final Dimension width;
	private final Dimension height;
	private final Dimension deviceWidth;
	private final Dimension deviceHeight;
	private final Map<String,Object> nonStandardFeatures;
	private final MediumBuilder parser;

	// see https://drafts.csswg.org/mediaqueries/#media-descriptor-table
	private final static Set<String> knownFeatures = new HashSet<>(
		Arrays.asList(
			"any-hover", "any-pointer", "aspect-ratio", "color", "color-gamut", "color-index",
			"device-aspect-ratio", "device-height", "device-width", "grid", "height", "hover",
			"monochrome", "orientation", "overflow-block", "overflow-inline", "pointer", "resolution",
			"scan", "update", "width"));

	private final static Set<String> knownDaisyFeatures = new HashSet<>(
		Arrays.asList(
			// for embossed
			"format", "file-format",
			"embosser",
			"page-format",
			"cell-width",
			"cell-height",
			"duplex",
			"saddle-stitch", "sheets-multiple-of-two",
			"z-folding",
			"blank-last-page",
			"pages-in-quire",
			"number-of-copies",
			"table",
			"charset",
			"line-breaks",
			"page-breaks",
			"pad",
			"file-extension",
			"locale",          // explicitly specified locale; intended for medium selection
			"document-locale"  // locale of input document; automatically added; intended for medium selection
		));

	/**
	 * @param otherFeatures can be used to pass additional features of the target medium. Must not
	 *                      include any <a
	 *                      href="https://drafts.csswg.org/mediaqueries/#media-descriptor-table">known
	 *                      features</a>. It is recommended that non-standard features are vendor
	 *                      prefixed.
	 */
	protected Medium(Type type, Dimension width, Dimension height, Dimension deviceWidth, Dimension deviceHeight,
	                 Map<String,Object> otherFeatures, MediumBuilder parser) {
		this.type = type;
		this.width = width;
		this.height = height;
		this.deviceWidth = deviceWidth;
		this.deviceHeight = deviceHeight;
		this.nonStandardFeatures = otherFeatures != null
			? Collections.unmodifiableMap(otherFeatures)
			: Collections.emptyMap();
		this.parser = parser;
	}

	private Medium(Type type) {
		this(type, null, null, null, null, null, null);
	}

	/**
	 * The media type: "embossed", "braille", "speech", "screen" or "print", or null if undetermined.
	 */
	public final Type getType() {
		return type;
	}

	/**
	 * The width of the display area, or null if undetermined or not applicable.
	 */
	public final Dimension getWidth() {
		return width;
	}

	/**
	 * The height of the display area, null if undetermined or not applicable.
	 */
	public final Dimension getHeight() {
		return height;
	}

	/**
	 * The width of the rendering surface, or null if undetermined or not applicable.
	 */
	public final Dimension getDeviceWidth() {
		return width;
	}

	/**
	 * The height of the rendering surface, null if undetermined or not applicable.
	 */
	public final Dimension getDeviceHeight() {
		return height;
	}

	/**
	 * Get the value of the specified non-standard feature of the target medium, or null if undetermined.
	 */
	protected Object getNonStandardFeature(String feature) {
		return nonStandardFeatures.get(feature);
	}

	public final boolean matches(String query) {
		try {
			return query != null
				? asMediaSpec().matches(MediaQueryParser.parseList(query))
				: asMediaSpec().matchesEmpty();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Evaluate a media expression about a non-standard feature, in <a
	 * href="https://drafts.csswg.org/mediaqueries-5/#mq-boolean-context">boolean context</a>.
	 */
	protected boolean matchesFeature(String feature) {
		if ("-daisy-document-locale".equals(feature))
			return false; // intended for medium selection, not valid in regular media queries
		Object v = getNonStandardFeature(feature);
		if (v == null)
			return false;
		else if (v instanceof String)
			return !"none".equals(v);
		else if (v instanceof Integer)
			return (Integer)v != 0;
		else if (v instanceof Boolean)
			return (Boolean)v == true;
		else if (v instanceof Dimension)
			return ((Dimension)v).getValue().equals(BigDecimal.ZERO);
		else
			return true;
	}

	/**
	 * Evaluate a media expression about a non-standard feature.
	 */
	protected boolean matchesFeature(String feature, Term<?> value) {
		if ("-daisy-document-locale".equals(feature))
			return false; // intended for medium selection, not valid in regular media queries
		Object v = getNonStandardFeature(feature);
		if (v != null)
			try {
				if (value instanceof TermIdent)
					if (v instanceof Locale)
						return v.equals(parser.parseLocale(value));
					else
						return v.equals(parser.parseIdent(value));
				else if (value instanceof TermInteger)
					if (v instanceof Boolean)
						return v.equals(parser.parseBoolean(value));
					else
						return v.equals(parser.parseInteger(value));
				else if (value instanceof TermLength)
					return v.equals(parser.parseLength(value, feature));
			} catch (IllegalArgumentException e) {
			}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder s  = new StringBuilder();
		if (type != null)
			s.append(type.toString());
		if (width != null) {
			if (s.length() > 0) s.append(" AND ");
			s.append("(width: ").append(width).append(")");
		}
		if (height != null) {
			if (s.length() > 0) s.append(" AND ");
			s.append("(height: ").append(height).append(")");
		}
		if (deviceWidth != null) {
			if (s.length() > 0) s.append(" AND ");
			s.append("(device-width: ").append(deviceWidth).append(")");
		}
		if (deviceHeight != null) {
			if (s.length() > 0) s.append(" AND ");
			s.append("(device-height: ").append(deviceHeight).append(")");
		}
		for (String f : nonStandardFeatures.keySet()) {
			if ("-daisy-document-locale".equals(f))
				continue;
			if (s.length() > 0) s.append(" AND ");
			s.append("(").append(f);
			Object v = nonStandardFeatures.get(f);
			if (v instanceof Boolean)
				s.append((Boolean)v ? "" : ": 0");
			else if (v instanceof Locale)
				s.append(": ").append(((Locale)v).toLanguageTag());
			else if (v.toString().matches("^[+-]?([0-9]*\\.)?[0-9]+([eE][+-]?[0-9]+)?([a-zA-Z]+|%)?$"))
				// integer, number, dimension or percentage
				s.append(": ").append(v.toString());
			else
				// identifier: escape if needed
				s.append(": ").append(CssEscape.escapeCssIdentifierMinimal(v.toString()));
			s.append(")");
		}
		return s.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Medium))
			return false;
		Medium that = (Medium)o;
		if (this.type != that.type)
			return false;
		if (this.width == null) {
			if (that.width != null)
				return false;
		} else if (!this.width.equals(that.width))
			return false;
		if (this.height == null) {
			if (that.height != null)
				return false;
		} else if (!this.height.equals(that.height))
			return false;
		if (this.deviceWidth == null) {
			if (that.deviceWidth != null)
				return false;
		} else if (!this.deviceWidth.equals(that.deviceWidth))
			return false;
		if (this.deviceHeight == null) {
			if (that.deviceHeight != null)
				return false;
		} else if (!this.deviceHeight.equals(that.deviceHeight))
			return false;
		if (this.nonStandardFeatures == null || this.nonStandardFeatures.isEmpty())
			return that.nonStandardFeatures == null || that.nonStandardFeatures.isEmpty();
		return this.nonStandardFeatures.equals(that.nonStandardFeatures);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (type != null)
			result = prime * result + type.hashCode();
		if (width != null)
			result = prime * result + width.hashCode();
		if (height != null)
			result = prime * result + height.hashCode();
		if (deviceWidth != null)
			result = prime * result + deviceWidth.hashCode();
		if (deviceHeight != null)
			result = prime * result + deviceHeight.hashCode();
		if (nonStandardFeatures != null)
			result = prime * result + nonStandardFeatures.hashCode();
		return result;
	}

	private MediaSpec mediaSpec;

	/**
	 * The medium as a jStyleParser {@link MediaSpec} object, used by {@link
	 * JStyleParserCssCascader} for passing to {@link CSSFactory#getUsedStyles} and {@link
	 * Analyzer#evaluateDOM}.
	 */
	public MediaSpec asMediaSpec() {
		if (mediaSpec == null) {
			Comparator<Dimension> lengthComparator = Dimension.comparator(Medium.this);
			mediaSpec = new MediaSpec(type.toString()) {
				@Override
				protected boolean matchesIgnoreNegation(MediaExpression e) {
					String f = e.getFeature();
					boolean isMin = false;
					boolean isMax = false;
					if (f.startsWith("min-")) { isMin = true; f = f.substring(4); }
					else if (f.startsWith("max-")) { isMax = true; f = f.substring(4); }
					if (knownFeatures.contains(f)) {
						MediaSpec.Feature ff = getFeatureByName(f);
						switch (ff) {
						case WIDTH:
						case HEIGHT:
						case DEVICE_WIDTH:
						case DEVICE_HEIGHT:
							if (e.size() != 1)
								return false;
							Dimension v = null; {
								switch (ff) {
								case WIDTH: v = Medium.this.width; break;
								case HEIGHT: v = Medium.this.height; break;
								case DEVICE_WIDTH: v = Medium.this.deviceWidth; break;
								case DEVICE_HEIGHT: v = Medium.this.deviceHeight; break;
								}
							}
							if (v == null)
								return false;
							Dimension length; {
								try {
									length = parser.parseLength(e.get(0), f);
								} catch (IllegalArgumentException ex) {
									return false;
								}
							}
							int comparison = lengthComparator.compare(v, length);
							return isMin
								? comparison >= 0
								: isMax
									? comparison <= 0
									: comparison == 0;
						default:
							return super.matchesIgnoreNegation(e);
						}
					} else {
						if (isMin || isMax)
							return false;
						f = parser.normalizeFeature(f);
						if (e.size() > 1)
							return false;
						else if (e.size() == 0)
							return Medium.this.matchesFeature(f);
						else
							return Medium.this.matchesFeature(f, e.get(0));
					}
				}
			};
		}
		if (Medium.this.type == Type.EMBOSSED)
			mediaSpec.setGrid(1);
		// These are commented out because the unit conversion may fail and it is not so
		// important that these are set anyway: the corresponding getter methods in MediaSpec
		// are not used anywhere within the jStyleParser code anyway.
		/*if (width != null)
			mediaSpec.setWidth(width.toUnit(Dimension.Unit.PX, this).getValue().floatValue());
		if (height != null)
			mediaSpec.setHeight(height.toUnit(Dimension.Unit.PX, this).getValue().floatValue());
		if (deviceWidth != null)
			mediaSpec.setWidth(deviceWidth.toUnit(Dimension.Unit.PX, this).getValue().floatValue());
		if (deviceHeight != null)
			mediaSpec.setHeight(deviceHeight.toUnit(Dimension.Unit.PX, this).getValue().floatValue());*/
		return mediaSpec;
	}

	/**
	 * Helper class to build {@link Medium} from {@link MediaQuery}, e.g. for use in {@link
	 * MediumProvider} implementations.
	 */
	public static class MediumBuilder {

		protected Type type = null;
		protected Map<String,Object> features = new HashMap<>();

		public final MediumBuilder parse(MediaQuery query) throws IllegalArgumentException {
			if (query.getType() != null && !"all".equals(query.getType())) {
				Type t; {
					try {
						t = Type.valueOf(query.getType().toUpperCase());
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException("Unexpected medium type: " + query.getType());
					}
				}
				if (type == null)
					type = t;
				else if (!type.equals(t))
					throw new IllegalArgumentException(
						String.format("Incompatible values for medium type: %s AND %s", type, t));
			}
			if (query.isNegative())
				throw new IllegalArgumentException("Unexpected medium: contains NOT: " + query);
			Map<String,Object> features = new HashMap<>();
			for (MediaExpression e : query) {
				if (e.isNegative())
					throw new IllegalArgumentException("Unexpected medium: contains NOT: " + query);
				String f = e.getFeature();
				if (f.startsWith("min-") || f.startsWith("max-"))
					throw new IllegalArgumentException("Unexpected medium: contains " + f.substring(0, 4));
				if (e.size() == 0)
					features.put(f, true);
				else if (e.size() == 1)
					features.put(f, e.get(0));
				else
					throw new IllegalArgumentException("Unexpected value for medium feature: " + e);
			}
			return parse(features);
		}

		protected Object parse(String feature, Object value) throws IllegalArgumentException {
			if ("width".equals(feature) || "height".equals(feature)
			    || "device-width".equals(feature) || "device-height".equals(feature))
				value = parseLength(value, feature);
			else if ("-daisy-document-locale".equals(feature) || "-daisy-locale".equals(feature))
				value = parseLocale(value);
			else if ("-daisy-duplex".equals(feature)
			         || "-daisy-saddle-stitch".equals(feature)
			         || "-daisy-z-folding".equals(feature)
			         || "-daisy-blank-last-page".equals(feature))
				value = parseBoolean(value);
			else
				// don't parse/validate unknown features, just check the type
				// note that any string can be represented by an ident (possibly using
				// escape characters)
				if (!(value instanceof String
				      || value instanceof Integer
				      || value instanceof Boolean
				      || value instanceof Dimension))
					throw new IllegalArgumentException();
			Object prev = features.get(feature);
			if (prev == null)
				features.put(feature, value);
			else if (!prev.equals(value))
				throw new IllegalArgumentException(
					String.format(
						"Incompatible values for medium feature: (%s: %s) AND (%s: %s)", feature, prev, feature, value));
			return value;
		}

		protected String parseIdent(Object value) throws IllegalArgumentException {
			if (value instanceof String)
				return (String)value;
			else if (value instanceof TermIdent)
				return CssEscape.unescapeCss(value.toString());
			else
				throw new IllegalArgumentException("Not an ident: " + value);
		}

		protected Integer parseInteger(Object value) throws IllegalArgumentException {
			if (value instanceof Integer)
				return (Integer)value;
			else if (value instanceof TermInteger)
				return ((TermInteger)value).getIntValue();
			else if (value instanceof String)
				try {
					return Integer.parseInt((String)value);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Not an integer: " + value, e);
				}
			else
				throw new IllegalArgumentException("Not an integer: " + value);
		}

		protected Boolean parseBoolean(Object value) throws IllegalArgumentException {
			if (value instanceof Boolean)
				return (Boolean)value;
			else if (value instanceof Integer)
				switch ((Integer)value) {
				case 0:
					return false;
				case 1:
					return true;
				default:
					throw new IllegalArgumentException("Not a boolean: " + value);
				}
			else if (value instanceof TermInteger)
				return parseBoolean(((TermInteger)value).getIntValue());
			else if (value instanceof String)
				if ("1".equals(value))
					return true;
				else if ("0".equals(value))
					return false;
				else
					return Boolean.parseBoolean((String)value);
			else
				throw new IllegalArgumentException("Not a boolean: " + value);
		}

		protected Locale parseLocale(Object value) throws IllegalArgumentException {
			if (value instanceof Locale)
				return (Locale)value;
			else if (value instanceof TermIdent)
				return parseLocale(value.toString());
			else if (value instanceof String)
				try {
					return new Locale.Builder().setLanguageTag((String)value).build();
				} catch (IllformedLocaleException e) {
					throw new IllegalArgumentException("Not a locale: " + value, e);
				}
			else
				throw new IllegalArgumentException("Not a locale: " + value);
		}

		protected Dimension parseLength(Object value, String feature) throws IllegalArgumentException {
			if (value instanceof Dimension)
				return (Dimension)value;
			else if (value instanceof TermLength)
				return new Dimension(((TermLength)value).getValue(),
				                     ((TermLength)value).getUnit() == TermLength.Unit.none
				                         ? null
				                         : Dimension.Unit.parse(((TermLength)value).getUnit().name()));
			else if (value instanceof String)
				try {
					return Dimension.parse((String)value);
				} catch (IllegalArgumentException e) {
					try {
						return parseLength(parseInteger(value), feature);
					} catch (IllegalArgumentException ee) {
						throw e;
					}
				}
			else if (value instanceof Integer)
				// Grid based media may support integer lengths.
				// Interpreting these lengths (e.g. as multiple of em or ch) is left up to
				// subclasses.  Here we assume we are parsing a media query (see MediaQueryParser),
				// so the exact interpretation does not matter.
				return new Dimension((Integer)value, null);
			else if (value instanceof TermInteger)
				return parseLength(((TermInteger)value).getIntValue(), feature);
			else
				throw new IllegalArgumentException("Not a dimension: " + value);
		}

		private static String normalizeFeature(String feature) {
			if (knownDaisyFeatures.contains(feature))
				feature = "-daisy-" + feature;
			if ("-daisy-file-format".equals(feature))
				feature = "-daisy-format";
			else if ("-daisy-sheets-multiple-of-two".equals(feature))
				feature = "-daisy-saddle-stitch"; // not exactly the same, but we want to support
				                                  // sheets-multiple-of-two for backwards
				                                  // compatibility
			else if ("page-width".equals(feature))
				feature = "device-width";
			else if ("page-height".equals(feature))
				feature = "device-height";
			return feature;
		}

		public Medium build() {
			return new Medium(type,
			                  (Dimension)features.remove("width"),
			                  (Dimension)features.remove("height"),
			                  (Dimension)features.remove("device-width"),
			                  (Dimension)features.remove("device-height"),
			                  features,
			                  this);
		}

	/* /////////////////////////// */
	/* for use in MediaQueryParser */
	/* /////////////////////////// */

		MediumBuilder parse(Map<String,Object> features) throws IllegalArgumentException {
			for (String feature : features.keySet()) {
				if (knownFeatures.contains(feature) && !(
					    "width".equals(feature)
					    || "height".equals(feature)
					    || "device-width".equals(feature)
					    || "device-height".equals(feature)))
					throw new IllegalArgumentException("Unsupported medium feature: " + feature);
				else if (feature.startsWith("-daisy-")
				         && !knownDaisyFeatures.contains(feature.substring("-daisy-".length())))
					throw new IllegalArgumentException("Unknown medium feature: " + feature);
				else {
					Object v = features.get(feature);
					feature = normalizeFeature(feature);
					if (v instanceof TermInteger)
						v = parseInteger(v);
					else if (v instanceof TermIdent)
						v = parseIdent(v);
					else if (v instanceof TermLength)
						v = parseLength(v, feature);
					try {
						parse(feature, v);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(
							String.format("Unexpected value for medium feature: (%s: %s)", feature, v), e);
					}
				}
			}
			return this;
		}
	}

	// also used in MediumProvider
	static Medium fromMediaQuery(MediaQuery query) throws IllegalArgumentException {
		return fromMediaQuery(query, () -> new MediumBuilder());
	}

	private static Medium fromMediaQuery(MediaQuery query, Supplier<MediumBuilder> parser) throws IllegalArgumentException {
		if (query instanceof MediaQueryForMedium)
			return ((MediaQueryForMedium)query).underlyingMedium;
		return parser.get().parse(query).build();
	}

	private MediaQuery mediaQuery = null;

	MediaQuery toMediaQuery() {
		if (mediaQuery == null)
			mediaQuery = new MediaQueryForMedium();
		return mediaQuery;
	}

	private Medium and(Medium medium) throws IllegalArgumentException {
		Type type = this.type; {
			if (medium.type != null)
				if (type == null)
					type = medium.type;
				else if (!medium.type.equals(type))
					throw new IllegalArgumentException("" + this + " can not be combined with " + medium);
		}
		Dimension width = this.width; {
			if (medium.width != null)
				if (width == null)
					width = medium.width;
				else if (!medium.width.equals(width))
					throw new IllegalArgumentException("" + this + " can not be combined with " + medium);
		}
		Dimension height = this.height; {
			if (medium.height != null)
				if (height == null)
					height = medium.height;
				else if (!medium.height.equals(height))
					throw new IllegalArgumentException("" + this + " can not be combined with " + medium);
		}
		Dimension deviceWidth = this.deviceWidth; {
			if (medium.deviceWidth != null)
				if (deviceWidth == null)
					deviceWidth = medium.deviceWidth;
				else if (!medium.deviceWidth.equals(deviceWidth))
					throw new IllegalArgumentException("" + this + " can not be combined with " + medium);
		}
		Dimension deviceHeight = this.deviceHeight; {
			if (medium.deviceHeight != null)
				if (deviceHeight == null)
					deviceHeight = medium.deviceHeight;
				else if (!medium.deviceHeight.equals(deviceHeight))
					throw new IllegalArgumentException("" + this + " can not be combined with " + medium);
		}
		Map<String,Object> nonStandardFeatures = this.nonStandardFeatures; {
			if (!medium.nonStandardFeatures.isEmpty())
				if (nonStandardFeatures.isEmpty())
					nonStandardFeatures = medium.nonStandardFeatures;
				else {
					nonStandardFeatures = new HashMap<String,Object>(nonStandardFeatures);
					for (String f : medium.nonStandardFeatures.keySet()) {
						Object v = medium.nonStandardFeatures.get(f);
						Object vv = nonStandardFeatures.get(f);
						if (vv != null && !vv.equals(v))
							throw new IllegalArgumentException("" + this + " can not be combined with " + medium);
						nonStandardFeatures.put(f, v);
					}
				}
		}
		return new Medium(type, width, height, deviceWidth, deviceHeight, nonStandardFeatures, parser);
	}

	/**
	 * Implementation of {@link MediaQuery} that is immutable and backed by a {@link Medium}
	 */
	private class MediaQueryForMedium extends AbstractList<MediaExpression> implements MediaQuery {

		private final Medium underlyingMedium;
		private final List<Object> features; // list is populated lazily with MediaExpression

		private MediaQueryForMedium() {
			underlyingMedium = Medium.this;
			features = new ArrayList<>();
			if (width != null)
				features.add("width");
			if (height != null)
				features.add("height");
			if (deviceWidth != null)
				features.add("device-width");
			if (deviceHeight != null)
				features.add("device-height");
			features.addAll(nonStandardFeatures.keySet());
		}

		public String getType() {
			Type t = underlyingMedium.getType();
			if (t == null)
				return null;
			return t.toString();
		}

		@Override
		public int size() {
			return features.size();
		}

		@Override
		public MediaExpression get(int index) {
			if (features.get(index) instanceof String)
				features.set(index, new MediaExpressionImpl((String)features.get(index)));
			return (MediaExpression)features.get(index);
		}

		@Override
		public boolean isNegative() {
			return false;
		}

		@Override
		public List<MediaExpression> asList() {
			return this;
		}

		@Override
		public MediaQuery and(MediaQuery query) {
			return underlyingMedium.and(Medium.fromMediaQuery(query)).toMediaQuery();
		}

		@Override
		public String toString() {
			return underlyingMedium.toString();
		}

		@Override
		public void setType(String type) {
			throw new UnsupportedOperationException("immutable object");
		}

		@Override
		public Rule<MediaExpression> replaceAll(List<MediaExpression> replacement) {
			throw new UnsupportedOperationException("immutable object");
		}

		@Override
		public void setNegative(boolean negative) {
			throw new UnsupportedOperationException("immutable object");
		}

		@Override
		public Rule<MediaExpression> unlock() {
			throw new UnsupportedOperationException("immutable object");
		}
	}

	private class MediaExpressionImpl extends AbstractList<Term<?>> implements MediaExpression {

		private final String feature;
		private Term<?> value = null; // value computed lazily

		private MediaExpressionImpl(String feature) {
			this.feature = feature;
		}

		@Override
		public String getFeature() {
			return feature;
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public Term<?> get(int index) {
			if (index != 0)
				throw new IndexOutOfBoundsException("index must be 0");
			if (value == null)
				if ("width".equals(feature))
					value = new UnmodifiableTermLength(width);
				else if ("height".equals(feature))
					value = new UnmodifiableTermLength(height);
				else if ("device-width".equals(feature))
					value = new UnmodifiableTermLength(deviceWidth);
				else if ("device-height".equals(feature))
					value = new UnmodifiableTermLength(deviceHeight);
				else {
					Object v = nonStandardFeatures.get(feature);
					if (v instanceof Integer)
						value = new UnmodifiableTermInteger((Integer)v);
					else if (v instanceof Boolean)
						value = new UnmodifiableTermInteger(((Boolean)v) ? 1 : 0);
					else if (v instanceof Dimension)
						value = new UnmodifiableTermLength((Dimension)v);
					else if (v instanceof Locale)
						value = new UnmodifiableTermIdent(((Locale)v).toLanguageTag());
					else
						value = new UnmodifiableTermIdent(CssEscape.escapeCssIdentifierMinimal(v.toString()));
				}
			return value;
		}

		@Override
		public boolean isNegative() {
			return false;
		}

		@Override
		public List<Term<?>> asList() {
			return this;
		}

		@Override
		public void setFeature(String feature) {
			throw new UnsupportedOperationException("immutable object");
		}

		@Override
		public Rule<Term<?>> replaceAll(List<Term<?>> replacement) {
			throw new UnsupportedOperationException("immutable object");
		}

		@Override
		public void setNegative(boolean negative) {
			throw new UnsupportedOperationException("immutable object");
		}

		@Override
		public Rule<Term<?>> unlock() {
			throw new UnsupportedOperationException("immutable object");
		}
	}

	private static class UnmodifiableTermIdent extends UnmodifiableTerm<String> implements TermIdent {

		private final String value;

		private UnmodifiableTermIdent(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	private static class UnmodifiableTermInteger extends UnmodifiableTerm<Float> implements TermInteger {

		private final int value;

		private UnmodifiableTermInteger(int value) {
			this.value = value;
		}

		@Override
		public Float getValue() {
			return Float.valueOf(value);
		}

		@Override
		public TermNumeric.Unit getUnit() {
			return null;
		}

		@Override
		public boolean isPercentage() {
			return false;
		}

		@Override
		public int getIntValue() {
			return value;
		}

		@Override
		public TermInteger setValue(int value) {
			throw new UnsupportedOperationException("immutable object");
		}

		@Override
		public TermInteger setUnit(TermNumeric.Unit unit) {
			throw new UnsupportedOperationException("immutable object");
		}

		@Override
		public String toString() {
			return "" + value;
		}
	}

	private static class UnmodifiableTermLength extends UnmodifiableTerm<Float> implements TermLength {

		private final float value;
		private final TermNumeric.Unit unit;

		private UnmodifiableTermLength(Dimension length) {
			this.value = length.getValue().floatValue();
			this.unit = length.getUnit() != null
				? TermNumeric.Unit.valueOf(TermNumeric.Unit.class, length.getUnit().name().toLowerCase())
				: TermNumeric.Unit.none;
		}

		private UnmodifiableTermLength(float value, TermNumeric.Unit unit) {
			this.value = value;
			this.unit = unit;
		}

		@Override
		public Float getValue() {
			return value;
		}

		@Override
		public TermNumeric.Unit getUnit() {
			return unit;
		}

		@Override
		public boolean isPercentage() {
			return false;
		}

		@Override
		public TermLength setUnit(TermNumeric.Unit unit) {
			throw new UnsupportedOperationException("immutable object");
		}

		@Override
		public String toString() {
			return "" + value + unit.toString();
		}
	}
}
