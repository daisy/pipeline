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
package com_braillo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.embosser.AbstractEmbosser;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;


public class BrailloEmbosserProvider implements EmbosserProvider {
	public static enum EmbosserType {
		BRAILLO_200,
		BRAILLO_200_FW_11,
		BRAILLO_270,
		BRAILLO_400_S, 
		BRAILLO_400_SR,
		BRAILLO_440_SW,
		BRAILLO_440_SWSF
	};
	
	private final Map<String, Embosser> embossers;
	
	public BrailloEmbosserProvider() {
		embossers = new HashMap<String, Embosser>();
		addEmbosser(new Braillo200Embosser("Braillo 200", "Firmware 000.17 or later. Embosser table must match hardware setup."));
		addEmbosser(new Braillo200_270_400_v1_11Embosser("Braillo 200/270/400 FW 1-11", "Firmware 11 and older. Embosser must be set to interpoint embossing. Table must match hardware setup."));
		addEmbosser(new Braillo200_270_400_v12_16Embosser("Braillo 200/270/400 FW 12-16", "Firmware 12 to 16. Embosser table must match hardware setup."));
		addEmbosser(new Braillo400SEmbosser("Braillo 400S", "Firmware 000.17 or later. Embosser table must match hardware setup."));
		addEmbosser(new Braillo400SREmbosser("Braillo 400SR", "Firmware 000.17 or later. Embosser table must match hardware setup."));
		addEmbosser(new Braillo440SWEmbosser("Braillo 440SW", "Embosser table must match hardware setup."));
		addEmbosser(new Braillo440SFEmbosser("Braillo 440SWSF", "Embosser table must match hardware setup."));
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
