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
package org.daisy.validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;

/**
 * Simple factory for instantiating a Validator based on its identifier 
 * @author Joel Håkansson
 */
//@Component
public class ValidatorFactory {
	private final Map<String, Validator> map;
	
	public ValidatorFactory() {
		map = Collections.synchronizedMap(new HashMap<String, Validator>());
	}

	/**
	 * Obtains a new instance of a ValidatorFactory.
	 *  
	 * @return returns a new ValidatorFactory instance. 
	 */
	public static ValidatorFactory newInstance() {
		ValidatorFactory ret = new ValidatorFactory();
		Iterator<Validator> i = ServiceRegistry.lookupProviders(Validator.class);
		while (i.hasNext()) {
			ret.addFactory(i.next());
		}
		return ret;
	}
	
	//@Reference(type = '*')
	public void addFactory(Validator factory) {
		map.put(factory.getIdentifier(), factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(Validator factory) {
		map.remove(factory.getIdentifier());
	}

	/**
	 * Obtains a new instance of a Validator with the given identifier
	 * @param identifier a string that identifies the desired implementation
	 * @return returns a Validator for the given identifier, or null if none is found
	 */
	public Validator newValidator(String identifier) {
		return map.get(identifier);
	}

}
