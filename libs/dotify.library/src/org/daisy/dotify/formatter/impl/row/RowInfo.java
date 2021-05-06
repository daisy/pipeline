package org.daisy.dotify.formatter.impl.row;

import org.daisy.dotify.common.text.StringTools;

class RowInfo {

    private final String leftIndent;
    private final int available;

    /**
     * @param leftIndent The left text indent, possibly with list label inside
     * @param available  The total space available on a row for left margin, content and left and right text indent.
     */
    RowInfo(String leftIndent, int available) {
        this.leftIndent = leftIndent;
        this.available = available;
    }

    /**
     * @param row a row
     * @return the space available on the row for content and right text indent.
     */
    int getMaxLength(RowImpl.Builder row) {
        int maxLenText = available - row.getLeftMargin().getContent().length() - StringTools.length(leftIndent);
        if (maxLenText < 1) {
            throw new RuntimeException("Cannot continue layout: No space left for characters.");
        }
        return maxLenText;
    }

    /**
     * @param row a row
     * @return the column after the last character in the row (including left margin and left text indent)
     */
    int getPreTabPosition(RowImpl.Builder row) {
        return
            row.getLeftMargin().getContent().length() +
            StringTools.length(leftIndent) +
            StringTools.length(row.getText());
    }

    public String getLeftIndent() {
        return leftIndent;
    }
}
