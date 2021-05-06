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
package org.daisy.braille.utils.impl.provider.brailler;

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
public class EnablingTechnologiesEmbosserProvider implements EmbosserProvider {

    /**
     * TODO: write java doc.
     */
    public enum EmbosserType implements EmbosserFactoryProperties {
        ROMEO_ATTACHE("Romeo Attache", ""),
        ROMEO_ATTACHE_PRO("Romeo Attache Pro", ""),
        ROMEO_25("Romeo 25", ""),
        ROMEO_PRO_50("Romeo Pro 50", ""),
        ROMEO_PRO_LE_NARROW("Romeo Pro LE Narrow", ""),
        ROMEO_PRO_LE_WIDE("Romeo Pro LE Wide", ""),
        THOMAS("Thomas", ""),
        THOMAS_PRO("Thomas Pro", ""),
        MARATHON("Marathon", ""),
        ET("ET", ""),
        JULIET_PRO("Juliet Pro", ""),
        JULIET_PRO_60("Juliet Pro 60", ""),
        JULIET_CLASSIC("Juliet Classic", ""),
        BOOKMAKER("Bookmaker", ""),
        BRAILLE_EXPRESS_100("Braille Express 100", ""),
        BRAILLE_EXPRESS_150("Braille Express 150", ""),
        BRAILLE_PLACE("BraillePlace", "");
        private static final String MAKE = "Enabling Technologies";
        private final String name;
        private final String model;
        private final String desc;
        private final String identifier;

        EmbosserType(String model, String desc) {
            this.name = MAKE + " - " + model;
            this.model = model;
            this.desc = desc;
            this.identifier = "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType." + this.toString();
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

    public EnablingTechnologiesEmbosserProvider() {
        embossers = new HashMap<>();

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

    private void addEmbosser(EmbosserFactoryProperties e) {
        embossers.put(e.getIdentifier(), e);
    }

    @Override
    public Embosser newFactory(String identifier) {
        FactoryProperties fp = embossers.get(identifier);
        switch ((EmbosserType) fp) {
            case ROMEO_ATTACHE:
                return new EnablingTechnologiesSingleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.ROMEO_ATTACHE
                );
            case ROMEO_ATTACHE_PRO:
                return new EnablingTechnologiesSingleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.ROMEO_ATTACHE_PRO
                );
            case ROMEO_25:
                return new EnablingTechnologiesSingleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.ROMEO_25
                );
            case ROMEO_PRO_50:
                return new EnablingTechnologiesSingleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.ROMEO_PRO_50
                );
            case ROMEO_PRO_LE_NARROW:
                return new EnablingTechnologiesSingleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.ROMEO_PRO_LE_NARROW
                );
            case ROMEO_PRO_LE_WIDE:
                return new EnablingTechnologiesSingleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.ROMEO_PRO_LE_WIDE
                );
            case THOMAS:
                return new EnablingTechnologiesSingleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.THOMAS
                );
            case THOMAS_PRO:
                return new EnablingTechnologiesSingleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.THOMAS_PRO
                );
            case MARATHON:
                return new EnablingTechnologiesSingleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.MARATHON
                );
            case ET:
                return new EnablingTechnologiesDoubleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.ET
                );
            case JULIET_PRO:
                return new EnablingTechnologiesDoubleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.JULIET_PRO
                );
            case JULIET_PRO_60:
                return new EnablingTechnologiesDoubleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.JULIET_PRO_60
                );
            case JULIET_CLASSIC:
                return new EnablingTechnologiesDoubleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.JULIET_CLASSIC
                );
            case BOOKMAKER:
                return new EnablingTechnologiesDoubleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.BOOKMAKER
                );
            case BRAILLE_EXPRESS_100:
                return new EnablingTechnologiesDoubleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.BRAILLE_EXPRESS_100
                );
            case BRAILLE_EXPRESS_150:
                return new EnablingTechnologiesDoubleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.BRAILLE_EXPRESS_150
                );
            case BRAILLE_PLACE:
                return new EnablingTechnologiesDoubleSidedEmbosser(
                    tableCatalogService,
                    EmbosserType.BRAILLE_PLACE
                );
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
