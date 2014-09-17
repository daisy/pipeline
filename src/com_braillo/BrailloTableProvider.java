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
package com_braillo;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import org.daisy.braille.table.AbstractConfigurableTableProvider;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.daisy.braille.table.EmbosserTable;
import org.daisy.braille.table.Table;

public class BrailloTableProvider extends AbstractConfigurableTableProvider<BrailloTableProvider.TableType> {
	//public final static String IS_ONE_TO_ONE = "is one-to-one";
	//public final static String IS_DISPLAY_FORMAT = "is display format";
	
	enum TableType {
		BRAILLO_6DOT_001_00, // US computer braille, compatible with
				// "Braillo USA 6 DOT 001.00"
		BRAILLO_6DOT_044_00, // US computer braille (lower case), compatible with
				// "Braillo ENGLAND 6 DOT 044.00" which is identical to
				// "Braillo USA 6 DOT 001.00"
		BRAILLO_6DOT_046_01, // compatible with "Braillo SWEDEN 6 DOT 046.01"
		BRAILLO_6DOT_047_01 // compatible with "Braillo NORWAY 6 DOT 047.01"
	};

	private final ArrayList<Table> tables;

	public BrailloTableProvider() {
		super(EightDotFallbackMethod.values()[0], '\u2800');
		tables = new ArrayList<Table>(); 
		tables.add(new EmbosserTable<TableType>("Braillo USA 6 DOT 001.00", "Compatible with Braillo USA 6 DOT 001.00", TableType.BRAILLO_6DOT_001_00, this));
		tables.add(new EmbosserTable<TableType>("Braillo ENGLAND 6 DOT 044.00", "Compatible with Braillo ENGLAND 6 DOT 044.00", TableType.BRAILLO_6DOT_044_00, this));		
		tables.add(new EmbosserTable<TableType>("Braillo SWEDEN 6 DOT 046.01", "Compatible with Braillo SWEDEN 6 DOT 046.01", TableType.BRAILLO_6DOT_046_01, this));
		tables.add(new EmbosserTable<TableType>("Braillo NORWAY 6 DOT 047.01", "Compatible with Braillo NORWAY 6 DOT 047.01", TableType.BRAILLO_6DOT_047_01, this));		
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
			case BRAILLO_6DOT_001_00:
				return new EmbosserBrailleConverter(
						new String(
								" A1B'K2L@CIF/MSP\"E3H9O6R^DJG>NTQ,*5<-U8V.%[$+X!&;:4\\0Z7(_?W]#Y)="),
						Charset.forName("UTF-8"), fallback, replacement, true);
			case BRAILLO_6DOT_044_00:
				return new EmbosserBrailleConverter(
						new String(
								" a1b'k2l@cif/msp\"e3h9o6r^djg>ntq,*5<-u8v.%[$+x!&;:4\\0z7(_?w]#y)="),
						Charset.forName("UTF-8"), fallback, replacement, true);
			case BRAILLO_6DOT_046_01: // sv-SE
				return new EmbosserBrailleConverter(
						new String(
								" a,b'k;l^cif/msp!e:h*o+r\"djg[ntq_1?2-u<v%396]x\\&#5.8>z=($4w70y)@"),
						Charset.forName("UTF-8"), fallback, replacement, true);
			case BRAILLO_6DOT_047_01: // no-NO
				return new EmbosserBrailleConverter(
						new String(
								" A,B.K;L`CIF/MSP'E:H@O!RaDJG[NTQ*]?r-U\"Vqm\\h&Xli_e%u$Z=k|dWg#Ynj"),
						Charset.forName("UTF-8"), fallback, replacement, false);
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
