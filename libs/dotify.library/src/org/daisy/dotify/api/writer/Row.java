package org.daisy.dotify.api.writer;

/**
 * Defines a row of braille.
 *
 * @author Joel Håkansson
 */
public interface Row {

    /**
     * Gets the characters.
     *
     * @return returns the characters
     */
    public String getChars();

    /**
     * Gets the row spacing, in rows.
     *
     * @return the row spacing, or null if not set
     */
    public Float getRowSpacing();

    /**
     * Get the external reference element.
     *
     * <p>The returned object should be unchanged.</p>
     *
     * <p>A use-case of this could be to have a external reference tag in the OBFL specification and then
     * use it the writer to add information to the rows of the PEF. This extra reference is not a part
     * of the official PEF specification and used when the output is not meant for print.</p>
     *
     * @return The external reference object, not modified
     */
    public Object getExternalReference();
}
