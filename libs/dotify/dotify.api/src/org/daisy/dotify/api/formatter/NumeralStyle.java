package org.daisy.dotify.api.formatter;


/**
 * Defines numeral styles
 */
public enum NumeralStyle {
	/**
	 * Defines default numeral style, alias of DECIMAL
	 */
	DEFAULT,
	/**
	 * Defines a decimal numeral style
	 */
	DECIMAL,
	/**
	 * Defines a number with a single leading zero if the number is &lt; 10 (01, 02, 03, etc.)
	 */
	DECIMAL_LEADING_ZERO,
	/**
	 * Defines roman numeral style, alias of UPPER_ROMAN
	 */
	ROMAN,
	/**
	 * Defines upper roman numeral style
	 */
	UPPER_ROMAN,
	/**
	 * Defines lower roman numeral style
	 */
	LOWER_ROMAN,
	/**
	 * Defines alpha numeral style, alias of UPPER_ALPHA
	 */
	ALPHA,
	/**
	 * Defines upper alpha numeral style
	 */
	UPPER_ALPHA,
	/**
	 * Defines lower alpha numeral style
	 */
	LOWER_ALPHA;
	
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
			case DECIMAL_LEADING_ZERO:
				return (i<10?"0":"")+i;
			case DEFAULT: 
			case DECIMAL:
			default:
				return "" + i;
		}
	}
};