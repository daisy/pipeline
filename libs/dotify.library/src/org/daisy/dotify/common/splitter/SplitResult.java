package org.daisy.dotify.common.splitter;

import java.util.List;

/**
 * Provides a split result.
 *
 * @param <T> the type of units
 * @param <U> the type of data source
 * @author Joel Håkansson
 */
public interface SplitResult<T extends SplitPointUnit, U extends SplitPointDataSource<T, U>> {

    /**
     * The head of the result.
     *
     * @return returns the head
     */
    public List<T> head();

    /**
     * The tail of the result.
     *
     * @return returns the tail
     */
    public U tail();
}
