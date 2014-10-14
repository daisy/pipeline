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
public class BrailleEditorsTableProvider implements TableProvider {
	enum TableType {
		MICROBRAILLE
	};

	private final Map<TableType, Table> tables;
	
	public BrailleEditorsTableProvider() {
		tables = new HashMap<TableType, Table>(); 
		tables.put(TableType.MICROBRAILLE, new EmbosserTable<TableType>("MicroBraille BRL-file", "Table for MicroBraille files (.brl)", TableType.MICROBRAILLE, EightDotFallbackMethod.values()[0], '\u2800'){

			/**
			 * 
			 */
			private static final long serialVersionUID = -5319371919262493138L;

			@Override
			public BrailleConverter newBrailleConverter() {
				return new EmbosserBrailleConverter(new String(" á¬â§ë»ì«ãéæ¯íóð½åºèªï¡òüäêçÁîôñ¾ÜÎÇ­õ¿öûÓ¼ÄÉøÝÛß×®Ï¢ú¨àýÔ÷Å£ùÐÆ"), Charset.forName("UTF-8"), fallback, replacement, false);
			}});
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
