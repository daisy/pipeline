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
	 * Creates a new numeral field with the supplied style.
	 * @param style the style for this numeral
	 */
	public NumeralField(NumeralStyle style) {
		this(style, null);
	}
	
	/**
	 * Creates a new numeral field with the supplied numeral and text styles
	 * @param style the numeral style
	 * @param textStyle the text style
	 */
	public NumeralField(NumeralStyle style, String textStyle) {
		this.style = style;
		this.textStyle = textStyle;
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