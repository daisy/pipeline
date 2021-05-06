package org.daisy.dotify.formatter.impl.obfl;

import org.daisy.dotify.api.formatter.Context;

/**
 * <p>Variables that can be used in an OBFL expression.</p>
 *
 * <p>Within a {@link Context} certain variables are available, such as current
 * volume number and current page number. Here the different variables that
 * exist are listed.</p>
 *
 * <p>Some variables exclude each other. For instance,
 * <code>STARTED_PAGE_NUMBER</code> and
 * <code>STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER</code> are both "<i>meta</i>"
 * page numbers and at most one such "<i>meta</i>" page number is available in
 * any particular context within the document.</p>
 *
 * @author Paul Rambags
 */
public enum OBFLVariable {
    /**
     * <p>The current page number.</p>
     */
    PAGE_NUMBER,
    /**
     * <p>The current volume number.</p>
     */
    VOLUME_NUMBER,
    /**
     * <p>The total number of volumes in the document.</p>
     */
    VOLUME_COUNT,
    /**
     * <p>The volume number of the context described in the current
     * context. This is a meta volume number.</p>
     *
     * <p>In the context of a OBFL <code>toc-entry</code> element this is the
     * volume number the <code>toc-entry</code> refers to.</p>
     */
    STARTED_VOLUME_NUMBER,
    /**
     * <p>The page number of the context described in the current context. This
     * is a meta page number.</p>
     *
     * <p>In the context of a OBFL <code>toc-entry</code> element this is the
     * page number the <code>toc-entry</code> refers to.</p>
     */
    STARTED_PAGE_NUMBER,
    /**
     * <p>The page number of the first content page of the context described in
     * in the current context. This is a meta page number.</p>
     *
     * <p>In the context of a OBFL <code>toc-entry-on-resumed</code> element
     * this is the page number of the first content page after the volume break
     * that corresponds with this entry.</p>
     */
    STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER,
    /**
     * <p>Indicates that no content has been printed on this page yet.</p>
     */
    STARTS_AT_TOP_OF_PAGE,
    /**
     * <p>The total number of sheets in the document.</p>
     */
    SHEET_COUNT,
    /**
     * <p>The number of sheets in the current volume.</p>
     */
    VOLUME_SHEET_COUNT;
}
