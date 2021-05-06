package org.daisy.dotify.api.paper;


/**
 * Provides a paper object for paper in rolls.
 *
 * @author Joel Håkansson
 */
public class RollPaper extends AbstractPaper {

    /**
     *
     */
    private static final long serialVersionUID = 8817783441029734127L;
    private final Length across;

    /**
     * Creates a new roll paper.
     *
     * @param name       a name for the paper
     * @param desc       a description
     * @param identifier an identifier
     * @param across     the height of the roll
     */
    public RollPaper(String name, String desc, String identifier, Length across) {
        super(name, desc, identifier);
        this.across = across;
    }

    /**
     * Gets the length of the paper perpendicular to the direction of the paper feed.
     *
     * @return returns the length, in mm.
     */
    public Length getLengthAcrossFeed() {
        return across;
    }

    @Override
    public Type getType() {
        return Type.ROLL;
    }

    @Override
    public RollPaper asRollPaper() {
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RollPaper [lengthAcrossFeed=" + getLengthAcrossFeed() + "]";
    }
}
