package org.daisy.dotify.text.impl;

import org.daisy.dotify.api.text.Integer2Text;
import org.daisy.dotify.api.text.IntegerOutOfRange;

abstract class BasicInteger2Text implements Integer2Text {

	protected BasicInteger2Text() {
	}
	
	/**
	 * Gets a lookup value value.
	 * 
	 * @param value
	 *            the value to look up
	 * @return returns the string for the lookup value
	 * @throws UndefinedNumberException
	 *             if the value cannot is not defined
	 */
	abstract String getDefinedValue(int value) throws UndefinedNumberException;

	/**
	 * Formats the value as negative. The value will be intToText(-x)
	 * 
	 * @param value
	 *            the number as text
	 * @return returns the formatted string
	 */
	abstract String formatNegative(String value);

	/**
	 * Formats thousands. E.g. if the value is 9700 the first argument
	 * will be intToText(9) and the second will be intToText(700).
	 * 
	 * @param thousands
	 *            the thousands as text
	 * @param rem
	 *            the remainder as text
	 * 
	 * @return returns the formatted string
	 */
	abstract String formatThousands(String thousands, String rem) throws IntegerOutOfRange;

	/**
	 * Formats hundreds. E.g. if the value is 120 the first argument
	 * will be intToText(1) and the second will be intToText(20).
	 * 
	 * @param hundreds
	 *            the hundreds as text
	 * @param rem
	 *            the remainder as text
	 * @return returns the formatted string
	 */
	abstract String formatHundreds(String hundreds, String rem) throws IntegerOutOfRange;

	/**
	 * Formats tens. E.g. if the value is 38 the first argument
	 * will be intToText(30) and the second will be intToText(8).
	 * 
	 * @param tens
	 *            the tens as text
	 * @param rem
	 *            the remainder as text
	 * @return returns the formatted string
	 */
	abstract String formatTens(String tens, String rem);

	/**
	 * Applies post processing to the processed result
	 * 
	 * @param value
	 *            the conversion result
	 * @return returns the processed value
	 */
	abstract String postProcess(String value);

	@Override
	public String intToText(int value) throws IntegerOutOfRange {
		return postProcess(intToTextInner(value));
	}

	private String intToTextInner(int value) throws IntegerOutOfRange {
		if (value >= 10000) {
			throw new IntegerOutOfRange("Value out of range: " + value);
		} else if (value < 0) {
			return formatNegative(intToTextInner(-value));
		} else {
			try {
				return getDefinedValue(value);
			} catch (UndefinedNumberException e) {
				// no defined value, try to divide...
			}
			if (value >= 1000) {
				int rem = value % 1000;
				return formatThousands(intToTextInner(value / 1000), (rem > 0 ? intToTextInner(rem) : ""));
			} else if (value >= 100) {
				int rem = value % 100;
				return formatHundreds(intToTextInner(value / 100), (rem > 0 ? intToTextInner(rem) : ""));
			} else if (value >= 20) {
				int t = value % 10;
				int r = (value / 10) * 10;
				return formatTens(intToTextInner(r), (t > 0 ? intToTextInner(t) : ""));
			} else {
				throw new RuntimeException();
			}
		}
	}

}
