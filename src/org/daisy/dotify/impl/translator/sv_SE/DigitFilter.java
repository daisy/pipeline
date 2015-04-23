package org.daisy.dotify.impl.translator.sv_SE;

import org.daisy.dotify.common.text.CombinationFilter;
import org.daisy.dotify.common.text.RegexFilter;
import org.daisy.dotify.common.text.StringFilter;

class DigitFilter implements StringFilter {
	enum Algorithm {
		/**
		 * Process using regular expressions.
		 */
		REGEX,
		/**
		 * Process using a specialized algorithm.
		 */
		SPECIALIZED}
	
	private final CombinationFilter filters;
	private final Algorithm mode;

	/**
	 * Creates a new instance with the default algorithm.
	 */
	DigitFilter() {
		this(Algorithm.SPECIALIZED);
	}

	/**
	 * Creates a new instance with the specified algorithm.
	 * @param mode the algorithm to use
	 */
	DigitFilter(Algorithm mode) {
		filters = new CombinationFilter();
		// One or more digit followed by zero or more digits, commas or periods
		filters.add(new RegexFilter("([\\d]+[\\d,\\.]*)", "\u283c$1"));
		// Insert a "reset character" between a digit and lower case a-j
		filters.add(new RegexFilter("([\\d])([a-j])", "$1\u2831$2"));
		this.mode = mode;
	}

	Algorithm getAlgorithm() {
		return mode;
	}

	public String filter(String str) {
		switch (mode) {
			case REGEX: return filterRegex(str);
			case SPECIALIZED: default: return filterSpecialized(str);
		}
	}

	private String filterSpecialized(String str) {
		int codePoint;
		
		StringBuilder sb = new StringBuilder(str.length());
		boolean inNumber = false;
		boolean lastWasDigit = false;
		int last = 0;
		int offset;
		for (offset = 0; offset < str.length(); offset += Character.charCount(codePoint)) {
			codePoint = str.codePointAt(offset);
			if (((codePoint-'a') | ('j'-codePoint)) >= 0) {
				if (lastWasDigit) {
					sb.append(str, last, offset);
					last = offset;
					sb.append('\u2831');
				}
				inNumber = false;
				lastWasDigit = false;
			} else if (((codePoint-'0') | ('9'-codePoint)) >= 0) {
				if (!inNumber) {
					sb.append(str, last, offset);
					last = offset;
					sb.append('\u283c');
				}
				inNumber = true;
				lastWasDigit = true;
			} else if (codePoint==(int)'.' || codePoint==(int)',') {
				lastWasDigit = false;
			} else {
				lastWasDigit = false;
				inNumber = false;
			}
		}
		sb.append(str, last, offset);
		last = offset;
		return sb.toString();
	}

	private String filterRegex(String str) {
		return filters.filter(str);
	}

}
