package org.daisy.dotify.api.formatter;


/**
 * Provides a numeral field.
 * 
 * @author Joel Håkansson
 */
public class NumeralField implements Field {
	/**
	 * Defines numeral styles
	 */
	public static enum NumeralStyle {
		/**
		 * Defines default numeral style
		 */
		DEFAULT,
		/**
		 * Defines roman numeral style
		 */
		ROMAN, UPPER_ROMAN, LOWER_ROMAN,
		/**
		 * Defines alpha numeral style
		 */
		ALPHA, UPPER_ALPHA, LOWER_ALPHA;
	};

	private NumeralStyle style;
	
	/**
	 * Creates a new numeral with the supplied style.
	 * @param style the style for this numeral
	 */
	public NumeralField(NumeralStyle style) {
		this.style = style;
	}
	
	/**
	 * Gets the style for this numeral.
	 * @return the style for this numeral
	 */
	public NumeralStyle getStyle() {
		return style;
	}


}
