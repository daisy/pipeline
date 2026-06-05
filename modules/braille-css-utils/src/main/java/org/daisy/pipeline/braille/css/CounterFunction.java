package org.daisy.pipeline.braille.css;

import java.util.Optional;

import cz.vutbr.web.css.Term;

import org.daisy.pipeline.css.CounterStyle;

/**
 * A {@code counter()} function. See <a
 * href="http://braillespecs.github.io/braille-css/#h4_printing-counters-the-counter-function">Printing
 * Counters</a> in the braille CSS specification.
 */
public interface CounterFunction extends Term<CounterFunction> {

	public String getCounter();

	public Optional<CounterStyle> getStyle();

}
