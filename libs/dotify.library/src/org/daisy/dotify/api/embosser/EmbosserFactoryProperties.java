package org.daisy.dotify.api.embosser;

import org.daisy.dotify.api.factory.FactoryProperties;

/**
 * Extends {@link FactoryProperties} with embosser specific information.
 *
 * @author Joel Håkansson
 */
public interface EmbosserFactoryProperties extends FactoryProperties {

    /**
     * Gets the make for this embosser.
     *
     * @return returns the make
     */
    public String getMake();

    /**
     * Gets the model for this embosser.
     *
     * @return returns the model
     */
    public String getModel();

}
