package org.daisy.dotify.api.paper;


/**
 * PageFormat extends a Paper with options selected by a user (if applicable).
 *
 * @author Joel Håkansson
 */
public interface PageFormat {
    /**
     * Defines page format types.
     */
    public enum Type {
        /**
         * Defines a cut-sheet paper format. Implementation must extend SheetPaperFormat.
         */
        SHEET,
        /**
         * Defines a tractor paper format. Implementation must extend TractorPaperFormat.
         */
        TRACTOR,
        /**
         * Defines a roll paper format. Implementation must extend RollPaperFormat.
         */
        ROLL
    }

    ;

    /**
     * Gets the paper in this paper format.
     *
     * @return returns the type
     */
    public Type getPageFormatType();

    /**
     * Returns this PageFormat as a SheetPaperFormat.
     *
     * @return returns the SheetPaperFormat
     * @throws ClassCastException if the instance is not SheetPaperFormat
     */
    public SheetPaperFormat asSheetPaperFormat();

    /**
     * Returns this PageFormat as a TractorPaperFormat.
     *
     * @return returns the TractorPaperFormat
     * @throws ClassCastException if the instance is not TractorPaperFormat
     */
    public TractorPaperFormat asTractorPaperFormat();

    /**
     * Returns this PageFormat as a RollPaperFormat.
     *
     * @return returns the RollPaperFormat
     * @throws ClassCastException if the instance is not RollPaperFormat
     */
    public RollPaperFormat asRollPaperFormat();

}
