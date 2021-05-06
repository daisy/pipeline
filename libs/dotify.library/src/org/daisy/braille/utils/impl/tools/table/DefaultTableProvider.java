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
package org.daisy.braille.utils.impl.tools.table;

import org.daisy.dotify.api.embosser.EightDotFallbackMethod;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableProvider;
import org.osgi.service.component.annotations.Component;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a default table, for convenience.
 *
 * @author Joel Håkansson
 */
@Component
public class DefaultTableProvider implements TableProvider {

    /**
     * Table ids.
     */
    public enum TableType implements FactoryProperties {
        /**
         * US computer braille.
         */
        EN_US("US", "Commonly used embosser table"), // compatible with "Braillo USA 6 DOT 001.00"
        ;
        private final String name;
        private final String desc;
        private final String identifier;

        TableType(String name, String desc) {
            this.name = name;
            this.desc = desc;
            this.identifier = "org.daisy.braille.impl.table.DefaultTableProvider.TableType." + this.toString();
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
    }

    ;

    private final Map<String, FactoryProperties> tables;

    /**
     * Creates a new DefaultTableProvider.
     */
    public DefaultTableProvider() {
        tables = new HashMap<String, FactoryProperties>();
        addTable(TableType.EN_US);
    }

    private void addTable(FactoryProperties t) {
        tables.put(t.getIdentifier(), t);
    }

    /**
     * Get a new table instance based on the factory's current settings.
     *
     * @param t the type of table to return, this will override the factory's
     *          default table type.
     * @return returns a new table instance.
     */
    public BrailleConverter newTable(TableType t) {
        return newFactory(t.getIdentifier()).newBrailleConverter();
    }

    @Override
    public Table newFactory(String identifier) {
        FactoryProperties fp = tables.get(identifier);
        switch ((TableType) fp) {
            case EN_US:
                return new EmbosserTable(TableType.EN_US, EightDotFallbackMethod.values()[0], '\u2800') {

                    /**
                     *
                     */
                    private static final long serialVersionUID = -64524572279113677L;

                    @Override
                    public BrailleConverter newBrailleConverter() {
                        return new EmbosserBrailleConverter(
                                new String(
                                        " A1B'K2L@CIF/MSP\"E3H9O6R^DJG>NTQ,*5<-U8V.%[$+X!&;:4\\0Z7(_?W]#Y)="),
                                Charset.forName("UTF-8"), fallback, replacement, true);
                    }
                };
            default:
                return null;
        }
    }

    @Override
    public Collection<FactoryProperties> list() {
        return Collections.unmodifiableCollection(tables.values());
    }

}
