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

import java.io.OutputStream;

import org.daisy.braille.embosser.ConfigurableEmbosser;
import org.daisy.braille.embosser.EmbosserFactoryException;
import org.daisy.braille.embosser.EmbosserTools;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.EmbosserWriterProperties;
import org.daisy.braille.embosser.PrintPage;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.embosser.StandardLineBreaks;
import org.daisy.braille.embosser.UnsupportedPaperException;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.braille.table.TableFilter;
import org.daisy.factory.FactoryProperties;
import org.daisy.paper.Length;
import org.daisy.paper.PageFormat;

import com_indexbraille.IndexEmbosserProvider.EmbosserType;

public class IndexV3Embosser extends IndexEmbosser {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 192884103325881800L;
	private final static TableFilter tableFilter;
    private final static String table6dot = "org.daisy.braille.table.DefaultTableProvider.TableType.EN_US";
  //private final static String table8dot = "com_indexbraille.IndexTableProvider.TableType.INDEX_TRANSPARENT_8DOT";

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

    public IndexV3Embosser(TableCatalogService service, EmbosserType props) {

        super(service, props);

        setTable = service.newTable(table6dot);

        switch (props) {
            case INDEX_BASIC_S_V3:
                duplexEnabled = false;
                break;
            case INDEX_BASIC_D_V3:
            case INDEX_EVEREST_D_V3:
            case INDEX_4WAVES_PRO_V3:
            case INDEX_4X4_PRO_V3:
                duplexEnabled = true;
                break;
            default:
                throw new IllegalArgumentException("Unsupported embosser type");
        }

        maxNumberOfCopies = 10000;
        maxCellsInWidth = 42;
        maxMarginInner = 10;
        maxMarginOuter = 10;
        maxMarginTop = 10;

        if (props == EmbosserType.INDEX_4X4_PRO_V3) {
            minMarginInner = 1;
            marginInner = 1;
        }
    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    public EmbosserWriter newEmbosserWriter(OutputStream os) {

        PageFormat page = getPageFormat();
        if (!supportsPageFormat(page)) {
            throw new IllegalArgumentException(new UnsupportedPaperException("Unsupported paper"));
        }

        int cellsInWidth = (int)Math.floor(getPrintArea(page).getWidth()/getCellWidth());

        if (cellsInWidth > maxCellsInWidth) {
            throw new IllegalArgumentException(new UnsupportedPaperException("Unsupported paper"));
        }
        if (numberOfCopies > maxNumberOfCopies || numberOfCopies < 1) {
            throw new IllegalArgumentException(new EmbosserFactoryException("Invalid number of copies: " + numberOfCopies + " is not in [1, 10000]"));
        }

        byte[] header = getIndexV3Header();
        byte[] footer = new byte[0];

        EmbosserWriterProperties props =
            new SimpleEmbosserProperties(getMaxWidth(page), getMaxHeight(page))
                .supports8dot(eightDotsEnabled)
                .supportsDuplex(duplexEnabled)
                .supportsAligning(supportsAligning());

        if (eightDotsEnabled) {
            return new IndexTransparentEmbosserWriter(os,
                                                      setTable.newBrailleConverter(),
                                                      header,
                                                      footer,
                                                      props);
        } else {
            return new ConfigurableEmbosser.Builder(os, setTable.newBrailleConverter())
                            .breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
                            .padNewline(ConfigurableEmbosser.Padding.NONE)
                            .footer(footer)
                            .embosserProperties(props)
                            .header(header)
                            .build();
        }
    }

    private byte[] getIndexV3Header() {

        PrintPage page = getPrintPage(getPageFormat());
        Length length = page.getLengthAlongFeed();
        Length width = page.getLengthAcrossFeed();

        byte[] xx;
        byte y;
        double iPart;
        double fPart;

        StringBuffer header = new StringBuffer();

        header.append((char)0x1b);
        header.append("D");                                         // Activate temporary formatting properties of a document
        header.append("BT0");                                       // Default braille table
        header.append(",TD0");                                      // Text dot distance = 2.5 mm
        header.append(",LS50");                                     // Line spacing = 5 mm
        header.append(",DP");
        if (saddleStitchEnabled)    { header.append('4'); } else
        if (zFoldingEnabled && 
                duplexEnabled)      { header.append('3'); } else
        if (zFoldingEnabled)        { header.append('5'); } else
        if (duplexEnabled)          { header.append('2'); } else
                                    { header.append('1'); }         // Page mode
        if (numberOfCopies > 1) {
            header.append(",MC");
            header.append(String.valueOf(numberOfCopies));          // Multiple copies
        }
        //header.append(",MI1");                                    // Multiple impact = 1
        header.append(",PN0");                                      // No page number
        switch (type) {
            case INDEX_BASIC_S_V3:
            case INDEX_BASIC_D_V3: {
                iPart = Math.floor(length.asInches());
                fPart = (length.asInches() - iPart);
                                     xx = EmbosserTools.toBytes((int)iPart, 2);
                if (fPart > 0.75)  { xx = EmbosserTools.toBytes((int)(iPart + 1), 2);
                                     y = '0'; } else
                if (fPart > 2d/3d) { y = '5'; } else
                if (fPart > 0.5)   { y = '4'; } else
                if (fPart > 1d/3d) { y = '3'; } else
                if (fPart > 0.25)  { y = '2'; } else
                if (fPart > 0)     { y = '1'; } else
                                   { y = '0'; }
                header.append(",PL");
                header.append((char)xx[0]);
                header.append((char)xx[1]);
                header.append((char)y);                             // Paper length
            }
            case INDEX_4WAVES_PRO_V3: {
                iPart = Math.floor(width.asInches());
                fPart = (width.asInches() - iPart);
                                     xx = EmbosserTools.toBytes((int)iPart, 2);
                if (fPart > 0.75)  { xx = EmbosserTools.toBytes((int)(iPart + 1), 2);
                                     y = '0'; } else
                if (fPart > 2d/3d) { y = '5'; } else
                if (fPart > 0.5)   { y = '4'; } else
                if (fPart > 1d/3d) { y = '3'; } else
                if (fPart > 0.25)  { y = '2'; } else
                if (fPart > 0)     { y = '1'; } else
                                   { y = '0'; }
                header.append(",PW");
                header.append((char)xx[0]);
                header.append((char)xx[1]);
                header.append((char)y);                             // Paper width
                break;
            }
            case INDEX_EVEREST_D_V3:
            case INDEX_4X4_PRO_V3: {
                header.append(",PL");
                header.append(String.valueOf(
                        (int)Math.ceil(length.asMillimeter())));    // Paper length
                header.append(",PW");
                header.append(String.valueOf(
                        (int)Math.ceil(width.asMillimeter())));     // Paper width
                break;
            }
            default:
        }
        header.append(",IM");
        header.append(String.valueOf(marginInner));                 // Inner margin
        header.append(",OM");
        header.append(String.valueOf(marginOuter));                 // Outer margin
        header.append(",TM");
        header.append(String.valueOf(marginTop));                   // Top margin
        header.append(",BM");
        header.append(String.valueOf(marginBottom));                // Bottom margin
        header.append(";");

        return header.toString().getBytes();

    }
}
