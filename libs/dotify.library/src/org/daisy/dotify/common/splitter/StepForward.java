package org.daisy.dotify.common.splitter;

/**
 * Provides a package interface for iterating over a list of units.
 *
 * @param <T>
 * @author Joel Håkansson
 */
interface StepForward<T extends SplitPointUnit> {
    /**
     * Performed when a unit should be included.
     *
     * @param unit the unit to include
     */
    void addUnit(T unit);

    /**
     * Checks whether the inclusion of the unit would overflow.
     *
     * @param buffer the unit to test
     * @return returns true, if inclusion overflows, false otherwise
     */
    boolean overflows(T buffer);

    /**
     * Performed when a unit is discarded.
     *
     * @param unit the unit that is discarded
     */
    void addDiscarded(T unit);
}
