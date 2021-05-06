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
package org.daisy.braille.utils.impl.provider.cidat;

import org.daisy.dotify.api.embosser.Embosser;
import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.embosser.EmbosserProvider;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.dotify.api.table.TableCatalogService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bert Frees
 */
@Component
public class CidatEmbosserProvider implements EmbosserProvider {

    /**
     * TODO: write java doc.
     */
    public enum EmbosserType implements EmbosserFactoryProperties {
        IMPACTO_600("Impacto 600", "High-quality, high-speed (600 pages per hour) double-sided embosser"),
        IMPACTO_TEXTO("Impacto Texto", "High-quality, high-speed (800 pages per hour) double-sided embosser"),
        PORTATHIEL_BLUE("Portathiel Blue", "Small, lightweight, portable double-sided embosser");
        private static final String MAKE = "Cidat";
        private final String name;
        private final String model;
        private final String desc;
        private final String identifier;

        EmbosserType(String model, String desc) {
            this.name = MAKE + " - " + model;
            this.model = model;
            this.desc = desc;
            this.identifier = "es_once_cidat.CidatEmbosserProvider.EmbosserType." + this.toString();
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

    ;

    private final Map<String, EmbosserFactoryProperties> embossers;
    private TableCatalogService tableCatalogService = null;

    public CidatEmbosserProvider() {
        embossers = new HashMap<>();
        addEmbosser(EmbosserType.IMPACTO_600);
        addEmbosser(EmbosserType.IMPACTO_TEXTO);
        addEmbosser(EmbosserType.PORTATHIEL_BLUE);
    }

    private void addEmbosser(EmbosserFactoryProperties e) {
        embossers.put(e.getIdentifier(), e);
    }

    @Override
    public Embosser newFactory(String identifier) {
        FactoryProperties fp = embossers.get(identifier);
        switch ((EmbosserType) fp) {
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

    @Override
    public Collection<EmbosserFactoryProperties> list() {
        return Collections.unmodifiableCollection(embossers.values());
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setTableCatalog(TableCatalogService service) {
        this.tableCatalogService = service;
    }

    public void unsetTableCatalog(TableCatalogService service) {
        this.tableCatalogService = null;
    }

    @Override
    public void setCreatedWithSPI() {
        if (tableCatalogService == null) {
            tableCatalogService = TableCatalog.newInstance();
        }
    }
}
