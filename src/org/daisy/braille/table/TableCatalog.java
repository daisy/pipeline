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

import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;

import org.daisy.factory.FactoryCatalog;

/**
 * Provides a catalog of Table factories.
 * @author Joel Håkansson
 */
public abstract class TableCatalog implements FactoryCatalog<Table> {
	
	protected TableCatalog() {	}
	
	/**
	 * Obtains a new TableCatalog instance. If at least one implementation can be found 
	 * using the Services API, then the first one will be returned. Otherwise the default TableCatalog
	 * will be used.
	 * 
	 * The default TableCatalog will use the Services API to
	 * find TableProviders. The combined result from all TableProviders are available to
	 * the catalog.
	 * @return returns a new TableCatalog instance. 
	 */
	public static TableCatalog newInstance() {
		Iterator<TableCatalog> i = ServiceRegistry.lookupProviders(TableCatalog.class);
		while (i.hasNext()) {
			return i.next();
		}
		return new DefaultTableCatalog();
	}

}
