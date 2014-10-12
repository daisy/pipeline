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
package org.daisy.braille.embosser;

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
 * Provides a catalog of Embosser factories.
 * @author Joel Håkansson
 */
//TODO: use EmbosserService instead of Embosser and enable OSGi support
//@Component
public class EmbosserCatalog implements FactoryCatalog<Embosser> {
	private final Map<String, Embosser> map;
	
	public EmbosserCatalog() {
		map = Collections.synchronizedMap(new HashMap<String, Embosser>());
	}
	
	/**
	 * <p>
	 * Creates a new EmbosserCatalog and populates it using the SPI
	 * (java service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the EmbosserCatalogService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new EmbosserCatalog
	 */
	public static EmbosserCatalog newInstance() {
		EmbosserCatalog ret = new EmbosserCatalog();
		Iterator<EmbosserProvider> i = ServiceRegistry.lookupProviders(EmbosserProvider.class);
		while (i.hasNext()) {
			EmbosserProvider provider = i.next();
			for (Embosser embosser : provider.list()) {
				ret.addFactory(embosser);
			}
		}
		return ret;
	}
	
	//@Reference(type = '*')
	public void addFactory(Embosser factory) {
		map.put(factory.getIdentifier(), factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(Embosser factory) {
		map.remove(factory.getIdentifier());
	}
	
	public Object getFeature(String key) {
		throw new IllegalArgumentException("Unsupported feature: " + key);	}
	
	public void setFeature(String key, Object value) {
		throw new IllegalArgumentException("Unsupported feature: " + key);	}
	
	public Embosser get(String identifier) {
		return map.get(identifier);
	}
	
	public Collection<Embosser> list() {
		return map.values();
	}
	
	public Collection<Embosser> list(FactoryFilter<Embosser> filter) {
		Collection<Embosser> ret = new ArrayList<Embosser>();
		for (Embosser embosser : map.values()) {
			if (filter.accept(embosser)) {
				ret.add(embosser);
			}
		}
		return ret;
	}
}
