package org.daisy.dotify.text.impl;

import java.text.MessageFormat;

class EnInt2TextLocalization extends BasicInteger2Text {

	@Override
	public String getDefinedValue(int value) throws UndefinedNumberException {
		switch (value) {
			case 0:
				return "zero";
			case 1:
				return "one";
			case 2:
				return "two";
			case 3:
				return "three";
			case 4:
				return "four";
			case 5:
				return "five";
			case 6:
				return "six";
			case 7:
				return "seven";
			case 8:
				return "eight";
			case 9:
				return "nine";
			case 10:
				return "ten";
			case 11:
				return "eleven";
			case 12:
				return "twelve";
			case 13:
				return "thirteen";
			case 14:
				return "fourteen";
			case 15:
				return "fifteen";
			case 16:
				return "sixteen";
			case 17:
				return "seventeen";
			case 18:
				return "eighteen";
			case 19:
				return "nineteen";
			case 20:
				return "twenty";
			case 30:
				return "thirty";
			case 40:
				return "forty";
			case 50:
				return "fifty";
			case 60:
				return "sixty";
			case 70:
				return "seventy";
			case 80:
				return "eighty";
			case 90:
				return "ninety";
			default:
				throw new UndefinedNumberException();
		}
	}

	@Override
	public String formatNegative(String value) {
		return MessageFormat.format("minus {0}", value);
	}

	@Override
	public String formatThousands(String th, String rem) {
		if ("".equals(rem)) {
			return MessageFormat.format("{0} thousand", th);
		} else {
			return MessageFormat.format("{0} thousand {1}", th, rem);
		}
	}

	@Override
	public String formatHundreds(String hu, String rem) {
		if ("".equals(hu) && "".equals(rem)) {
			return "hundred";
		} else if ("".equals(hu)) {
			return MessageFormat.format("hundred {0}", rem);
		} else if ("".equals(rem)) {
			return MessageFormat.format("{0} hundred", hu);
		} else {
			return MessageFormat.format("{0} hundred {1}", hu, rem);
		}
	}

	@Override
	public String formatTens(String tens, String rem) {
		return MessageFormat.format("{0}-{1}", tens, rem);
	}

	@Override
	public String postProcess(String value) {
		return value;
	}

}
