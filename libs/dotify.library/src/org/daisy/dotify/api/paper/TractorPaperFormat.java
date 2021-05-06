package org.daisy.dotify.api.paper;


/**
 * Provides a paper format for tractor paper.
 *
 * @author Joel Håkansson
 */
public class TractorPaperFormat extends AbstractPageFormat {
    private final Length across, along;

    /**
     * Creates a new tractor paper format.
     *
     * @param paper the paper to use
     */
    public TractorPaperFormat(TractorPaper paper) {
        this.across = paper.getLengthAcrossFeed();
        this.along = paper.getLengthAlongFeed();
    }

    /**
     * Creates a new tractor paper format.
     *
     * @param acrossPaperFeed the width of the paper
     * @param alongPaperFeed  the height of the paper
     */
    public TractorPaperFormat(Length acrossPaperFeed, Length alongPaperFeed) {
        this.across = acrossPaperFeed;
        this.along = alongPaperFeed;
    }

    /**
     * Gets the length of the paper perpendicular to the direction of the paper feed.
     *
     * @return returns the length.
     */
    public Length getLengthAcrossFeed() {
        return across;
    }

    /**
     * Gets the length of the paper along the direction of the paper feed.
     *
     * @return returns the length.
     */
    public Length getLengthAlongFeed() {
        return along;
    }

    @Override
    public Type getPageFormatType() {
        return Type.TRACTOR;
    }

    @Override
    public TractorPaperFormat asTractorPaperFormat() {
        return this;
    }

    @Override
    public String toString() {
        return "TractorPaperFormat [across=" + across + ", along=" + along
                + "]";
    }
}
