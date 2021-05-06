package org.daisy.dotify.api.paper;


/**
 * Provides a paper object for perforated paper with paper guides.
 *
 * @author Joel Håkansson
 */
public class TractorPaper extends AbstractPaper {

    /**
     *
     */
    private static final long serialVersionUID = 4768747877387932290L;
    private final Length across, along;

    /**
     * Creates a new tractor paper.
     *
     * @param name       a name
     * @param desc       a description
     * @param identifier an identifier
     * @param across     the width of the paper
     * @param along      the height of the paper
     */
    public TractorPaper(String name, String desc, String identifier, Length across, Length along) {
        super(name, desc, identifier);
        this.across = across;
        this.along = along;
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
    public Type getType() {
        return Type.TRACTOR;
    }

    @Override
    public TractorPaper asTractorPaper() {
        return this;
    }

    @Override
    public String toString() {
        return "TractorPaper [lengthAcrossFeed=" + getLengthAcrossFeed() +
                ", lengthAlongFeed=" + getLengthAlongFeed() + "]";
    }
}
