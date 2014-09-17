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
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;

import org.daisy.factory.FactoryFilter;

class DefaultTableCatalog extends TableCatalog {
	private final HashMap<String, Table> map;
	
	protected DefaultTableCatalog() {
		map = new HashMap<String, Table>();
		Iterator<TableProvider> i = ServiceRegistry.lookupProviders(TableProvider.class);
		while (i.hasNext()) {
			TableProvider provider = i.next();
			for (Table table : provider.list()) {
				map.put(table.getIdentifier(), table);
			}
		}
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
