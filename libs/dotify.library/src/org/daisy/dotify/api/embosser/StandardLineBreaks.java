package org.daisy.dotify.api.embosser;


/**
 * Provides standard line break definitions.
 *
 * @author Joel Håkansson
 */
public class StandardLineBreaks implements LineBreaks {
    /**
     * Defines standard line break types.
     */
    public static enum Type {
        /**
         * Indicates windows/dos line breaks.
         */
        DOS,
        /**
         * Indicates unix line breaks.
         */
        UNIX,
        /**
         * Indicates classic mac line breaks.
         */
        MAC,
        /**
         * Indicates system default line breaks.
         */
        DEFAULT
    }

    ;
    private final String newline;

    /**
     * Creates a new object with the system's default line break style.
     */
    public StandardLineBreaks() {
        this(Type.DEFAULT);
    }

    /**
     * Creates a new object with the specified line break style.
     *
     * @param t the type of line break
     */
    public StandardLineBreaks(Type t) {
        newline = getString(t);
    }

    @Override
    public String getString() {
        return newline;
    }

    /**
     * Gets the string used to represent line breaks.
     *
     * @param t the type of line breaks
     * @return returns the string used to represent line breaks
     */
    public static String getString(Type t) {
        switch (t) {
            case UNIX:
                return "\n";
            case DOS:
                return "\r\n";
            case MAC:
                return "\r";
            default:
                return System.getProperty("line.separator", "\r\n");
        }
    }
}
