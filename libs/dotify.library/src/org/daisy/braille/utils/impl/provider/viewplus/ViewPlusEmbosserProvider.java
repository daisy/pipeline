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
package org.daisy.braille.utils.impl.provider.viewplus;

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
 * TODO: write java doc.
 */
@Component
public class ViewPlusEmbosserProvider implements EmbosserProvider {

    /**
     * TODO: write java doc.
     */
    public enum EmbosserType implements EmbosserFactoryProperties {
        PREMIER_80("Premier 80", "80 CPS"),
        PREMIER_100("Premier 100", "100 CPS"),
        ELITE_150("Elite 150", "150 CPS"),
        ELITE_200("Elite 200", "200 CPS"),
        PRO_GEN_II("Pro Gen II", "100 CPS"),
        CUB_JR("Cub Jr.", "30 CPS"),
        CUB("Cub", "50 CPS"),
        MAX("Max", "60 CPS"),
        EMFUSE("EmFuse", "Large-print, Color, and Braille. 400 CPS"),
        EMPRINT_SPOTDOT("Emprint SpotDot", "Ink and Braille. 40-50 CPS");
        private static final String MAKE = "ViewPlus";
        private final String name;
        private final String model;
        private final String desc;
        private final String identifier;

        EmbosserType(String model, String desc) {
            this.name = MAKE + " - " + model;
            this.model = model;
            this.desc = desc;
            this.identifier = "com_viewplus.ViewPlusEmbosserProvider.EmbosserType." + this.toString();
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

    public ViewPlusEmbosserProvider() {
        embossers = new HashMap<>();

        // Elite Braille Printers
        addEmbosser(EmbosserType.ELITE_150);
        addEmbosser(EmbosserType.ELITE_200);

        // Premier Braille Printers
        addEmbosser(EmbosserType.PREMIER_80);
        addEmbosser(EmbosserType.PREMIER_100);

        // Pro Braille Printer
        addEmbosser(EmbosserType.PRO_GEN_II);

        // Desktop Braille Printers
        addEmbosser(EmbosserType.CUB_JR);
        addEmbosser(EmbosserType.CUB);
        addEmbosser(EmbosserType.MAX);

        // EmFuse Color Braille Station
        addEmbosser(EmbosserType.EMFUSE);
        // Emprint Spotdot Ink & Braille Embossers
        addEmbosser(EmbosserType.EMPRINT_SPOTDOT);
    }

    private void addEmbosser(EmbosserFactoryProperties e) {
        embossers.put(e.getIdentifier(), e);
    }

    @Override
    public Embosser newFactory(String identifier) {
        FactoryProperties fp = embossers.get(identifier);
        switch ((EmbosserType) fp) {
            case ELITE_150:
                return new TigerEmbosser(tableCatalogService, EmbosserType.ELITE_150);
            case ELITE_200:
                return new TigerEmbosser(tableCatalogService, EmbosserType.ELITE_200);
            case PREMIER_80:
                return new TigerEmbosser(tableCatalogService, EmbosserType.PREMIER_80);
            case PREMIER_100:
                return new TigerEmbosser(tableCatalogService, EmbosserType.PREMIER_100);
            case PRO_GEN_II:
                return new TigerEmbosser(tableCatalogService, EmbosserType.PRO_GEN_II);
            case CUB_JR:
                return new TigerEmbosser(tableCatalogService, EmbosserType.CUB_JR);
            case CUB:
                return new TigerEmbosser(tableCatalogService, EmbosserType.CUB);
            case MAX:
                return new TigerEmbosser(tableCatalogService, EmbosserType.MAX);
            case EMFUSE:
                return new TigerEmbosser(tableCatalogService, EmbosserType.EMFUSE);
            case EMPRINT_SPOTDOT:
                return new TigerEmbosser(tableCatalogService, EmbosserType.EMPRINT_SPOTDOT);
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
