package org.daisy.dotify.api.embosser;

import java.util.Collection;


/**
 * <p>
 * Provides an interface for an EmbosserCatalog service. The purpose of
 * this interface is to expose an implementation of an EmbosserCatalog
 * as an OSGi service.
 * </p>
 *
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 *
 * @author Joel Håkansson
 */
public interface EmbosserCatalogService {

    /**
     * Creates a new embosser with the specified identifier.
     *
     * @param identifier the identifier
     * @return returns a new embosser, or null if not found
     */
    public Embosser newEmbosser(String identifier);

    /**
     * Lists embossers.
     *
     * @return returns a list of embossers
     */
    public Collection<EmbosserFactoryProperties> listEmbossers();

    /**
     * Lists embossers matching the specified filter.
     *
     * @param filter the filter
     * @return returns a list of embossers
     */
    public Collection<EmbosserFactoryProperties> listEmbossers(EmbosserFilter filter);

}
