package org.daisy.dotify.api.paper;

/**
 * Provides two dimensional measurements in millimeters.
 *
 * @author Joel Håkansson
 */
public interface Dimensions {

    /**
     * Gets width, in mm.
     *
     * @return returns width in mm.
     */
    public double getWidth();

    /**
     * Gets height, in mm.
     *
     * @return returns height in mm.
     */
    public double getHeight();
}
