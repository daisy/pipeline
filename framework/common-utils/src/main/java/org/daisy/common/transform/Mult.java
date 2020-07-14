package org.daisy.common.transform;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Multiples of an input value.
 */
public interface Mult<I extends InputValue<?>> extends Supplier<I> {

	/**
	 * @throws NoSuchElementException if the maximum number of multiples has been reached.
	 */
	public I get() throws NoSuchElementException;

}
