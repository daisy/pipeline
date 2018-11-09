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
package org.daisy.braille.utils.impl.provider.indexbraille;

import java.io.OutputStream;

import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.embosser.UnsupportedPaperException;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.dotify.api.table.TableFilter;
import org.daisy.braille.utils.impl.provider.indexbraille.IndexEmbosserProvider.EmbosserType;
import org.daisy.braille.utils.impl.tools.embosser.ConfigurableEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.EmbosserTools;
import org.daisy.braille.utils.impl.tools.embosser.SimpleEmbosserProperties;

public class IndexV2Embosser extends IndexEmbosser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3826909986984899820L;
	private static final TableFilter tableFilter;
	private static final String table6dot = "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US";
	//private static final String table8dot = "com_indexbraille.IndexTableProvider.TableType.INDEX_TRANSPARENT_8DOT";
	private final boolean supportsDuplex;

	static {
		tableFilter = new TableFilter() {
			@Override
			public boolean accept(FactoryProperties object) {
				if (object == null) { return false; }
				String tableID = object.getIdentifier();
				if (tableID.equals(table6dot)) { return true; }
				//if (tableID.equals(table8dot)) { return true; }
				return false;
			}
		};
	}

	public IndexV2Embosser(TableCatalogService service, EmbosserType props) {

		super(service, props);

		setTable = service.newTable(table6dot);

		switch (type) {
		case INDEX_BASIC_S_V2:
			duplexEnabled = false;
			supportsDuplex = false;
			break;
		case INDEX_BASIC_D_V2:
		case INDEX_EVEREST_D_V2:
		case INDEX_4X4_PRO_V2:
			duplexEnabled = true;
			supportsDuplex = true;
			break;
		default:
			throw new IllegalArgumentException("Unsupported embosser type");
		}

		maxNumberOfCopies = 999;
		minCellsInWidth = 23;
		maxCellsInWidth = 42;
	}
	
	@Override
	public boolean supportsDuplex() {
		return supportsDuplex;
	}

	@Override
	public TableFilter getTableFilter() {
		return tableFilter;
	}

	@Override
	public EmbosserWriter newEmbosserWriter(OutputStream os) {

		//int pageCount = 1;                                 // ???
		//boolean magazine = false;

		PageFormat page = getPageFormat();
		if (!supportsPageFormat(page)) {
			throw new IllegalArgumentException(new UnsupportedPaperException("Unsupported paper"));
		}

		int cellsInWidth = (int)Math.floor(getPrintArea(page).getWidth()/getCellWidth());

		if (cellsInWidth < minCellsInWidth || cellsInWidth > maxCellsInWidth) {
			throw new IllegalArgumentException(new UnsupportedPaperException("Unsupported paper"));
		}
		if (numberOfCopies > maxNumberOfCopies || numberOfCopies < 1) {
			throw new IllegalArgumentException(new UnsupportedPaperException("Invalid number of copies: " + numberOfCopies + " is not in [1, 999]"));
		}
		//if (magazine && pageCount > 200) {
		//    throw new IllegalArgumentException(new UnsupportedPaperException("Number of pages = " + pageCount +  "; cannot exceed 200 when in magazine style mode"));
		//}

		byte[] header = getIndexV2Header(cellsInWidth, false);
		byte[] footer = new byte[]{0x1a};

		SimpleEmbosserProperties props =
				SimpleEmbosserProperties.with(getMaxWidth(page), getMaxHeight(page))
				.supportsDuplex(duplexEnabled)
				.supportsAligning(supportsAligning())
				.build();

		return new ConfigurableEmbosser.Builder(os, setTable.newBrailleConverter())
				.breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
				.padNewline(ConfigurableEmbosser.Padding.NONE)
				.footer(footer)
				.embosserProperties(props)
				.header(header)
				.build();
	}

	private byte[] getIndexV2Header(int cellsInWidth, boolean eightDots) {

		StringBuffer header = new StringBuffer();

		header.append((char)0x1b);
		header.append((char)0x0f);
		header.append((char)0x02);
		header.append("0,");                                    // 0: US table
		header.append("0,");                                    // 1: Type of braille code    = Computer
		header.append(eightDots?'1':'0');                       // 2: 6/8 dot braille
		header.append(",0,");                                   // 3: Capital prefix          = off
		header.append("x,");                                    // 4: Baud rate
		header.append("x,");                                    // 5: Number of data bits
		header.append("x,");                                    // 6: Parity
		header.append("x,");                                    // 7: Number of stop bits
		header.append("x,");                                    // 8: Handshake
		byte[] w = EmbosserTools.toBytes(cellsInWidth-23, 2);
		header.append((char)w[0]);
		header.append((char)w[1]);                              // 9: Characters per line
		header.append(",");
		header.append("0,");                                    // 10: Left margin            = 0 characters
		header.append("0,");                                    // 11: Binding margin         = 0 characters
		header.append("0,");                                    // 12: Top margin             = 0 lines
		header.append("0,");                                    // 13: Bottom margin          = 0 lines
		header.append("4,");                                    // 14: Line spacing           = 5 mm
		if (saddleStitchEnabled)  { header.append('3'); } else
			if (zFoldingEnabled)      { header.append('2'); } else
				if (duplexEnabled)        { header.append('1'); } else
				{ header.append('0'); }              // 15: Page mode              = 1,2 or 4 pages per sheet or z-folding (3)
		header.append(",");
		header.append("0,");                                    // 16: Print mode             = normal
		header.append("0,");                                    // 17: Page number            = off
		header.append("x,");                                    // 18: N/A
		header.append("0,");                                    // 19: Word wrap              = off
		header.append("0,");                                    // 20: Auto line feed         = off (??)
		header.append("x,");                                    // 21: Form feed
		header.append("x,");                                    // 22: Volume
		header.append("x,");                                    // 23: Impact level
		header.append("x,");                                    // 24: Delay
		header.append("x,");                                    // 25: Print quality
		header.append("x,");                                    // 26: Graphic dot distance
		header.append("0,");                                    // 27: Text dot distance      = normal (2.5 mm)
		header.append("1,");                                    // 28: Setup                  = open (??)
		header.append("x,x,x,x,x,x,x,x,x,x,x,");                // 29-39: N/A
		header.append((char)0x1b);
		header.append((char)0x0f);

		if (numberOfCopies > 1) {
			header.append((char)0x1b);
			header.append((char)0x12);
			byte[] c = EmbosserTools.toBytes(numberOfCopies, 3);
			header.append((char)c[0]);
			header.append((char)c[1]);
			header.append((char)c[2]);
			header.append((char)0x1b);
			header.append((char)0x12);
		}

		return header.toString().getBytes();
	}
}
