package org.daisy.dotify.impl.translator;

import java.util.logging.Logger;

class BorderSpecification {
	final static String KEY_STYLE = "style";
	final static String KEY_WIDTH = "width";
	final static String KEY_ALIGN = "align";
	final static int DEFAULT_WIDTH = 1;
	
	enum Style {
		NONE,
		SOLID
	}

	enum Align {
		INNER,
		OUTER
	}
	
	private Style style;
	private Integer width;
	private Align align;
	private final BorderSpecification fallback;

	BorderSpecification() {
		this(null);

	}
	BorderSpecification(BorderSpecification fallback) {
		this.fallback = fallback;
		if (fallback==null) {
			//since this definition does not have a fallback, provide defaults.
			this.style = Style.NONE;
			this.width = DEFAULT_WIDTH;
			this.align = Align.OUTER;
		} else {
			this.style = null;
			this.width = null;
			this.align = null;
		}
	}

	public void set(String key, String value) {
		key = key.toLowerCase();
		value = value.toUpperCase();
		if (key.equals(KEY_STYLE)) {
			setStyle(Style.valueOf(value));
		} else if (key.equals(KEY_WIDTH)) {
			try {
				width = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				//ignore
				Logger.getLogger(this.getClass().getCanonicalName()).warning("Ignoring unparsable value: " + value);
			}
		} else if (key.equals(KEY_ALIGN)) {
			setAlign(Align.valueOf(value));
		} else {
			throw new IllegalArgumentException("Unkown value '" + value + "' for " + key);
		}
	}
	
	public Style getStyle() {
		if (style==null) {
			return (fallback==null?null:fallback.getStyle());
		} else {
			return style;
		}
	}
	
	public void setStyle(Style style) {
		this.style = style;
	}

	public int getWidth() {
		if (width==null) {
			return (fallback==null?DEFAULT_WIDTH:fallback.getWidth());
		}
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public Align getAlign() {
		if (align==null) {
			return (fallback==null?null:fallback.getAlign());
		}
		return align;
	}

	public void setAlign(Align align) {
		this.align = align;
	}
			
}