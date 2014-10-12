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
 * Provides a catalog of Paper factories.
 * @author Joel Håkansson
 */
//TODO: use PaperService instead of Paper and enable OSGi support
//@Component
public class PaperCatalog implements FactoryCatalog<Paper> {
	private final Map<String, Paper> map;
	
	public PaperCatalog() {
		map = Collections.synchronizedMap(new HashMap<String, Paper>());
	}
	
	/**
	 * <p>
	 * Creates a new PaperCatalog and populates it using the SPI
	 * (java service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the PaperCatalogService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new PaperCatalogCatalog
	 */
	public static PaperCatalog newInstance() {
		PaperCatalog ret = new PaperCatalog();
		Iterator<PaperProvider> i = ServiceRegistry.lookupProviders(PaperProvider.class);
		while (i.hasNext()) {
			PaperProvider provider = i.next();
			for (Paper paper : provider.list()) {
				ret.addFactory(paper);
			}
		}
		return ret;
	}
	
	//@Reference(type = '*')
	public void addFactory(Paper factory) {
		map.put(factory.getIdentifier(), factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(Paper factory) {
		map.remove(factory.getIdentifier());
	}
	
	//jvm1.6@Override
	public Object getFeature(String key) {
		throw new IllegalArgumentException("Unsupported feature: " + key);
	}

	//jvm1.6@Override
	public void setFeature(String key, Object value) {
		throw new IllegalArgumentException("Unsupported feature: " + key);
	}
	
	//jvm1.6@Override
	public Paper get(String identifier) {
		return map.get(identifier);
	}

	//jvm1.6@Override
	public Collection<Paper> list() {
		return map.values();
	}

	//jvm1.6@Override
	public Collection<Paper> list(FactoryFilter<Paper> filter) {
		Collection<Paper> ret = new ArrayList<Paper>();
		for (Paper paper : map.values()) {
			if (filter.accept(paper)) {
				ret.add(paper);
			}
		}
		return ret;
	}
}
