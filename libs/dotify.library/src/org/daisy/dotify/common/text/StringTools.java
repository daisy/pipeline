package org.daisy.dotify.common.text;

import java.util.Arrays;

/**
 * StringTools is a utility class for simple static operations related
 * to strings.
 *
 * @author Joel Håkansson
 */
public class StringTools {

    // Default constructor is private as this class is not intended to be instantiated.
    private StringTools() {
    }

    /**
     * Count the number of code points in a String. This is equivalent
     * to calling codePointCount on the entire String (beginIndex=0
     * and endIndex=string.length()).
     *
     * @param str the String to count length on
     * @return returns the number of code points in the entire String
     */
    public static int length(String str) {
        return str.codePointCount(0, str.length());
    }

    /**
     * Fill a String with a single character.
     *
     * @param c      the character to fill with
     * @param length the length of the resulting String
     * @return returns a String filled with character c
     */
    public static String fill(char c, int length) {
        if (length < 1) {
            return "";
        }
        char[] ca = new char[length];
        Arrays.fill(ca, c);
        return new String(ca);
    }

    /**
     * Fill a String with copies of another String.
     *
     * @param s      the String to fill with
     * @param length the length of the resulting String
     * @return returns a String filled with String s
     * @throws IllegalArgumentException if the string is empty
     */
    public static String fill(String s, int length) {
        if (length < 1) {
            return "";
        }
        if (s.length() == 0) {
            throw new IllegalArgumentException("Cannot fill using an empty string.");
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(s);
        }
        return sb.substring(0, length).toString();
    }

}
