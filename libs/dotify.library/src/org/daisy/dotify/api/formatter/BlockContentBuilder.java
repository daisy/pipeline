package org.daisy.dotify.api.formatter;

/**
 * Provides methods that supply content to a block.
 *
 * @author Joel Håkansson
 */
public interface BlockContentBuilder {

    /**
     * Insert a marker at the current position in the flow.
     *
     * @param marker the marker to insert
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void insertMarker(Marker marker);

    /**
     * Insert an anchor at the current position in the flow.
     *
     * @param ref anchor name
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void insertAnchor(String ref);

    /**
     * Insert a leader at the current position in the flow.
     *
     * @param leader the leader to insert
     * @param props the TextProperties including the mode to translate the pattern
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void insertLeader(Leader leader, TextProperties props);

    /**
     * Add chars to the current block.
     *
     * @param chars the characters to add to the flow
     * @param props the SpanProperties for the characters
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void addChars(CharSequence chars, TextProperties props);

    /**
     * Starts a style section.
     *
     * @param style the name of the style
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void startStyle(String style);

    /**
     * Ends a previously opened style section.
     *
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void endStyle();

    /**
     * Starts a span section.
     *
     * @param props the span properties
     */
    public void startSpan(SpanProperties props);

    /**
     * Ends a previously opened span section.
     *
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void endSpan();

    /**
     * Explicitly break the current line, even if the line has space
     * left for more characters. The current block remains open.
     *
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void newLine();

    /**
     * Adds the page number of a reference.
     *
     * @param identifier   the element of interest
     * @param numeralStyle the numeral style
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void insertPageReference(String identifier, NumeralStyle numeralStyle);

    /**
     * Inserts a (compound) marker reference.
     *
     * @param ref the reference(s)
     * @param t   the text properties
     */
    public void insertMarkerReference(Iterable<? extends MarkerReference> ref, TextProperties t);

    /**
     * Inserts an expression to evaluate.
     *
     * @param exp the expression
     * @param t   the text properties
     */
    public void insertEvaluate(DynamicContent exp, TextProperties t);


    /**
     * Inserts a reference element that could be used to add information required for
     * external systems.
     *
     * @param reference Reference data.
     */
    public void insertExternalReference(Object reference);
}
