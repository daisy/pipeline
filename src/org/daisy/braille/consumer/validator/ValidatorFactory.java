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
package org.daisy.braille.consumer.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.spi.ServiceRegistry;

import org.daisy.braille.api.factory.FactoryFilter;
import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.validator.Validator;
import org.daisy.braille.api.validator.ValidatorFactoryService;
import org.daisy.braille.api.validator.ValidatorProvider;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * Simple factory for instantiating a Validator based on its identifier 
 * @author Joel Håkansson
 */
@Component
public class ValidatorFactory implements ValidatorFactoryService {
	private final List<ValidatorProvider> providers;
	private final Map<String, ValidatorProvider> map;
	
	public ValidatorFactory() {
		providers = new CopyOnWriteArrayList<ValidatorProvider>();
		map = Collections.synchronizedMap(new HashMap<String, ValidatorProvider>());
	}

	/**
	 * <p>
	 * Creates a new ValidatorFactory and populates it using the SPI
	 * (java service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the ValidatorFactoryService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new ValidatorFactory
	 */
	public static ValidatorFactory newInstance() {
		ValidatorFactory ret = new ValidatorFactory();
		Iterator<ValidatorProvider> i = ServiceRegistry.lookupProviders(ValidatorProvider.class);
		while (i.hasNext()) {
			ret.addFactory(i.next());
		}
		return ret;
	}
	
	@Reference(type = '*')
	public void addFactory(ValidatorProvider factory) {
		providers.add(factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(ValidatorProvider factory) {
		// this is to avoid adding items to the cache that were removed while
		// iterating
		synchronized (map) {
			providers.remove(factory);
			map.clear();
		}
	}

	/**
	 * Obtains a new instance of a Validator with the given identifier
	 * @param identifier a string that identifies the desired implementation
	 * @return returns a Validator for the given identifier, or null if none is found
	 */
	public Validator newValidator(String identifier) {
		if (identifier==null) {
			return null;
		}
		ValidatorProvider template = map.get(identifier);
		if (template==null) {
			// this is to avoid adding items to the cache that were removed
			// while iterating
			synchronized (map) {
				for (ValidatorProvider p : providers) {
					for (FactoryProperties fp : p.list()) {
						if (fp.getIdentifier().equals(identifier)) {
							map.put(fp.getIdentifier(), p);
							template = p;
							break;
						}
					}
				}
			}
		}
		if (template!=null) {
			return template.newValidator(identifier);
		} else {
			return null;
		}
	}

	@Override
	public Collection<FactoryProperties> list() {
		Collection<FactoryProperties> ret = new ArrayList<FactoryProperties>();
		for (ValidatorProvider p : providers) {
			ret.addAll(p.list());
		}
		return ret;
	}

	@Override
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
