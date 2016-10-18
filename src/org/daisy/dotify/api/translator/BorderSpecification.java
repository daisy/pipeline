package org.daisy.dotify.api.translator;

public class BorderSpecification {

	public enum Style {
		NONE,
		SOLID
	}

	public enum Align {
		INNER,
		CENTER,
		OUTER;
		public int align(int w) {
			switch (this) {
			case INNER: return w;
			case CENTER: return (int)Math.ceil(w/2d);
			case OUTER: default: return 0;
			}
		}
	}

	static final int DEFAULT_WIDTH = 1;
	
	public static class Builder {
		private Style style;
		private Integer width;
		private Align align;
		
		public Builder() {
			this.style = null;
			this.width = null;
			this.align = null;
		}
		
		public Builder style(Style value) {
			this.style = value;
			return this;
		}
		
		public Builder align(Align value) {
			this.align = value;
			return this;
		}
		
		public Builder width(Integer value) {
			this.width = value;
			return this;
		}
		
		public BorderSpecification build() {
			return new BorderSpecification(this, null);
		}
		
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
	
	public Style getStyle() {
		return style;
	}
	
	public int getWidth() {
		return width;
	}

	public Align getAlign() {
		return align;
	}

}