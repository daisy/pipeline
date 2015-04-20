package org.daisy.dotify.impl.translator.sv_SE;

import java.util.regex.Pattern;

import org.daisy.dotify.text.StringFilter;

import se.mtm.common.text.SplitResult;
import se.mtm.common.text.StringSplitter;


/**
 * Adds Swedish braille capitalization markers to a String.
 * @author Joel Håkansson, TPB
 */
class CapitalizationMarkers implements StringFilter {
	private final static String CHAR_MARKER = "\u2820";
	private final static String WORD_MARKER = "\u2820\u2820";
	private final static String WORD_PART_PREFIX = "\u2820\u2820";
	private final static String WORD_PART_POSTFIX = "\u2831";
	private final static String SEQ_PREFIX_MARKER = "\u2820\u2820\u2820";
	private final static String SEQ_POSTFIX_MARKER = "\u2831";
	private final Pattern p1;
	private final Pattern p2;
	private final Pattern p3;
	private final Pattern p4;
	private final Pattern p5;
	private final Pattern p6;
	
	public CapitalizationMarkers() {
		this.p1 = Pattern.compile("(?<=[^\\p{L}]|\\A)(((\\p{Lu}(\u00ad)?)+[\\s\\-/]+)+(\\p{Lu}(\u00ad)?)+)(?=[^\\p{L}^\u283c^\\d]|\\z)");
		this.p2 = Pattern.compile("([\\p{Lu}][\\s]+)+[\\p{Lu}]");
		this.p3 = Pattern.compile("(\\p{Lu})");
		this.p4 = Pattern.compile("[\\p{L}[\\-\\d\u00ad]]+");
		this.p5 = Pattern.compile("\\A(\\p{Lu}(\u00ad)?){2,}\\z");
		this.p6 = Pattern.compile("(\\A|(?<=\\-))(\\p{Lu}(\u00ad)?)+");
	}

	public String filter(String str) {
		return addCapitalizationMarkers(str);
	}

	private String addCapitalizationMarkers(String input) {
		StringBuffer ret = new StringBuffer();
		// Match an upper case sequence with whitespace, '-' or '/' in between,
		// if preceded by beginning of input or any non letter character and
		// followed by end of input or any non letter character except a digit or 0x283c
		//^\\-^/^\\d
		for (SplitResult sr : StringSplitter.split(input, p1)) {
			String s = sr.getText();
			if (sr.isMatch()) {
				if (p2.matcher(s).matches()) {
					// String is a group of single capital letters, e.g: 'E X A M P L E'
					ret.append(p3.matcher(s).replaceAll(CHAR_MARKER + "$1"));
				} else {
					// String is a group of capitalized words.
					String asGroup = markAsGroup(s);
					String asWords = markAsWords(s);
					// compare to see which is shorter
					if (asGroup.length()<=asWords.length()) {
						// group rendering is equally efficient, or better, use it
						ret.append(asGroup);
					} else {
						// word rendering is shorter, use it
						ret.append(asWords);
					}
				}
			} else {
				// String is not an all caps group
				ret.append(markAsWords(s));
			}
		}
		return ret.toString();
	}
	
	private String markAsGroup(String s) {
		return SEQ_PREFIX_MARKER+s+SEQ_POSTFIX_MARKER;
	}

	private String markAsWords(String s) {
		StringBuffer ret = new StringBuffer();
		// Split on words, or word like character groups (such as passwords)
		for (SplitResult tr : StringSplitter.split(s, p4)) {
			String t = tr.getText();
			if (tr.isMatch()) {
				// String is a word, e.g. 'hello', 'pqr6XWr', 'ISBN-centralen'
				if (p5.matcher(t).matches()) {
					// String is a single capitalized word longer than one letter, e.g. 'OK'
					ret.append(WORD_MARKER+t);
				} else {
					// String contains non upper case letters or other characters
					for (SplitResult ur : StringSplitter.split(t, p6)) {
						String u = ur.getText();
						if (ur.isMatch() && u.length()>2) {
							// Input begins with upper case letters
							ret.append(WORD_PART_PREFIX);
							ret.append(u);
							ret.append(WORD_PART_POSTFIX);
						} else {
							// Use a single upper case mark for all upper case letters
							ret.append(p3.matcher(u).replaceAll(CHAR_MARKER+"$1"));
						}
					}
				}
			} else {
				// String consists of word separator characters, just add to output
				ret.append(t);
			}
		}
		return ret.toString();
	}

}
