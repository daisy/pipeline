package org.daisy.dotify.api.table;

import org.daisy.dotify.api.factory.FactoryProperties;

import java.util.Collection;


/**
 * <p>
 * Provides an interface for a TableCatalog service. The purpose of
 * this interface is to expose an implementation of a TableCatalog
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
public interface TableCatalogService {

    /**
     * Creates a new table with the specified identifier.
     *
     * @param identifier the identifier
     * @return returns a new table
     */
    public Table newTable(String identifier);

    /**
     * Lists the tables.
     *
     * @return returns a collection of table properties
     */
    public Collection<FactoryProperties> list();

    /**
     * Lists the tables that matches the specified filter.
     *
     * @param filter the filter
     * @return returns a collection fo table properties
     */
    public Collection<FactoryProperties> list(TableFilter filter);

}
