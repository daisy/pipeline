package org.daisy.dotify.api.paper;

import org.daisy.dotify.api.factory.FactoryProperties;

/**
 * Provides an interface for a physical paper media. Implementations
 * must be immutable.
 *
 * @author Joel Håkansson
 */
public interface Paper extends FactoryProperties {
    /**
     * Defines paper types.
     */
    public enum Type {
        /**
         * Defines a cut-sheet paper. Implementations must extend SheetPaper.
         */
        SHEET,
        /**
         * Defines a tractor paper. Implementations must extend TractorPaper.
         */
        TRACTOR,
        /**
         * Defines a roll paper. Implementations must extend RollPaper.
         */
        ROLL
    }

    ;

    /**
     * Gets width of the paper, in mm. The width is defined as the length of the page perpendicular to
     * the paper feed direction.
     * @return returns width in mm.
     *//*
    @Override
    public double getWidth();*/

    /**
     * Gets height of the paper, in mm. The height is defined as the length of the page along
     * the paper feed direction.
     * @return returns height in mm.
     *//*
    @Override
    public double getHeight();*/

    /**
     * Gets the type of paper.
     *
     * @return returns the type of paper
     */
    public Type getType();

    /**
     * Returns this Paper as a SheetPaper.
     *
     * @return returns the SheetPaper
     * @throws ClassCastException if the instance is not SheetPaper
     */
    public SheetPaper asSheetPaper();

    /**
     * Returns this Paper as a TractorPaper.
     *
     * @return returns the TractorPaper
     * @throws ClassCastException if the instance is not TractorPaper
     */
    public TractorPaper asTractorPaper();

    /**
     * Returns this Paper as a RollPaper.
     *
     * @return returns the RollPaper
     * @throws ClassCastException if the instance is not RollPaper
     */
    public RollPaper asRollPaper();

}
