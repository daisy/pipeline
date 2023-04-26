package org.daisy.braille.css;

import java.util.regex.Pattern;

import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.csskit.TermImpl;

import java.io.UnsupportedEncodingException;

public class TermDotPattern extends TermImpl<Character> {

	private static final Pattern DOT_PATTERN_RE = Pattern.compile("[\u2800-\u28ff]");

	private TermDotPattern() {}

	@Override
	public TermDotPattern setValue(Character value) {
		if (value == null) {
			throw new IllegalArgumentException(
					"Invalid value for TermDotPattern(null)");
		}
		if (!DOT_PATTERN_RE.matcher("" + value).matches()) {
			throw new IllegalArgumentException("Invalid value for TermDotPattern(" + value + ")");
		}
		this.value = value;
		return this;
	}
	
	public static TermDotPattern createDotPattern(TermIdent ident) {
		TermDotPattern pattern = new TermDotPattern();
		String value = ident.getValue();
		if (value.length() != 1) {
			throw new IllegalArgumentException(
					"Invalid value for TermDotPattern(" + value + ")");
		}
		pattern.setValue(value.charAt(0));
		return pattern;
	}
}
