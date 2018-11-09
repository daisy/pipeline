package org.daisy.dotify.translator.impl.liblouis;
enum CharClass {
	//groups from liblouis
	PUNCTUATION('p'),
	SPACE('s'),
	MATH('M'),
	SIGN('S'),
	UPPERCASE('U'),
	LOWERCASE('l'),
	DIGIT('d'),
	UNDEFINED('u'),
	
	//internal additions
	BRAILLE('b');
	
	private final char token;
	private CharClass(char token) {
		this.token = token;
	}
	
	/**
	 * Gets the token character to be used to represent this enum value.
	 * @return returns the token character
	 */
	public char token() {
		return token;
	}
}