package org.daisy.braille.table;

import java.util.Collection;

import org.daisy.braille.api.factory.FactoryFilter;
import org.daisy.braille.api.factory.FactoryProperties;


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
	
	public Table newTable(String identifier);
	
	public Collection<FactoryProperties> list();
	
	public Collection<FactoryProperties> list(FactoryFilter filter);

}
