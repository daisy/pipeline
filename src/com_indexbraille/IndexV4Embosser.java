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

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.paper.Length;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.embosser.ConfigurableEmbosser;
import org.daisy.braille.embosser.EmbosserFactoryException;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.EmbosserWriterProperties;
import org.daisy.braille.embosser.PrintPage;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.embosser.StandardLineBreaks;
import org.daisy.braille.embosser.UnsupportedPaperException;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.braille.table.TableFilter;

import com_indexbraille.IndexEmbosserProvider.EmbosserType;

public class IndexV4Embosser extends IndexEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3888325825465502071L;
	private final static TableFilter tableFilter;
    private final static String table6dot = "org.daisy.braille.table.DefaultTableProvider.TableType.EN_US";
  //private final static String table8dot = "com_indexbraille.IndexTableProvider.TableType.INDEX_TRANSPARENT_8DOT";

    private int bindingMargin = 0;
    private static final int maxBindingMargin = 10;

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

    public IndexV4Embosser(TableCatalogService service, EmbosserType props) {

        super(service, props);

        setTable = service.newTable(table6dot);
        duplexEnabled = true;

        switch (type) {
            case INDEX_BASIC_D_V4:
                maxCellsInWidth = 49;
                break;
            case INDEX_EVEREST_D_V4:
            case INDEX_BRAILLE_BOX_V4:          
                maxCellsInWidth = 48;                
                break;
            default:
                throw new IllegalArgumentException("Unsupported embosser type");
        }

        maxNumberOfCopies = 10000;
        maxMarginInner = 10;
        maxMarginOuter = 10;
        maxMarginTop = 10;
    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    @Override
    public boolean supportsPrintPage(PrintPage dim) {

        if (dim==null) { return false; }
        if (type == EmbosserType.INDEX_BRAILLE_BOX_V4) {
            Length across = dim.getLengthAcrossFeed();
            Length along = dim.getLengthAlongFeed();
            if (saddleStitchEnabled) {
                return (across.asMillimeter() == 297 && along.asMillimeter() == 420) ||
                       (across.asInches()     == 11  && along.asInches()     == 17);
            } else {
                return (across.asMillimeter() == 297 && along.asMillimeter() == 210) ||
                       (across.asInches()     == 11  && along.asInches()     == 8.5) ||
                       (across.asInches()     == 11  && along.asInches()     == 11.5);
            }
        } else {
            return super.supportsPrintPage(dim);
        }
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

        byte[] header = getIndexV4Header();
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

    private byte[] getIndexV4Header() {

        StringBuffer header = new StringBuffer();

        header.append((char)0x1b);
        header.append("D");                                           // Activate temporary formatting properties of a document
        header.append("BT0");                                         // Default braille table
        header.append(",TD0");                                        // Text dot distance = 2.5 mm
        header.append(",LS50");                                       // Line spacing = 5 mm
        header.append(",DP");

        if (saddleStitchEnabled
                && !duplexEnabled)   { header.append('8'); } else
//      if (swZFoldingEnabled
//              && !duplexEnabled)   { header.append('7'); } else
//      if (swZFoldingEnabled)       { header.append('6'); } else
        if (zFoldingEnabled
                && !duplexEnabled)   { header.append('5'); } else
        if (saddleStitchEnabled)     { header.append('4'); } else
        if (zFoldingEnabled)         { header.append('3'); } else
        if (duplexEnabled)           { header.append('2'); } else
                                     { header.append('1'); }          // Page mode
        if (numberOfCopies > 1) {
            header.append(",MC");
            header.append(String.valueOf(numberOfCopies));            // Multiple copies
        }
        //header.append(",MI1");                                      // Multiple impact = 1
        header.append(",PN0");                                        // No page number
        header.append(",CH");
        header.append(String.valueOf(getMaxWidth(getPageFormat())));  // Characters per line
        header.append(",LP");
        header.append(String.valueOf(getMaxHeight(getPageFormat()))); // Lines per page
        header.append(",BI");
        header.append(String.valueOf(bindingMargin));                 // Binding margin
        header.append(",TM");
        header.append(String.valueOf(marginTop));                     // Top margin

        header.append(";");

        return header.toString().getBytes();

    }
}
