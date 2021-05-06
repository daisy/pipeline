package org.daisy.dotify.api.table;

/**
 * Provides common table features.
 *
 * @author Joel Håkansson
 */
public class TableProperties {
    /**
     * Defines that table has a one-to-one mapping between character and braille pattern.
     */
    public static final String IS_ONE_TO_ONE = "is one-to-one";
    /**
     * Defines that the table is meant for screen use, that is to say,
     * does not contain binary type characters.
     */
    public static final String IS_DISPLAY_FORMAT = "is display format";
}
