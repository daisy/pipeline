/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.imageio.spi.ServiceRegistry;

import org.daisy.factory.FactoryCatalog;
import org.daisy.factory.FactoryFilter;
import org.daisy.factory.FactoryProperties;

/**
 * Provides a catalog of Table factories.
 * @author Joel Håkansson
 */
//TODO: use TableService instead of Table and enable OSGi support
//@Component
public class TableCatalog implements FactoryCatalog<Table, FactoryProperties> {
	private final List<TableProvider> providers;
	private final Map<String, TableProvider> map;
	private final Logger logger;
	
	public TableCatalog() {
		logger = Logger.getLogger(this.getClass().getCanonicalName());
		providers = new CopyOnWriteArrayList<TableProvider>();
		map = Collections.synchronizedMap(new HashMap<String, TableProvider>());
	}
	
	/**
	 * <p>
	 * Creates a new TableCatalog and populates it using the SPI
	 * (java service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the TableCatalogService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new TableCatalog
	 */
	public static TableCatalog newInstance() {
		TableCatalog ret = new TableCatalog();
		Iterator<TableProvider> i = ServiceRegistry.lookupProviders(TableProvider.class);
		while (i.hasNext()) {
			ret.addFactory(i.next());
		}
		return ret;
	}
	
	//@Reference(type = '*')
	public void addFactory(TableProvider factory) {
		logger.finer("Adding factory: " + factory);
		providers.add(factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(TableProvider factory) {
		// this is to avoid adding items to the cache that were removed while
		// iterating
		synchronized (map) {
			providers.remove(factory);
			map.clear();
		}
	}

	public Table get(String identifier) {
		TableProvider template = map.get(identifier);
		if (template==null) {
			// this is to avoid adding items to the cache that were removed
			// while iterating
			synchronized (map) {
				for (TableProvider p : providers) {
					for (FactoryProperties fp : p.list()) {
						if (fp.getIdentifier().equals(identifier)) {
							logger.fine("Found a factory for " + identifier + " (" + p.getClass() + ")");
							map.put(fp.getIdentifier(), p);
							template = p;
							break;
						}						
					}
				}
			}
		}
		if (template==null) {
			throw new IllegalArgumentException("Cannot find a factory for " + identifier);
		}
		return template.newFactory(identifier);
	}
	
	public Collection<FactoryProperties> list() {
		Collection<FactoryProperties> ret = new ArrayList<FactoryProperties>();
		for (TableProvider p : providers) {
			ret.addAll(p.list());
		}
		return ret;
	}
	
	public Collection<FactoryProperties> list(FactoryFilter filter) {
		Collection<FactoryProperties> ret = new ArrayList<FactoryProperties>();
		for (FactoryProperties fp : list()) {
			if (filter.accept(fp)) {
				ret.add(fp);
			}
		}
		return ret;
	}

}
