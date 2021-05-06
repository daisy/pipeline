package org.daisy.dotify.formatter.impl.sheet;

/**
 * Provides state needed for a text flow.
 *
 * @author Joel Håkansson
 */
public class PageCounter {
    private int pageOffset;
    private int pageCount;

    public PageCounter() {
        pageOffset = 0;
        pageCount = 0;
    }

    public PageCounter(PageCounter template) {
        this.pageOffset = template.pageOffset;
        this.pageCount = template.pageCount;
    }

    public void setDefaultPageOffset(int value) {
        pageOffset = value;
    }

    /**
     * Page number counter. Represents the current value of the default "<a
     * href="http://braillespecs.github.io/obfl/obfl-specification.html#pagenumbercounter"
     * ><code>page-number-counter</code></a>" (i.e. the value for the page that was produced
     * last).
     *
     * <p>Initially <code>0</code>, or the value specified with <code>initial-page-number</code>
     * minus 1.</p>
     *
     * @return the counter value
     */
    public int getDefaultPageOffset() {
        return pageOffset;
    }

    /**
     * Simple page counter. Represents the number of pages currently produced. This is used for
     * searching and MUST be continuous. Do not use for page numbers.
     *
     * <p>Initially <code>0</code>.</p>
     *
     * @return the counter value
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * Advance to the next page. Increments the value of {@link #getPageCount()} by <code>1</code>.
     */
    public void increasePageCount() {
        pageCount++;
    }

}
