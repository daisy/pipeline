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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserProvider;
import org.daisy.braille.table.TableCatalog;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.factory.FactoryProperties;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class BrailloEmbosserProvider implements EmbosserProvider {
	public static enum EmbosserType implements FactoryProperties {
		BRAILLO_200("Braillo 200", "Firmware 000.17 or later. Embosser table must match hardware setup."),
		BRAILLO_200_FW_11("Braillo 200/270/400 FW 1-11", "Firmware 11 and older. Embosser must be set to interpoint embossing. Table must match hardware setup."),
		BRAILLO_270("Braillo 200/270/400 FW 12-16", "Firmware 12 to 16. Embosser table must match hardware setup."),
		BRAILLO_400_S("Braillo 400S", "Firmware 000.17 or later. Embosser table must match hardware setup."), 
		BRAILLO_400_SR("Braillo 400SR", "Firmware 000.17 or later. Embosser table must match hardware setup."),
		BRAILLO_440_SW("Braillo 440SW", "Embosser table must match hardware setup."),
		BRAILLO_440_SWSF("Braillo 440SWSF", "Embosser table must match hardware setup.");
		private final String name;
		private final String desc;
		private final String identifier;
    	EmbosserType (String name, String desc) {
			this.name = name;
			this.desc = desc;
			this.identifier = this.getClass().getCanonicalName() + "." + this.toString();
		}
		@Override
		public String getIdentifier() {
			return identifier;
		}
		@Override
		public String getDisplayName() {
			return name;
		}
		@Override
		public String getDescription() {
			return desc;
		}
	};
	
	private final Map<String, FactoryProperties> embossers;
	private TableCatalogService tableCatalogService = null;
	
	public BrailloEmbosserProvider() {
		embossers = new HashMap<String, FactoryProperties>();
		addEmbosser(EmbosserType.BRAILLO_200);
		addEmbosser(EmbosserType.BRAILLO_200_FW_11);
		addEmbosser(EmbosserType.BRAILLO_270);
		addEmbosser(EmbosserType.BRAILLO_400_S);
		addEmbosser(EmbosserType.BRAILLO_400_SR);
		addEmbosser(EmbosserType.BRAILLO_440_SW);
		addEmbosser(EmbosserType.BRAILLO_440_SWSF);
	}
	
	private void addEmbosser(FactoryProperties e) {
		embossers.put(e.getIdentifier(), e);
	}
	
	public Embosser newFactory(String identifier) {
		FactoryProperties fp = embossers.get(identifier);
		switch ((EmbosserType)fp) {
		case BRAILLO_200:
			return new Braillo200Embosser(tableCatalogService, EmbosserType.BRAILLO_200);
		case BRAILLO_200_FW_11:
			return new Braillo200_270_400_v1_11Embosser(tableCatalogService, EmbosserType.BRAILLO_200_FW_11);
		case BRAILLO_270:
			return new Braillo200_270_400_v12_16Embosser(tableCatalogService, EmbosserType.BRAILLO_270);
		case BRAILLO_400_S:
			return new Braillo400SEmbosser(tableCatalogService, EmbosserType.BRAILLO_400_S);
		case BRAILLO_400_SR:
			return new Braillo400SREmbosser(tableCatalogService, EmbosserType.BRAILLO_400_SR);
		case BRAILLO_440_SW:
			return new Braillo440SWEmbosser(tableCatalogService, EmbosserType.BRAILLO_440_SW);
		case BRAILLO_440_SWSF:
			return new Braillo440SFEmbosser(tableCatalogService, EmbosserType.BRAILLO_440_SWSF);
		default:
			return null;
		}
	}

	//jvm1.6@Override
	public Collection<FactoryProperties> list() {
		return Collections.unmodifiableCollection(embossers.values());
	}
	
	@Reference
	public void setTableCatalog(TableCatalogService service) {
		this.tableCatalogService = service;
	}
	
	public void unsetTableCatalog(TableCatalogService service) {
		this.tableCatalogService = null;
	}

	@Override
	public void setCreatedWithSPI() {
		if (tableCatalogService==null) {
			tableCatalogService = TableCatalog.newInstance/*SPI*/();
		}
	}

}
