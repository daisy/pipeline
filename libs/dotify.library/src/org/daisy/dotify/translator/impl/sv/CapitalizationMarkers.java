package org.daisy.dotify.translator.impl.sv;

import org.daisy.dotify.common.text.SplitResult;
import org.daisy.dotify.common.text.StringFilter;
import org.daisy.dotify.common.text.StringSplitter;

import java.util.regex.Pattern;


/**
 * Adds Swedish braille capitalization markers to a String.
 *
 * @author Joel Håkansson
 */
class CapitalizationMarkers implements StringFilter {
    private static final String CHAR_MARKER = "\u2820";
    private static final String WORD_MARKER = "\u2820\u2820";
    private static final String WORD_PART_PREFIX = "\u2820\u2820";
    private static final String WORD_PART_POSTFIX = "\u2831";
    private static final String SEQ_PREFIX_MARKER = "\u2820\u2820\u2820";
    private static final String SEQ_POSTFIX_MARKER = "\u2831";
    /**
     * Matches sequences of upper case letters and soft hyphens with white spaces, dashes and forward slashes in
     * between, starting at the beginning of input or after a non-letter character and ending at the end of input
     * or before any character that isn't a letter, the braille number symbol (⠼)  or a digit.
     */
    private static final Pattern UPPERCASE_LETTER_SEQUENCE_SPACE_DASH_SLASH = Pattern.compile(
        "(?<=[^\\p{L}]|\\A)(((\\p{Lu}(\u00ad)?)+[\\s\\-/]+)+(\\p{Lu}(\u00ad)?)+)(?=[^\\p{L}^\u283c^\\d]|\\z)"
    );
    /**
     * Matches sequences of upper case letters with one or more whitespace in between.
     */
    private static final Pattern UPPERCASE_LETTER_SEQUENCE_WITH_SPACES_IN_BETWEEN = Pattern.compile(
        "([\\p{Lu}][\\s]+)+[\\p{Lu}]"
    );
    /**
     * Matches upper case letters in the input.
     */
    private static final Pattern UPPERCASE_LETTER = Pattern.compile("(\\p{Lu})");
    /**
     * Matches sequences of letters, dashes, digits and soft hyphens.
     */
    private static final Pattern WORDS = Pattern.compile("[\\p{L}[\\-\\d\u00ad]]+");
    /**
     * Matches an input containing two or more upper case letters (optionally with soft hyphens in between) and
     * nothing else.
     */
    private static final Pattern UPPERCASE_LETTER_SEQUENCE_INPUT = Pattern.compile("\\A(\\p{Lu}(\u00ad)?){2,}\\z");
    /**
     * Matches one or more upper case letters (optionally followed by a soft hyphen) at the beginning of input or
     * following a dash.
     */
    private static final Pattern UPPERCASE_LETTER_SEQUENCE_START = Pattern.compile("(\\A|(?<=\\-))(\\p{Lu}(\u00ad)?)+");

    @Override
    public String filter(String str) {
        return addCapitalizationMarkers(str);
    }

    private String addCapitalizationMarkers(String input) {
        StringBuffer ret = new StringBuffer();
        // Match an upper case sequence with whitespace, '-' or '/' in between,
        // if preceded by beginning of input or any non letter character and
        // followed by end of input or any non letter character except a digit or 0x283c
        //^\\-^/^\\d
        for (SplitResult sr : StringSplitter.split(input, UPPERCASE_LETTER_SEQUENCE_SPACE_DASH_SLASH)) {
            String s = sr.getText();
            if (sr.isMatch()) {
                if (UPPERCASE_LETTER_SEQUENCE_WITH_SPACES_IN_BETWEEN.matcher(s).matches()) {
                    // String is a group of single capital letters, e.g: 'E X A M P L E'
                    ret.append(UPPERCASE_LETTER.matcher(s).replaceAll(CHAR_MARKER + "$1"));
                } else {
                    // String is a group of capitalized words.
                    String asGroup = markAsGroup(s);
                    String asWords = markAsWords(s);
                    // compare to see which is shorter
                    if (asGroup.length() <= asWords.length()) {
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
        return SEQ_PREFIX_MARKER + s + SEQ_POSTFIX_MARKER;
    }

    private String markAsWords(String s) {
        StringBuffer ret = new StringBuffer();
        // Split on words, or word like character groups (such as passwords)
        for (SplitResult tr : StringSplitter.split(s, WORDS)) {
            String t = tr.getText();
            if (tr.isMatch()) {
                // String is a word, e.g. 'hello', 'pqr6XWr', 'ISBN-centralen'
                if (UPPERCASE_LETTER_SEQUENCE_INPUT.matcher(t).matches()) {
                    // String is a single capitalized word longer than one letter, e.g. 'OK'
                    ret.append(WORD_MARKER + t);
                } else {
                    // String contains non upper case letters or other characters
                    for (SplitResult ur : StringSplitter.split(t, UPPERCASE_LETTER_SEQUENCE_START)) {
                        String u = ur.getText();
                        if (ur.isMatch() && u.length() > 2) {
                            // Input begins with upper case letters
                            ret.append(WORD_PART_PREFIX);
                            ret.append(u);
                            ret.append(WORD_PART_POSTFIX);
                        } else {
                            // Use a single upper case mark for all upper case letters
                            ret.append(UPPERCASE_LETTER.matcher(u).replaceAll(CHAR_MARKER + "$1"));
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
