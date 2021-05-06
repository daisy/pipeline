package org.daisy.dotify.common.splitter;

/**
 * Provides a specification for a split point.
 *
 * @author Joel Håkansson
 */
public class SplitPointSpecification {
    enum Type {
        ALL,
        NONE,
        EMPTY,
        INDEX;
    }

    private static final SplitPointSpecification INSTANCE_NONE = new SplitPointSpecification(Type.NONE);
    private static final SplitPointSpecification INSTANCE_EMPTY = new SplitPointSpecification(Type.EMPTY);
    private static final SplitPointSpecification INSTANCE_ALL = new SplitPointSpecification(Type.ALL);
    private final int index;
    private final boolean hard;
    private final Type type;
    private final boolean trimTrailing;

    private SplitPointSpecification(Type type) {
        if (type == Type.INDEX) {
            throw new IllegalArgumentException("This constructor doesn't support type:  " + Type.INDEX);
        }
        this.type = type;
        this.hard = false;
        this.index = -1;
        this.trimTrailing = false;
    }

    SplitPointSpecification(int index, boolean hard, boolean trimTrailing) {
        if (index < 0) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        this.type = Type.INDEX;
        this.hard = hard;
        this.index = index;
        this.trimTrailing = trimTrailing;
    }

    static SplitPointSpecification none() {
        return INSTANCE_NONE;
    }

    static SplitPointSpecification empty() {
        return INSTANCE_EMPTY;
    }

    static SplitPointSpecification all() {
        return INSTANCE_ALL;
    }

    Type getType() {
        return type;
    }

    int getIndex() {
        if (type != Type.INDEX) {
            throw new IllegalStateException("This type doesn't support this method.");
        }
        return index;
    }

    boolean isHard() {
        if (type != Type.INDEX) {
            throw new IllegalStateException("This type doesn't support this method.");
        }
        return hard;
    }

    boolean shouldTrimTrailing() {
        return trimTrailing;
    }

}
