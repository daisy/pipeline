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

import org.daisy.braille.api.embosser.FileFormat;
import org.daisy.braille.api.embosser.FileFormatProvider;
import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.table.TableCatalogService;
import org.daisy.braille.impl.spi.SPIHelper;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 *
 * @author Bert Frees
 */
@Component
public class BrailleEditorsFileFormatProvider implements FileFormatProvider {

    public static enum FileType implements FactoryProperties {
        BRF("BRF (Braille Formatted)", "Duxbury Braille file"),
        BRA("BRA", "Spanish Braille file"),
        BRL("BRL", "MicroBraille Braille file");
        private final String name;
        private final String desc;
        private final String identifier;
        FileType (String name, String desc) {
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

    private final Map<String,FactoryProperties> formats;

    public BrailleEditorsFileFormatProvider() {
        formats = new HashMap<String,FactoryProperties>();
        formats.put(FileType.BRF.getIdentifier(), FileType.BRF);
        formats.put(FileType.BRA.getIdentifier(), FileType.BRA);
    }

    public Collection<FactoryProperties> list() {
        return Collections.unmodifiableCollection(formats.values());
    }

    public FileFormat newFactory(String identifier) {
        FileType type = (FileType)formats.get(identifier);
        if (type != null)
            return new BrailleEditorsFileFormat(type, tableCatalogService);
        else
            return null;
    }

    private TableCatalogService tableCatalogService = null;

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
            tableCatalogService = SPIHelper.getTableCatalog();
        }
    }
}
