package org.daisy.dotify.api.formatter;

/**
 * Provides a page area builder.
 *
 * @author Joel Håkansson
 */
public interface PageArea {

    /**
     * Gets a formatter core for the area before the page area.
     *
     * @return returns a formatter core
     */
    public FormatterCore getBeforeArea();

    /**
     * Gets a formatter core for the area after the page area.
     *
     * @return returns a formatter core
     */
    public FormatterCore getAfterArea();
}
