package org.daisy.pipeline.css;

import java.util.List;

import cz.vutbr.web.css.MediaExpression;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;

public class Medium {

	public enum Type {
		EMBOSSED,
		PRINT;
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	private final Type type;
	private final Integer width;
	private final Integer height;

	private Medium(Type type, Integer width, Integer height) {
		switch (type) {
		case EMBOSSED:
			this.type = type;
			this.width = width;
			this.height = height;
			break;
		case PRINT:
			if (width != null) {
				throw new IllegalArgumentException("Unexpected 'width' argument for medium 'print'");
			}
			if (height != null) {
				throw new IllegalArgumentException("Unexpected 'height' argument for medium 'print'");
			}
			this.type = type;
			this.width = this.height = null;
			break;
		default:
			throw new IllegalArgumentException("Unexpected medium: " + type);
		}
	}

	/**
	 * The media type: "embossed" or "print".
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

	public boolean matches(String mediaQuery) {
		return asMediaSpec().matchesOneOf(CSSParserFactory.getInstance().parseMediaQuery(mediaQuery));
	}

	public String toString() {
		StringBuilder s  = new StringBuilder();
		s.append(type.toString());
		if (width != null)
			s.append(" AND (width:").append(width).append(")");
		if (height != null)
			s.append(" AND (height:").append(height).append(")");
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

	public static Medium parse(String medium) {
		List<MediaQuery> q = CSSParserFactory.getInstance().parseMediaQuery(medium);
		if (q.size() != 1)
			throw new IllegalArgumentException("Unexpected medium: " + medium);
		Type type = null;
		Integer width = null;
		Integer height = null;
		try {
			type = Type.valueOf(q.get(0).getType().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unexpected medium type: " + q.get(0).getType());
		}
		if (q.get(0).isNegative())
			throw new IllegalArgumentException("Unexpected medium: contains NOT: " + medium);
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
			} else
				throw new IllegalArgumentException("Unexpected medium feature: " + feature);
		}
		return new Medium(type, width, height);
	}

	private MediaSpec mediaSpec;

	/**
	 * The medium as a jStyleParser {@link MediaSpec} object.
	 */
	public MediaSpec asMediaSpec() {
		if (mediaSpec == null)
			switch (type) {
			case EMBOSSED:
				MediaSpec spec = new MediaSpec(type.toString()) {
						// overriding getExpressionLengthPx() because MediaSpec expects width and height
						// to be in pixels but width and height are specified in braille cells
						@Override
						protected Float getExpressionLengthPx(MediaExpression e) {
							return (float)getExpressionInteger(e);
						}
					};
				if (width != null)
					spec.setWidth((float)width);
				if (height != null)
					spec.setWidth((float)height);
				mediaSpec = spec;
				break;
			default:
				mediaSpec = new MediaSpec(type.toString());
				break;
			}
		return mediaSpec;
	}
}
