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
package se_tpb;

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

public class CXTableProvider implements TableProvider {
//	public final static String IS_ONE_TO_ONE = "is one-to-one";
//	public final static String IS_DISPLAY_FORMAT = "is display format";
	
	enum TableType {
		SV_SE_CX 
	};

	private final Map<TableType, Table> tables;

	public CXTableProvider() {
		tables = new HashMap<TableType, Table>(); 
		tables.put(TableType.SV_SE_CX, new EmbosserTable<TableType>("Swedish CX", "Matches the Swedish representation in CX", TableType.SV_SE_CX, EightDotFallbackMethod.values()[0], '\u2800'){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1834860594148104502L;

			@Override
			public BrailleConverter newBrailleConverter() {
				return new EmbosserBrailleConverter(
						new String(
								" a,b.k;l^cif/msp'e:h*o!r~djgäntq_å?ê-u(v@îöë§xèç\"û+ü)z=à|ôwï#yùé"),
						Charset.forName("UTF-8"), fallback, replacement, true);
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
