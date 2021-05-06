package org.daisy.dotify.api.paper;

import java.io.Serializable;
import java.util.Objects;

/**
 * Provides a default implementation for Paper.
 *
 * @author Joel Håkansson
 */
public abstract class AbstractPaper implements Paper, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6719586492760029428L;
    private final String name;
    private final String desc;
    private final String identifier;

    /**
     * Creates a new paper.
     *
     * @param name       the name of the paper
     * @param desc       the description of the paper
     * @param identifier the identifier
     */
    public AbstractPaper(String name, String desc, String identifier) {
        this.name = name;
        this.desc = desc;
        this.identifier = Objects.requireNonNull(identifier);
    }


    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public SheetPaper asSheetPaper() {
        throw new ClassCastException();
    }

    @Override
    public TractorPaper asTractorPaper() {
        throw new ClassCastException();
    }

    @Override
    public RollPaper asRollPaper() {
        throw new ClassCastException();
    }

}
