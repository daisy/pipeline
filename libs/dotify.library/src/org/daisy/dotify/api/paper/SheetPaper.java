package org.daisy.dotify.api.paper;


/**
 * Provides a paper object for cut-sheet paper.
 *
 * @author Joel Håkansson
 */
public class SheetPaper extends AbstractPaper {

    /**
     *
     */
    private static final long serialVersionUID = -5937438111444144962L;
    protected final Length pageWidth, pageHeight;

    /**
     * Creates a new cut-sheet paper.
     *
     * @param name       a name
     * @param desc       a description
     * @param identifier an identifier
     * @param pageWidth  the width of the paper in the default orientation
     * @param pageHeight the height of the paper in the default orientation
     */
    public SheetPaper(String name, String desc, String identifier, Length pageWidth, Length pageHeight) {
        super(name, desc, identifier);
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
    }

    @Override
    public Type getType() {
        return Type.SHEET;
    }

    /**
     * Gets the width of the paper in the default orientation.
     *
     * @return returns the width
     */
    public Length getPageWidth() {
        return pageWidth;
    }

    /**
     * Gets the height of the paper in default orientation.
     *
     * @return returns the height
     */
    public Length getPageHeight() {
        return pageHeight;
    }

    @Override
    public SheetPaper asSheetPaper() {
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SheetPaper [pageWidth=" + getPageWidth() + ", pageHeight="
                + getPageHeight() + "]";
    }

}
