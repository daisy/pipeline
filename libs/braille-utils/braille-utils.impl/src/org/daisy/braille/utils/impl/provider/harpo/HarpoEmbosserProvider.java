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
package org.daisy.braille.utils.impl.provider.harpo;

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
public class HarpoEmbosserProvider implements EmbosserProvider {

	public enum EmbosserType implements EmbosserFactoryProperties {
		MOUNTBATTEN_LS("LS", ""),
		MOUNTBATTEN_PRO("Pro", ""),
		MOUNTBATTEN_WRITER_PLUS("Writer+", "");
		private static final String MAKE = "Mountbatten";
		private final String name;
		private final String model;
		private final String desc;
		private final String identifier;
		EmbosserType (String model, String desc) {
			this.name = MAKE + " " + model;
			// Make is included in the model name here
			this.model = name;
			this.desc = desc;
			this.identifier = "pl_com_harpo.HarpoEmbosserProvider.EmbosserType." + this.toString();
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
	}

	private final Map<String, EmbosserFactoryProperties> embossers;
	private TableCatalogService tableCatalogService = null;

	public HarpoEmbosserProvider() {
		embossers = new HashMap<>();
		addEmbosser(EmbosserType.MOUNTBATTEN_LS);
		addEmbosser(EmbosserType.MOUNTBATTEN_PRO);
		addEmbosser(EmbosserType.MOUNTBATTEN_WRITER_PLUS);
	}

	private void addEmbosser(EmbosserFactoryProperties e) {
		embossers.put(e.getIdentifier(), e);
	}

	@Override
	public Embosser newFactory(String identifier) {
		FactoryProperties fp = embossers.get(identifier);
		switch ((EmbosserType)fp) {
		case MOUNTBATTEN_LS:
			return new MountbattenEmbosser(tableCatalogService, EmbosserType.MOUNTBATTEN_LS);
		case MOUNTBATTEN_PRO:
			return new MountbattenEmbosser(tableCatalogService, EmbosserType.MOUNTBATTEN_PRO);
		case MOUNTBATTEN_WRITER_PLUS:
			return new MountbattenEmbosser(tableCatalogService, EmbosserType.MOUNTBATTEN_WRITER_PLUS);
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
