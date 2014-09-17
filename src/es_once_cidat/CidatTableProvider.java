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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import org.daisy.braille.table.AbstractConfigurableTableProvider;
import org.daisy.braille.table.AdvancedBrailleConverter;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.daisy.braille.table.EmbosserTable;
import org.daisy.braille.table.Table;
import org.daisy.braille.tools.StringTranslator.MatchMode;

/**
 *
 * @author Bert Frees
 * @author Joel Håkansson
 */
public class CidatTableProvider extends AbstractConfigurableTableProvider<CidatTableProvider.TableType> {
    
    enum TableType { IMPACTO_TRANSPARENT_6DOT,
                     IMPACTO_TRANSPARENT_8DOT,
                     PORTATHIEL_TRANSPARENT_6DOT,
                     //PORTATHIEL_TRANSPARENT_8DOT
    };

    private final ArrayList<Table> tables;

    public CidatTableProvider() {
        super(EightDotFallbackMethod.values()[0], '\u2800');
        tables = new ArrayList<Table>();
        tables.add(new EmbosserTable<TableType>("Transparent mode",         "Impacto 6-dot in transparent mode",    TableType.IMPACTO_TRANSPARENT_6DOT, this));
        tables.add(new EmbosserTable<TableType>("Transparent mode (8 dot)", "Impacto 8-dot in transparent mode",    TableType.IMPACTO_TRANSPARENT_8DOT, this));
        tables.add(new EmbosserTable<TableType>("Transparent mode",         "Portathiel 6-dot in transparent mode", TableType.PORTATHIEL_TRANSPARENT_6DOT, this));
      //tables.add(new EmbosserTable<TableType>("Transparent mode (8 dot)", "Portathiel 8-dot in transparent mode", TableType.PORTATHIEL_TRANSPARENT_8DOT, this));
    }

    /**
     * Get a new table instance based on the factory's current settings.
     *
     * @param t
     *            the type of table to return, this will override the factory's
     *            default table type.
     * @return returns a new table instance.
     */
    public BrailleConverter newTable(TableType t) {
        switch (t) {
            case IMPACTO_TRANSPARENT_6DOT: {
                ArrayList<String> al = new ArrayList<String>();
                for (int i=0; i<0x1b; i++) {
                    al.add(String.valueOf((char)i));
                }
                al.add(String.copyValueOf(new char[]{(char)0x1b,(char)0x1b}));
                for (int i=0x1c; i<64; i++) {
                    al.add(String.valueOf((char)i));
                }
                return new AdvancedBrailleConverter(
                    al.toArray(new String[al.size()]),
                    Charset.forName("ISO-8859-1"),
                    fallback,
                    replacement,
                    false,
                    MatchMode.RELUCTANT);
            }
            case IMPACTO_TRANSPARENT_8DOT: {
                ArrayList<String> al = new ArrayList<String>();
                for (int i=0; i<0x1b; i++) {
                    al.add(String.valueOf((char)i));
                }
                al.add(String.copyValueOf(new char[]{(char)0x1b,(char)0x1b}));
                for (int i=0x1c; i<256; i++) {
                    al.add(String.valueOf((char)i));
                }
                return new AdvancedBrailleConverter(
                    al.toArray(new String[al.size()]),
                    Charset.forName("ISO-8859-1"),
                    false,
                    MatchMode.RELUCTANT);
            }
            case PORTATHIEL_TRANSPARENT_6DOT: {
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<0x1b; i++) {
                    sb.append((char)i);
                }
                sb.append((char)0xf7);
                for (int i=0x1c; i<64; i++) {
                    sb.append((char)i);
                }
                return new EmbosserBrailleConverter(
                        sb.toString(),
                        Charset.forName("ISO-8859-1"),
                        fallback,
                        replacement,
                        false);
            }
            default:
                throw new IllegalArgumentException("Cannot find table type " + t);
        }
    }

    //jvm1.6@Override
    public Collection<Table> list() {
        return tables;
    }
}
