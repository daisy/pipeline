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
package org.daisy.paper;

import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;

import org.daisy.factory.FactoryCatalog;

/**
 * Provides a catalog of Paper factories.
 * @author Joel Håkansson
 */
public abstract class PaperCatalog implements FactoryCatalog<Paper> {
	
	protected PaperCatalog() {	}
	
	/**
	 * Obtains a new PaperCatalog instance. If at least one implementation can be found 
	 * using the Services API, then the first one will be returned. Otherwise the default PaperCatalog
	 * will be used.
	 * 
	 * The default PaperCatalog will use the Services API to
	 * find PaperProviders. The combined result from all PaperProviders are available to
	 * the catalog.
	 * @return returns a new PaperCatalog instance. 
	 */
	public static PaperCatalog newInstance() {
		Iterator<PaperCatalog> i = ServiceRegistry.lookupProviders(PaperCatalog.class);
		while (i.hasNext()) {
			return i.next();
		}
		return new DefaultPaperCatalog();
	}
}
