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

import java.nio.charset.Charset;
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
public class IndexTableProvider implements TableProvider {

	enum TableType { INDEX_TRANSPARENT_6DOT,
                         INDEX_TRANSPARENT_8DOT };

	private final Map<TableType, Table> tables;

	public IndexTableProvider() {
		tables = new HashMap<TableType, Table>();
		tables.put(TableType.INDEX_TRANSPARENT_6DOT, new EmbosserTable<TableType>("Index transparent 6 dot", "Table for transparent mode, 6 dot", TableType.INDEX_TRANSPARENT_6DOT, EightDotFallbackMethod.values()[0], '\u2800'){

			/**
			 * 
			 */
			private static final long serialVersionUID = 4158051645788882958L;

			@Override
			public BrailleConverter newBrailleConverter() {
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<64; i++) {
					sb.append((char)((i & 0x07) + ((i & 0x38) << 1)));
				}
				return new EmbosserBrailleConverter(sb.toString(), Charset.forName("US-ASCII"), fallback, replacement, false);
			}});
        //tables.add(new EmbosserTable<TableType>("Index transparent 8 dot", "Table for transparent mode, 8 dot", TableType.INDEX_TRANSPARENT_8DOT, this));
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
		return tables.get(t).newBrailleConverter();
	}

	//jvm1.6@Override
	public Collection<Table> list() {
		return tables.values();
	}

}
