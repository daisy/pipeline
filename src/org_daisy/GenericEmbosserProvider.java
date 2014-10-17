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
package org_daisy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.embosser.AbstractEmbosser;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;

public class GenericEmbosserProvider implements EmbosserProvider {
	public static enum EmbosserType {
		NONE
	};
	
	private final Map<String, Embosser> embossers;
	
	public GenericEmbosserProvider() {
		embossers = new HashMap<String, Embosser>();
		addEmbosser(new GenericEmbosser("Unspecified", "Limited support for unknown embossers", EmbosserType.NONE));
	}

	private void addEmbosser(AbstractEmbosser e) {
		embossers.put(e.getIdentifier(), e);
	}

	public Embosser newFactory(String identifier) {
		return embossers.get(identifier);
	}

	//jvm1.6@Override
	public Collection<Embosser> list() {
		return embossers.values();
	}

}
 
