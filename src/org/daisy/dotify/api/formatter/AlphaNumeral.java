package org.daisy.dotify.api.formatter;

class AlphaNumeral {
	private static final int size = 26;

	public static String int2alpha(int n) {
		if (n < 1) {
			throw new NumberFormatException("Value must be bigger than zero.");
		}
		n = n - 1;
		int pos = n % size;
		if (n >= size) {
			return int2alpha((n - pos) / size) + (char) (pos + 65);
		} else {
			return "" + (char) (pos + 65);
		}
	}

}
