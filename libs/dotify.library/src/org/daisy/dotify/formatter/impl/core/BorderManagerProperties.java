package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.translator.TextBorderStyle;

/**
 * TODO: Write java doc.
 */
public interface BorderManagerProperties {

    /**
     * Gets the border.
     *
     * @return the border
     */
    public TextBorderStyle getBorder();

    /**
     * Gets row spacing, in row heights. For example, use 2.0 for double row spacing and 1.0 for normal row spacing.
     *
     * @return returns row spacing
     */
    public float getRowSpacing();

    /**
     * Gets the flow width.
     *
     * @return returns the flow width
     */
    public int getFlowWidth();

    /**
     * Gets the page width.
     * An implementation must ensure that getPageWidth()=getFlowWidth()+getInnerMargin()+getOuterMargin()
     *
     * @return returns the page width
     */
    public int getPageWidth();

}
