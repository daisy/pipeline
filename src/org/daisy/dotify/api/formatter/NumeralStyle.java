package org.daisy.dotify.api.formatter;


/**
 * Defines numeral styles
 */
public enum NumeralStyle {
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
	
	/**
	 * Formats the numeral with the given style
	 * 
	 * @param i
	 *            the number
	 * @return returns the formatted number
	 */
	public String format(int i) {
		switch (this) {
			case ROMAN:
			case UPPER_ROMAN:
				return RomanNumeral.int2roman(i);
			case LOWER_ROMAN:
				return RomanNumeral.int2roman(i).toLowerCase();
			case ALPHA:
			case UPPER_ALPHA:
				return AlphaNumeral.int2alpha(i);
			case LOWER_ALPHA:
				return AlphaNumeral.int2alpha(i).toLowerCase();
			case DEFAULT:
			default:
				return "" + i;
		}
	}
};