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
package com_indexbraille;

import java.io.OutputStream;

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.EmbosserWriterProperties;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.braille.table.TableFilter;

import com_indexbraille.IndexEmbosserProvider.EmbosserType;

public class BlueBarEmbosser extends IndexEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2619451994009139923L;
	private final static TableFilter tableFilter;
    private final static String table6dot = IndexTableProvider.class.getCanonicalName() + ".TableType.INDEX_TRANSPARENT_6DOT";
    
    static {
        tableFilter = new TableFilter() {
            @Override
            public boolean accept(FactoryProperties object) {
                if (object == null) { return false; }
                String tableID = object.getIdentifier();
                if (tableID.equals(table6dot)) { return true; }
                return false;
            }
        };
    }

    public BlueBarEmbosser(TableCatalogService service, EmbosserType props) {
        
        super(service, props);
        setTable = service.newTable(table6dot);
    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    public EmbosserWriter newEmbosserWriter(OutputStream os) {

        PageFormat page = getPageFormat();

        if (!supportsPageFormat(page)) {
            throw new IllegalArgumentException("Unsupported paper for embosser " + getDisplayName());
        }

        EmbosserWriterProperties props =
                new SimpleEmbosserProperties(getMaxWidth(page), getMaxHeight(page))
                    .supports8dot(eightDotsEnabled)
                    .supportsDuplex(duplexEnabled)
                    .supportsAligning(supportsAligning());

        return new IndexTransparentEmbosserWriter(os,
                                                  setTable.newBrailleConverter(),
                                                  null,
                                                  null,
                                                  props);
    }
}
