package org.daisy.dotify.translator;

/**
 * Provides an interface for finalizing a braille translation. The purpose is to
 * replace remaining non braille characters that are useful for breaking the
 * translated string into lines. Any remaining non braille characters must have
 * a single braille cell translation, in other words, the total length of the
 * string cannot change.
 *
 * @author Joel Håkansson
 */
@FunctionalInterface
public interface BrailleFinalizer {

    /**
     * Finalizes braille translation, replacing remaining non braille characters
     * with braille characters. An implementation can assume that the input has been
     * filtered. The resulting string must have the same length as the input string.
     *
     * @param input the input string, mostly braille
     * @return returns the finalized string
     */
    public String finalizeBraille(String input);

}
