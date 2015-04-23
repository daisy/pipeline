package org.daisy.dotify.common.text;

class NonStandardHyphenationInfo {
	private final String pattern;
	private final String replacement;
	private final int length;
	
	/**
	 * Creates a new NonStandardHyphenationInfo
	 * @param replacement the replacement contains the characters to insert
	 * @param pattern
	 * 
	 *  
	 */
	NonStandardHyphenationInfo(String pattern, String replacement) {
		super();
		this.pattern = pattern;
		this.replacement = replacement;
		int charsCount = 0;
		for (int i =0; i<pattern.length(); i++) {
			if (i>charsCount+2) {
				throw new IllegalArgumentException("Only one soft hyphen or zero width space allowed.");
			} else if (pattern.charAt(i)!='\u00ad' && pattern.charAt(i)!='\u200b') {
				charsCount++;
			}
		}
		if (charsCount<pattern.length()-1) {
			throw new IllegalArgumentException("Exactly one soft hyphen or zero width space must be within range.");
		}
		this.length = pattern.length();
	}
	
	NonStandardHyphenationInfo(String replacement, int length) {
		super();
		this.pattern = null;
		this.replacement = replacement;
		this.length = length;
	}

	/**
	 * Gets the length of the string from the offset up to the breakpoint (and including it, if it is a soft hyphen)
	 * @return returns the length
	 */
	static int getHeadLength(String input, int offset) {
		int charsCount = 0;
		for (int i=offset; i<input.length(); i++) {
			if (input.charAt(i)=='\u00ad') {
				return charsCount+1;
			} else if (input.charAt(i)=='\u200b') {
				return charsCount;
			} else {
				charsCount++;
			}
		}
		return charsCount;
	}
	
	/**
	 * Replaces the occurrence of the input string in the supplied string at the specified offset.
	 * @param offset
	 * @param charsStr
	 * @return
	 */
	String apply(String charsStr, int offset) {
		if (pattern!=null) {
			for (int i =0; i<pattern.length(); i++) {
				if (pattern.charAt(i)!=charsStr.charAt(offset+i)) {
					throw new IllegalArgumentException("Cannot apply pattern.");
				}
			}
		}
		StringBuilder ns = new StringBuilder();
		ns.append(charsStr.substring(0, offset));
		ns.append(replacement);
		ns.append(charsStr.substring(offset+length, charsStr.length()));
		return ns.toString();
	}

}
