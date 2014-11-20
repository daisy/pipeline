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
package com_brailler;

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
public class EnablingTechnologiesEmbosserProvider implements EmbosserProvider {

    public static enum EmbosserType implements FactoryProperties {
        ROMEO_ATTACHE("Enabling Technologies - Romeo Attache", ""),
        ROMEO_ATTACHE_PRO("Enabling Technologies - Romeo Attache Pro", ""),
        ROMEO_25("Enabling Technologies - Romeo 25", ""),
        ROMEO_PRO_50("Enabling Technologies - Romeo Pro 50", ""),
        ROMEO_PRO_LE_NARROW("Enabling Technologies - Romeo Pro LE Narrow", ""),
        ROMEO_PRO_LE_WIDE("Enabling Technologies - Romeo Pro LE Wide", ""),
        THOMAS("Enabling Technologies - Thomas", ""),
        THOMAS_PRO("Enabling Technologies - Thomas Pro", ""),
        MARATHON("Enabling Technologies - Marathon", ""),
        ET("Enabling Technologies - ET", ""),
        JULIET_PRO("Enabling Technologies - Juliet Pro", ""),
        JULIET_PRO_60("Enabling Technologies - Juliet Pro 60", ""),
        JULIET_CLASSIC("Enabling Technologies - Juliet Classic", ""),
        BOOKMAKER("Enabling Technologies - Bookmaker", ""),
        BRAILLE_EXPRESS_100("Enabling Technologies - Braille Express 100", ""),
        BRAILLE_EXPRESS_150("Enabling Technologies - Braille Express 150", ""),
        BRAILLE_PLACE("Enabling Technologies - BraillePlace", "");
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

    public EnablingTechnologiesEmbosserProvider() {
        embossers = new HashMap<String, FactoryProperties>();

        // Single sided
        addEmbosser(EmbosserType.ROMEO_ATTACHE);
        addEmbosser(EmbosserType.ROMEO_ATTACHE_PRO);
        addEmbosser(EmbosserType.ROMEO_25);
        addEmbosser(EmbosserType.ROMEO_PRO_50);
        addEmbosser(EmbosserType.ROMEO_PRO_LE_NARROW);
        addEmbosser(EmbosserType.ROMEO_PRO_LE_WIDE);
        addEmbosser(EmbosserType.THOMAS);
        addEmbosser(EmbosserType.THOMAS_PRO);
        addEmbosser(EmbosserType.MARATHON);

        // Double sided
        addEmbosser(EmbosserType.ET);
        addEmbosser(EmbosserType.JULIET_PRO);
        addEmbosser(EmbosserType.JULIET_PRO_60);
        addEmbosser(EmbosserType.JULIET_CLASSIC);

        // Production double sided
        addEmbosser(EmbosserType.BOOKMAKER);
        addEmbosser(EmbosserType.BRAILLE_EXPRESS_100);
        addEmbosser(EmbosserType.BRAILLE_EXPRESS_150);
        addEmbosser(EmbosserType.BRAILLE_PLACE);
    }

	private void addEmbosser(FactoryProperties e) {
		embossers.put(e.getIdentifier(), e);
	}
    
	public Embosser newFactory(String identifier) {
		FactoryProperties fp = embossers.get(identifier);
		switch ((EmbosserType)fp) {
		case ROMEO_ATTACHE:
			return new EnablingTechnologiesSingleSidedEmbosser(tableCatalogService, EmbosserType.ROMEO_ATTACHE);
		case ROMEO_ATTACHE_PRO:
			return new EnablingTechnologiesSingleSidedEmbosser(tableCatalogService, EmbosserType.ROMEO_ATTACHE_PRO);
		case ROMEO_25:
			return new EnablingTechnologiesSingleSidedEmbosser(tableCatalogService, EmbosserType.ROMEO_25);
		case ROMEO_PRO_50:
			return new EnablingTechnologiesSingleSidedEmbosser(tableCatalogService, EmbosserType.ROMEO_PRO_50);
		case ROMEO_PRO_LE_NARROW:
			return new EnablingTechnologiesSingleSidedEmbosser(tableCatalogService, EmbosserType.ROMEO_PRO_LE_NARROW);
		case ROMEO_PRO_LE_WIDE:
			return new EnablingTechnologiesSingleSidedEmbosser(tableCatalogService, EmbosserType.ROMEO_PRO_LE_WIDE);
		case THOMAS:
			return new EnablingTechnologiesSingleSidedEmbosser(tableCatalogService, EmbosserType.THOMAS);
		case THOMAS_PRO:
			return new EnablingTechnologiesSingleSidedEmbosser(tableCatalogService, EmbosserType.THOMAS_PRO);
		case MARATHON:
			return new EnablingTechnologiesSingleSidedEmbosser(tableCatalogService, EmbosserType.MARATHON);
		case ET:
			return new EnablingTechnologiesDoubleSidedEmbosser(tableCatalogService, EmbosserType.ET);
		case JULIET_PRO:
			return new EnablingTechnologiesDoubleSidedEmbosser(tableCatalogService, EmbosserType.JULIET_PRO);
		case JULIET_PRO_60:
			return new EnablingTechnologiesDoubleSidedEmbosser(tableCatalogService, EmbosserType.JULIET_PRO_60);
		case JULIET_CLASSIC:
			return new EnablingTechnologiesDoubleSidedEmbosser(tableCatalogService, EmbosserType.JULIET_CLASSIC);
		case BOOKMAKER:
			return new EnablingTechnologiesDoubleSidedEmbosser(tableCatalogService, EmbosserType.BOOKMAKER);
		case BRAILLE_EXPRESS_100:
			return new EnablingTechnologiesDoubleSidedEmbosser(tableCatalogService, EmbosserType.BRAILLE_EXPRESS_100);
		case BRAILLE_EXPRESS_150:
			return new EnablingTechnologiesDoubleSidedEmbosser(tableCatalogService, EmbosserType.BRAILLE_EXPRESS_150);
		case BRAILLE_PLACE:
			return new EnablingTechnologiesDoubleSidedEmbosser(tableCatalogService, EmbosserType.BRAILLE_PLACE);
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
