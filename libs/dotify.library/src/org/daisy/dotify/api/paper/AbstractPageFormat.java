package org.daisy.dotify.api.paper;

/**
 * Provides an abstract page format.
 *
 * @author Joel Håkansson
 */
public abstract class AbstractPageFormat implements PageFormat {

    @Override
    public SheetPaperFormat asSheetPaperFormat() {
        throw new ClassCastException();
    }

    @Override
    public TractorPaperFormat asTractorPaperFormat() {
        throw new ClassCastException();
    }

    @Override
    public RollPaperFormat asRollPaperFormat() {
        throw new ClassCastException();
    }

}
