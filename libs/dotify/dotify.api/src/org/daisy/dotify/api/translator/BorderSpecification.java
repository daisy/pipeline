package org.daisy.dotify.api.translator;

/**
 * Provides a specification for a border.
 * 
 * @author Joel Håkansson
 */
public class BorderSpecification {

	/**
	 * Defines types of borders
	 */
	public enum Style {
		/**
		 * Defines that a border is not there.
		 */
		NONE,
		/**
		 * Defines a solid border.
		 */
		SOLID
	}

	/**
	 * Defines border alignments
	 *
	 */
	public enum Align {
		/**
		 * Defines that the border line should be close to the content
		 */
		INNER,
		/**
		 * Defines that the border line should be equally close to the surrounded
		 * content as it is to the surrounding content.
		 */
		CENTER,
		/**
		 * Defines that the border line should be far from the content
		 */
		OUTER;
		/**
		 * Returns the offset from the outer edge of the specified value.
		 * @param w the width to align
		 * @return returns the alignment value
		 */
		public int align(int w) {
			switch (this) {
			case INNER: return w;
			case CENTER: return (int)Math.ceil(w/2d);
			case OUTER: default: return 0;
			}
		}
	}

	static final int DEFAULT_WIDTH = 1;
	
	/**
	 * Provides a builder for a border specification.
	 */
	public static class Builder {
		private Style style;
		private Integer width;
		private Align align;
		
		/**
		 * Creates a new builder.
		 */
		public Builder() {
			this.style = null;
			this.width = null;
			this.align = null;
		}
		
		/**
		 * Sets the border style.
		 * @param value the border style
		 * @return returns this builder
		 */
		public Builder style(Style value) {
			this.style = value;
			return this;
		}
		
		/**
		 * Sets the border alignment.
		 * @param value the border alignment
		 * @return returns this builder
		 */
		public Builder align(Align value) {
			this.align = value;
			return this;
		}
		
		/**
		 * Sets the width of the border.
		 * @param value the border width
		 * @return returns this builder
		 */
		public Builder width(Integer value) {
			this.width = value;
			return this;
		}
		
		/**
		 * Creates a new border specification.
		 * @return returns a new border specification
		 */
		public BorderSpecification build() {
			return new BorderSpecification(this, null);
		}
		
		/**
		 * Creates a new border specification with the supplied fallback
		 * border.
		 * @param fallback the fallback border specification
		 * @return returns a new border specification
		 */
		public BorderSpecification build(BorderSpecification fallback) {
			return new BorderSpecification(this, fallback);
		}
	}
	
	private final Style style;
	private final int width;
	private final Align align;

	private BorderSpecification(Builder builder, BorderSpecification fallback) {
		if (builder.style==null) {
			this.style = (fallback==null?null:fallback.getStyle());
		} else {
			this.style = builder.style;
		}
		if (builder.width==null) {
			this.width = (fallback==null?DEFAULT_WIDTH:fallback.getWidth());
		} else {
			this.width = builder.width;
		}
		if (builder.align==null) {
			this.align = (fallback==null?null:fallback.getAlign());
		} else {
			this.align = builder.align;
		}
	}
	
	/**
	 * Gets the style of this border
	 * @return returns the style
	 */
	public Style getStyle() {
		return style;
	}
	
	/**
	 * Gets the width of this border
	 * @return returns the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the alignment of this border
	 * @return returns the alignment
	 */
	public Align getAlign() {
		return align;
	}

}