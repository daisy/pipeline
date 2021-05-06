package org.daisy.dotify.api.formatter;


/**
 * Provides methods needed to add a content collection to a formatter.
 * Note that adding contents outside of items has no specified
 * meaning and may be ignored by a formatter.
 *
 * @author Joel Håkansson
 */
public interface ContentCollection extends FormatterCore {

    /**
     * Starts a new item with the supplied properties. An identifier
     * must be set in the block properties for the item to be referenced.
     *
     * @param props the properties
     */
    public void startItem(BlockProperties props);

    /**
     * Ends the current item.
     */
    public void endItem();

}
