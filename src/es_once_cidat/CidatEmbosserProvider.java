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
package es_once_cidat;

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

/**
 *
 * @author Bert Frees
 */
@Component
public class CidatEmbosserProvider implements EmbosserProvider {

    public static enum EmbosserType implements FactoryProperties {
        IMPACTO_600("Cidat - Impacto 600", "High-quality, high-speed (600 pages per hour) double-sided embosser"),
        IMPACTO_TEXTO("Cidat - Impacto Texto", "High-quality, high-speed (800 pages per hour) double-sided embosser"),
        PORTATHIEL_BLUE("Cidat - Portathiel Blue", "Small, lightweight, portable double-sided embosser");
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

    public CidatEmbosserProvider() {
        embossers = new HashMap<String, FactoryProperties>();
        addEmbosser(EmbosserType.IMPACTO_600);
        addEmbosser(EmbosserType.IMPACTO_TEXTO);
        addEmbosser(EmbosserType.PORTATHIEL_BLUE);
    }

	private void addEmbosser(FactoryProperties e) {
		embossers.put(e.getIdentifier(), e);
	}

	public Embosser newFactory(String identifier) {
		FactoryProperties fp = embossers.get(identifier);
		switch ((EmbosserType)fp) {
		case IMPACTO_600:
			return new ImpactoEmbosser(tableCatalogService, EmbosserType.IMPACTO_600);
		case IMPACTO_TEXTO:
			return new ImpactoEmbosser(tableCatalogService, EmbosserType.IMPACTO_TEXTO);
		case PORTATHIEL_BLUE:
			return new PortathielBlueEmbosser(tableCatalogService, EmbosserType.PORTATHIEL_BLUE);
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
