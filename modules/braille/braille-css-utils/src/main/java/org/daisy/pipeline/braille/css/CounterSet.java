package org.daisy.pipeline.braille.css;

import cz.vutbr.web.css.TermPair;

/**
 * A counter name-value pair. See <a
 * href="http://braillespecs.github.io/braille-css/#h4_manipulating-counters-the-counter-increment-counter-set-and-counter-reset-properties">Manipulating
 * Counters</a> in the braille CSS specification.
 */
public interface CounterSet extends TermPair<String,Integer> {
}
