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
package org.daisy.braille.table;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;

/**
 * Provides a default table, for convenience.
 * @author Joel Håkansson
 */
public class DefaultTableProvider extends AbstractConfigurableTableProvider<DefaultTableProvider.TableType> {
	
	enum TableType {
		EN_US, // US computer braille, compatible with
				// "Braillo USA 6 DOT 001.00"
	};

	private final ArrayList<Table> tables;

	/**
	 * Creates a new DefaultTableProvider
	 */
	public DefaultTableProvider() {
		super(EightDotFallbackMethod.values()[0], '\u2800');
		tables = new ArrayList<Table>(); 
		tables.add(new EmbosserTable<TableType>("US", "Commonly used embosser table", TableType.EN_US, this));
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
		case EN_US:
			return new EmbosserBrailleConverter(
					new String(
							" A1B'K2L@CIF/MSP\"E3H9O6R^DJG>NTQ,*5<-U8V.%[$+X!&;:4\\0Z7(_?W]#Y)="),
					Charset.forName("UTF-8"), fallback, replacement, true);
		default:
			throw new IllegalArgumentException("Cannot find table type "
					+ t);
		}
	}

	//jvm1.6@Override
	public Collection<Table> list() {
		return tables;
	}

}
