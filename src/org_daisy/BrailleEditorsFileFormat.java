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
import java.util.ArrayList;
import java.io.OutputStream;

import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableCatalog;
import org.daisy.braille.table.TableFilter;
import org.daisy.braille.embosser.FileFormat;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.StandardLineBreaks;
import org.daisy.braille.embosser.EmbosserWriterProperties;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.embosser.ConfigurableEmbosser;
import org.daisy.braille.embosser.AbstractEmbosserWriter.Padding;
import org.daisy.factory.AbstractFactory;
import org.daisy.factory.FactoryProperties;

import org_daisy.BrailleEditorsFileFormatProvider.FileType;


/**
 *
 * @author Bert Frees
 */
public class BrailleEditorsFileFormat extends AbstractFactory implements FileFormat {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4217010913717769350L;
	private FileType type;
    private Table table;
    private TableCatalog tableCatalog;
    private TableFilter tableFilter;
    private final Collection<String> supportedTableIds = new ArrayList<String>();

    private final boolean duplexEnabled = false;
    private final boolean eightDotsEnabled = false;

    public BrailleEditorsFileFormat(String name, String desc, FileType identifier) {

        super(name, desc, identifier);

        type = identifier;

        switch (type) {
            case BRF:
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.MIT");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.NABCC");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.NABCC_8DOT");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.EN_GB");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.NL_NL");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.EN_GB");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.DA_DK");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.DE_DE");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.CS_CZ");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.IT_IT_FIRENZE");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.ES_ES");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.ES_ES_TABLE_2");
                break;
            case BRA:
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.ES_ES");
                supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.ES_ES_TABLE_2");
                break;
            case BRL:
                supportedTableIds.add("org_daisy.BrailleEditorsTableProvider.TableType.BRL");
                break;
            default:
                throw new IllegalArgumentException("Unsupported filetype");
        }

        tableFilter = new TableFilter() {
            //jvm1.6@Override
            public boolean accept(FactoryProperties object) {
                return supportedTableIds.contains(object.getIdentifier());
            }
        };

        tableCatalog = TableCatalog.newInstance();
        table = tableCatalog.get("org_daisy.EmbosserTableProvider.TableType.MIT");
    }

    public TableFilter getTableFilter() {
            return tableFilter;
    }
    
    public boolean supportsTable(Table table) {
        return getTableFilter().accept(table);
    }

    public boolean supportsDuplex() {
        return false;
    }

    public boolean supports8dot() {
        return false;
    }

    public EmbosserWriter newEmbosserWriter(OutputStream os) {

        if (!supportsTable(table)) {
            throw new IllegalArgumentException("Unsupported table: " + table.getDisplayName());
        }

        int maxCols = 1000;
        int maxRows = 1000;

        EmbosserWriterProperties props =
            new SimpleEmbosserProperties(maxCols, maxRows)
                .supports8dot(eightDotsEnabled)
                .supportsDuplex(duplexEnabled)
                .supportsAligning(false);

        switch (type) {
            case BRF:
                return new ConfigurableEmbosser.Builder(os, table.newBrailleConverter())
                                    .breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
                                    .padNewline(Padding.BEFORE)
                                    .embosserProperties(props)
                                    .build();
            case BRA:
                return new ConfigurableEmbosser.Builder(os, table.newBrailleConverter())
                                    .breaks(new StandardLineBreaks(StandardLineBreaks.Type.UNIX))
                                    .pagebreaks(new NoPageBreaks())
                                    .padNewline(Padding.AFTER)
                                    .embosserProperties(props)
                                    .build();
            case BRL:
                return new MicroBrailleFileFormatWriter(os);
            default:
                return null;
        }
    }
    
    public String getFileExtension() {
        return "." + type.name().toLowerCase();
    }

    public void setFeature(String key, Object value) {

        if (EmbosserFeatures.TABLE.equals(key)) {
            if (value == null) {
                throw new IllegalArgumentException("Unsupported value for table");
            }
            Table t;
            try {
                t = (Table)value;
            } catch (ClassCastException e) {
                t = TableCatalog.newInstance().get(value.toString());
            }
            if (getTableFilter().accept(t)) {
                table = t;
            } else {
                throw new IllegalArgumentException("Unsupported value for table.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported feature " + key);
        }
    }

    public Object getFeature(String key) {

        if (EmbosserFeatures.TABLE.equals(key)) {
            return table;
        } else {
            throw new IllegalArgumentException("Unsupported feature " + key);
        }
    }

    public Object getProperty(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
