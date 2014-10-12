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
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;

import org.daisy.factory.FactoryCatalog;
import org.daisy.factory.FactoryFilter;

/**
 * Provides a catalog of Table factories.
 * @author Joel Håkansson
 */
//TODO: use TableService instead of Table and enable OSGi support
//@Component
public class TableCatalog implements FactoryCatalog<Table> {
	private final Map<String, Table> map;
	
	public TableCatalog() {
		map = Collections.synchronizedMap(new HashMap<String, Table>());
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
			TableProvider provider = i.next();
			for (Table table : provider.list()) {
				ret.addFactory(table);
			}
		}
		return ret;
	}
	
	//@Reference(type = '*')
	public void addFactory(Table factory) {
		map.put(factory.getIdentifier(), factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(Table factory) {
		map.remove(factory.getIdentifier());
	}
	
	public Object getFeature(String key) {
		throw new IllegalArgumentException("Unsupported feature: " + key);
	}
	
	public void setFeature(String key, Object value) {
		throw new IllegalArgumentException("Unsupported feature: " + key);	
	}
	
	public Table get(String identifier) {
		return map.get(identifier);
	}
	
	public Collection<Table> list() {
		return map.values();
	}
	
	public Collection<Table> list(FactoryFilter<Table> filter) {
		Collection<Table> ret = new ArrayList<Table>();
		for (Table table : map.values()) {
			if (filter.accept(table)) {
				ret.add(table);
			}
		}
		return ret;
	}

}
