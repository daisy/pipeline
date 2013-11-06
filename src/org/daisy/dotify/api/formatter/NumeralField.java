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
