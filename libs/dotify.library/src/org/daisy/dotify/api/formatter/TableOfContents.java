package org.daisy.dotify.api.formatter;

/**
 * Provides methods needed to add a TOC to a formatter. Note
 * that adding contents outside of entries has no specified
 * meaning and may be ignored by a formatter.
 *
 * @author Joel Håkansson
 */
public interface TableOfContents extends FormatterCore {

    /**
     * Starts a new entry with the supplied properties.
     *
     * @param refId the element that this toc entry is connected to
     */
    public void startEntry(String refId);

    /**
     * Starts a new entry-on-resumed with the supplied properties.
     *
     * @param range the range of this entry on resumed
     */
    public void startEntryOnResumed(TocEntryOnResumedRange range);

    /**
     * Ends the current entry.
     */
    public void endEntry();

}
