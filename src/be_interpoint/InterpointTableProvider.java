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
package be_interpoint;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter;
import org.daisy.braille.table.TableProvider;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.daisy.braille.table.EmbosserTable;
import org.daisy.braille.table.Table;

/**
 *
 * @author Bert Frees
 * @author Joel Håkansson
 */
public class InterpointTableProvider implements TableProvider {
    
    enum TableType { 
    	USA1_8;
		private final String identifier;
		TableType() {
			this.identifier = this.getClass().getCanonicalName() + "." + this.toString();
		}
		String getIdentifier() {
			return identifier;
		}
    };

    private final Map<String, Table> tables;

    public InterpointTableProvider() {
        tables = new HashMap<String, Table>();
        addTable(new EmbosserTable<TableType>("USA1_8 font", "Interpoint 8-dot table", TableType.USA1_8, EightDotFallbackMethod.values()[0], '\u2800'){

			/**
			 * 
			 */
			private static final long serialVersionUID = 5042547624591164148L;

			@Override
			public BrailleConverter newBrailleConverter() {
				String table = " a,b.k;l\"cif|msp!e:h*o+r>djg`ntq'1?2-u(v$3960x~&<5/8)z={\u007f4w7#y}%"; // TODO: add patterns U+2840 - U+28ff

                StringBuffer sb = new StringBuffer();
                sb.append(table);
                return new EmbosserBrailleConverter(sb.toString(), Charset.forName("ISO-8859-1"), fallback, replacement, true);
			}});
    }

	private void addTable(EmbosserTable<?> t) {
		tables.put(t.getIdentifier(), t);
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
    	return tables.get(t.getIdentifier()).newBrailleConverter();
    }

	public Table newFactory(String identifier) {
		return tables.get(identifier);
	}

    //jvm1.6@Override
    public Collection<Table> list() {
    	//The implementation is intentionally excluded from listing
        return new ArrayList<Table>();
    }
}
