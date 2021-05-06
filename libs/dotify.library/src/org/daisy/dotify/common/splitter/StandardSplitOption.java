package org.daisy.dotify.common.splitter;

/**
 * Defines standard split options.
 *
 * @author Joel Håkansson
 */
public enum StandardSplitOption implements SplitOption {

    /**
     * Allow force to be used.
     */
    ALLOW_FORCE,
    /**
     * Retain trailing skippable units.
     */
    RETAIN_TRAILING,
    /**
     * Treats the last unit as a regular unit.
     */
    NO_LAST_UNIT_SIZE;
}
