package org.daisy.dotify.api.paper;


/**
 * Provides an interface for filtering a collection of Papers.
 *
 * @author Joel Håkansson
 */
public interface PaperFilter {

    /**
     * Tests if a specified object should be included in a list.
     *
     * @param object the Object to test
     * @return returns true if the specified object should be included in a list, false otherwise
     */
    public boolean accept(Paper object);

}
