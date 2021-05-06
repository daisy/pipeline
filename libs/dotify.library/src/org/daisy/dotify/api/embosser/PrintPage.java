package org.daisy.dotify.api.embosser;

import org.daisy.dotify.api.embosser.EmbosserProperties.PrintMode;
import org.daisy.dotify.api.paper.Dimensions;
import org.daisy.dotify.api.paper.Length;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.RollPaperFormat;
import org.daisy.dotify.api.paper.SheetPaperFormat;
import org.daisy.dotify.api.paper.TractorPaperFormat;

/**
 * @author Bert Frees
 * @author Joel Håkansson
 */
public class PrintPage implements Dimensions {

    /**
     * Direction of print.
     */
    public enum PrintDirection {
        /**
         * Direction of embosser head is equal to direction of feeding paper.
         */
        UPRIGHT,
        /**
         * Direction of embosser head is opposite to direction of feeding paper.
         */
        SIDEWAYS
    }

    /**
     * The shape of the paper.
     */
    public enum Shape {
        /**
         * Represents portrait shape, that is to say that getWidth()&lt;getHeight().
         */
        PORTRAIT,
        /**
         * Represents landscape shape, that is to say that getWidth&gt;getHeight().
         */
        LANDSCAPE,
        /**
         * Represents square shape, that is to say that getWidth()==getHeight().
         */
        SQUARE
    }

    private final PageFormat inputPage;
    private final PrintDirection direction;
    private final PrintMode mode;

    /**
     * Creates a new print page with the specified parameters.
     *
     * @param inputPage the page format
     * @param direction the print direction
     * @param mode      the print mode
     */
    public PrintPage(PageFormat inputPage, PrintDirection direction, PrintMode mode) {
        this.inputPage = inputPage;
        this.direction = direction;
        this.mode = mode;
    }

    /**
     * Creates a new print page with the specified page format
     * and default print direction and print mode.
     *
     * @param inputPage the page format
     */
    public PrintPage(PageFormat inputPage) {
        this(inputPage, PrintDirection.UPRIGHT, PrintMode.REGULAR);
    }

    /**
     * Gets the length of the paper perpendicular to the direction of the paper feed.
     *
     * @return returns the length.
     */
    public Length getLengthAcrossFeed() {
        switch (inputPage.getPageFormatType()) {
            case SHEET: {
                switch (direction) {
                    case SIDEWAYS:
                        return ((SheetPaperFormat) inputPage).getPageHeight();
                    case UPRIGHT:
                    default:
                        return ((SheetPaperFormat) inputPage).getPageWidth();
                }
            }
            case ROLL:
                return ((RollPaperFormat) inputPage).getLengthAcrossFeed();
            case TRACTOR:
                return ((TractorPaperFormat) inputPage).getLengthAcrossFeed();
            default:
                throw new AssertionError("Coding error");
        }
    }

    /**
     * Gets the length of the paper along the direction of the paper feed.
     *
     * @return returns the length.
     */
    public Length getLengthAlongFeed() {
        switch (inputPage.getPageFormatType()) {
            case SHEET: {
                switch (direction) {
                    case SIDEWAYS:
                        return ((SheetPaperFormat) inputPage).getPageWidth();
                    case UPRIGHT:
                    default:
                        return ((SheetPaperFormat) inputPage).getPageHeight();
                }
            }
            case ROLL:
                return ((RollPaperFormat) inputPage).getLengthAlongFeed();
            case TRACTOR:
                return ((TractorPaperFormat) inputPage).getLengthAlongFeed();
            default:
                throw new AssertionError("Coding error");
        }
    }

    @Override
    public double getWidth() {
        double width;

        switch (direction) {
            case SIDEWAYS:
                width = getLengthAlongFeed().asMillimeter();
                break;
            case UPRIGHT:
            default:
                width = getLengthAcrossFeed().asMillimeter();
        }

        switch (mode) {
            case MAGAZINE:
                return width / 2;
            case REGULAR:
            default:
                return width;
        }
    }

    @Override
    public double getHeight() {
        switch (direction) {
            case SIDEWAYS:
                return getLengthAcrossFeed().asMillimeter();
            case UPRIGHT:
            default:
                return getLengthAlongFeed().asMillimeter();
        }
    }

    /**
     * Gets the shape of the print page.
     *
     * @return returns the shape
     */
    public Shape getShape() {
        if (getWidth() < getHeight()) {
            return Shape.PORTRAIT;
        } else if (getWidth() > getHeight()) {
            return Shape.LANDSCAPE;
        } else {
            return Shape.SQUARE;
        }
    }
}
