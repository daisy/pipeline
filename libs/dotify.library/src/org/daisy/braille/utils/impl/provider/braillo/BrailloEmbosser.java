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

import org.daisy.braille.utils.impl.tools.embosser.AbstractEmbosser;
import org.daisy.braille.utils.impl.tools.table.DefaultTableProvider;
import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.dotify.api.table.TableFilter;

/**
 * Provides an abstract Braillo embosser.
 *
 * @author Joel Håkansson
 */
public abstract class BrailloEmbosser extends AbstractEmbosser {
    private static final long serialVersionUID = 7640218914742790228L;
    private static final TableFilter tableFilter;

    static {
        tableFilter = new TableFilter() {
            @Override
            public boolean accept(FactoryProperties object) {
                if (object.getIdentifier().equals(DefaultTableProvider.TableType.EN_US.getIdentifier())) {
                    return true;
                }
                if (object.getIdentifier().startsWith(BrailloTableProvider.TableType.IDENTIFIER_PREFIX)) {
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * Creates a new Braillo embosser.
     *
     * @param service the table catalog
     * @param props   the embosser properties
     */
    public BrailloEmbosser(TableCatalogService service, EmbosserFactoryProperties props) {
        super(service, props);
    }

    @Override
    protected double getCellWidth() {
        //TODO: fix this, width is 6.0325
        return 6;
    }

    @Override
    protected double getCellHeight() {
        return 10;
    }

    @Override
    public TableFilter getTableFilter() {
        return tableFilter;
    }

}
