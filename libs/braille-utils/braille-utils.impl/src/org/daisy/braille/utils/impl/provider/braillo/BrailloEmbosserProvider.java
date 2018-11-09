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
package org.daisy.braille.utils.impl.provider.braillo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.dotify.api.embosser.Embosser;
import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.embosser.EmbosserProvider;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.dotify.api.table.TableCatalogService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component
public class BrailloEmbosserProvider implements EmbosserProvider {

	public enum EmbosserType implements EmbosserFactoryProperties {

		BRAILLO_200("200", "Firmware 000.17 or later. Embosser table must match hardware setup."),
		BRAILLO_200_FW_11("200/270/400 FW 1-11", "Firmware 11 and older. Embosser must be set to interpoint embossing. Table must match hardware setup."),
		BRAILLO_270("200/270/400 FW 12-16", "Firmware 12 to 16. Embosser table must match hardware setup."),
		BRAILLO_300("300", "Embosser table must match hardware setup."),
		BRAILLO_400_S("400S", "Firmware 000.17 or later. Embosser table must match hardware setup."), 
		BRAILLO_400_SR("400SR", "Firmware 000.17 or later. Embosser table must match hardware setup."),
		BRAILLO_440_SW("440SW", "Embosser table must match hardware setup."),
		BRAILLO_440_SWSF("440SWSF", "Embosser table must match hardware setup."),
		BRAILLO_600("600", "Embosser table must match hardware setup. Based on Braillo 400S."),
		BRAILLO_600_SR("600SR", "Embosser table must match hardware setup. Based on Braillo 400S."),
		BRAILLO_650_SF("650SF", "Embosser table must match hardware setup. Based on Braillo SW."),
		BRAILLO_650_SW("650SW", "Embosser table must match hardware setup. Based on Braillo SW.");

		private static final String MAKE = "Braillo";
		private final String name;
		private final String model;
		private final String desc;
		private final String identifier;

		EmbosserType(String model, String desc) {
			this.name = MAKE + " " + model;
			// Make is included in the model name here
			this.model = name;
			this.desc = desc;
			this.identifier = "com_braillo.BrailloEmbosserProvider.EmbosserType." + this.toString();
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

		@Override
		public String getMake() {
			return MAKE;
		}

		@Override
		public String getModel() {
			return model;
		}
	};

	private final Map<String, EmbosserFactoryProperties> embossers;
	private TableCatalogService tableCatalogService = null;

	public BrailloEmbosserProvider() {
		embossers = new HashMap<>();
		addEmbosser(EmbosserType.BRAILLO_200);
		addEmbosser(EmbosserType.BRAILLO_200_FW_11);
		addEmbosser(EmbosserType.BRAILLO_270);
		addEmbosser(EmbosserType.BRAILLO_300);
		addEmbosser(EmbosserType.BRAILLO_400_S);
		addEmbosser(EmbosserType.BRAILLO_400_SR);
		addEmbosser(EmbosserType.BRAILLO_440_SW);
		addEmbosser(EmbosserType.BRAILLO_440_SWSF);
		addEmbosser(EmbosserType.BRAILLO_600);
		addEmbosser(EmbosserType.BRAILLO_600_SR);
		addEmbosser(EmbosserType.BRAILLO_650_SF);
		addEmbosser(EmbosserType.BRAILLO_650_SW);
	}

	private void addEmbosser(EmbosserFactoryProperties e) {
		embossers.put(e.getIdentifier(), e);
	}

	@Override
	public Embosser newFactory(String identifier) {
		FactoryProperties fp = embossers.get(identifier);
		switch ((EmbosserType)fp) {
		case BRAILLO_200:
			return new Braillo200Embosser(tableCatalogService, EmbosserType.BRAILLO_200);
		case BRAILLO_200_FW_11:
			return new Braillo200_270_400_v1_11Embosser(tableCatalogService, EmbosserType.BRAILLO_200_FW_11);
		case BRAILLO_270:
			return new Braillo200_270_400_v12_16Embosser(tableCatalogService, EmbosserType.BRAILLO_270);
		case BRAILLO_300:
			return new Braillo300Embosser(tableCatalogService, EmbosserType.BRAILLO_300);
		case BRAILLO_400_S:
			return new Braillo400SEmbosser(tableCatalogService, EmbosserType.BRAILLO_400_S);
		case BRAILLO_400_SR:
			return new Braillo400SREmbosser(tableCatalogService, EmbosserType.BRAILLO_400_SR);
		case BRAILLO_440_SW:
			return new Braillo440SWEmbosser(tableCatalogService, EmbosserType.BRAILLO_440_SW);
		case BRAILLO_440_SWSF:
			return new Braillo440SFEmbosser(tableCatalogService, EmbosserType.BRAILLO_440_SWSF);
		case BRAILLO_600:
			return new Braillo600Embosser(tableCatalogService, EmbosserType.BRAILLO_600);
		case BRAILLO_600_SR:
			return new Braillo600SREmbosser(tableCatalogService, EmbosserType.BRAILLO_600_SR);
		case BRAILLO_650_SF:
			return new Braillo650SFEmbosser(tableCatalogService, EmbosserType.BRAILLO_650_SF);
		case BRAILLO_650_SW:
			return new Braillo650SWEmbosser(tableCatalogService, EmbosserType.BRAILLO_650_SW);
		default:
			return null;
		}
	}

	@Override
	public Collection<EmbosserFactoryProperties> list() {
		return Collections.unmodifiableCollection(embossers.values());
	}

	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setTableCatalog(TableCatalogService service) {
		this.tableCatalogService = service;
	}

	public void unsetTableCatalog(TableCatalogService service) {
		this.tableCatalogService = null;
	}

	@Override
	public void setCreatedWithSPI() {
		if (tableCatalogService==null) {
			tableCatalogService = TableCatalog.newInstance();
		}
	}
}