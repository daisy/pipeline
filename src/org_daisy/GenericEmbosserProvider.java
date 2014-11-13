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
public class GenericEmbosserProvider implements EmbosserProvider {
	public static enum EmbosserType implements FactoryProperties {
		NONE("Unspecified", "Limited support for unknown embossers");
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
	
	public GenericEmbosserProvider() {
		embossers = new HashMap<String, FactoryProperties>();
		addEmbosser(EmbosserType.NONE);
	}

	private void addEmbosser(FactoryProperties e) {
		embossers.put(e.getIdentifier(), e);
	}

	public Embosser newFactory(String identifier) {
		FactoryProperties fp = embossers.get(identifier);
		switch ((EmbosserType)fp) {
		case NONE:
			return new GenericEmbosser(tableCatalogService, EmbosserType.NONE);
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
			tableCatalogService = TableCatalog.newInstance();
		}
	}

}
 
