package org.daisy.dotify.api.formatter;


/**
 * Provides a numeral field.
 * 
 * @author Joel Håkansson
 */
public class NumeralField implements Field {

	private final NumeralStyle style;
	private final String textStyle;
	
	/**
	 * Creates a new numeral with the supplied style.
	 * @param style the style for this numeral
	 */
	public NumeralField(NumeralStyle style) {
		this(style, null);
	}
	
	public NumeralField(NumeralStyle style, String textStyle) {
		this.style = style;
		this.textStyle = textStyle;
	}
	
	/**
	 * Gets the style for this numeral.
	 * @return the style for this numeral
	 * @deprecated
	 */
	public NumeralStyle getStyle() {
		return style;
	}
	
	/**
	 * Gets the numeral style.
	 * @return the numeral style
	 */
	public NumeralStyle getNumeralStyle() {
		return style;
	}

	@Override
	public String getTextStyle() {
		return textStyle;
	}


}
