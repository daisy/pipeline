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

import java.io.OutputStream;

import org.daisy.braille.embosser.ConfigurableEmbosser;
import org.daisy.braille.embosser.EmbosserFactoryException;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserTools;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.embosser.UnsupportedPaperException;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.braille.table.TableFilter;
import org.daisy.factory.FactoryProperties;
import org.daisy.paper.PageFormat;
import org.daisy.paper.PrintPage;

import es_once_cidat.CidatEmbosserProvider.EmbosserType;

/**
 *
 * @author Bert Frees
 */
public class ImpactoEmbosser extends CidatEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3928307285808352010L;
	private final static TableFilter tableFilter;
    private final static String transparentTable = CidatTableProvider.class.getCanonicalName() + ".TableType.IMPACTO_TRANSPARENT_6DOT";
  //private final static String transparent8dotTable = CidatTableProvider.class.getCanonicalName() + ".TableType.IMPACTO_TRANSPARENT_8DOT";

    static {
        tableFilter = new TableFilter() {
            //jvm1.6@Override
            public boolean accept(FactoryProperties object) {
                if (object == null) { return false; }
                if (object.getIdentifier().equals(transparentTable))     { return true; }
              //if (object.getIdentifier().equals(transparent8dotTable)) { return true; }
                return false;
            }
        };
    }

    private int numberOfCopies = 1;
    private int maxNumberOfCopies = 32767;

    public ImpactoEmbosser(TableCatalogService service, EmbosserType props) {

        super(service, props);
        setTable = service.newTable(transparentTable);
    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    public EmbosserWriter newEmbosserWriter(OutputStream os) {

        PageFormat page = getPageFormat();
        
        if (!supportsPageFormat(page)) {
            throw new IllegalArgumentException("Unsupported paper");
        }
        if (numberOfCopies > maxNumberOfCopies || numberOfCopies < 1) {
            throw new IllegalArgumentException(new EmbosserFactoryException(
                    "Invalid number of copies: " + numberOfCopies + " is not in [1, " + maxNumberOfCopies + "]"));
        }
        
        try {

            byte[] header = getImpactoHeader(duplexEnabled, eightDotsEnabled);
            byte[] footer = new byte[]{0x1b,0x54};

            ConfigurableEmbosser.Builder b = new ConfigurableEmbosser.Builder(os, setTable.newBrailleConverter())
                .breaks(new CidatLineBreaks(CidatLineBreaks.Type.IMPACTO_TRANSPARENT))
                .pagebreaks(new CidatPageBreaks(CidatPageBreaks.Type.IMPACTO_TRANSPARENT))
                .padNewline(ConfigurableEmbosser.Padding.NONE)
                .footer(footer)
                .embosserProperties(
                    new SimpleEmbosserProperties(getMaxWidth(page), getMaxHeight(page))
                        .supportsDuplex(duplexEnabled)
                        .supportsAligning(supportsAligning())
                        .supports8dot(eightDotsEnabled)
                )
                .header(header);
            return b.build();

        } catch (EmbosserFactoryException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private byte[] getImpactoHeader(boolean duplex,
                                    boolean eightDots)
                             throws EmbosserFactoryException {

      //int pageCount = 1;                              // examine PEF file
        PageFormat page = getPageFormat();
        PrintPage printPage = getPrintPage(page);
        int pageLength = (int)Math.ceil(printPage.getHeight()/EmbosserTools.INCH_IN_MM);
        int charsPerLine = EmbosserTools.getWidth(printPage, getCellWidth());
        int linesPerPage = EmbosserTools.getHeight(printPage, getCellHeight()); // depends on cell heigth -> depends on rowgap
        
        if (pageLength   < 6  || pageLength   > 13) { throw new UnsupportedPaperException("Paper height = " + pageLength + " inches, must be in [6,13]"); }
        if (charsPerLine < 12 || charsPerLine > 42) { throw new UnsupportedPaperException("Characters per line = " + charsPerLine + ", must be in [12,42]"); }
        if (linesPerPage < 12 || linesPerPage > 43) { throw new UnsupportedPaperException("Lines per page = " + linesPerPage + ", must be in [12,43]"); }

        StringBuffer header = new StringBuffer();

        header.append((char)0x1b); header.append(')');                          // Transparent mode ON
        header.append((char)0x1b); header.append(eightDots?'+':'*');            // 6- or 8-dot matrix
        header.append((char)0x1b); header.append('.');
                                   header.append((char)(0x30 + pageLength));    // Page length in inches
        header.append((char)0x1b); header.append("/1");                         // Line spacing = 1/10 inch
        header.append((char)0x1b); header.append('0');
                                   header.append((char)(0x30 + charsPerLine));  // Characters per line
        header.append((char)0x1b); header.append('1');
                                   header.append((char)(0x30 + linesPerPage));  // Lines per page
        header.append((char)0x1b); header.append('3');                          // Cut off words
        header.append((char)0x1b); header.append(duplex?'Q':'P');               // Front-side or double-sided embossing
      //header.append((char)0x1b); header.append("EP");
      //                           header.append(String.valueOf(pageCount));
      //                           header.append('\n');                         // Number of last page to emboss
        header.append((char)0x1b); header.append("GU0\n");                      // Gutter (binding margin) = 0
        header.append((char)0x1b); header.append("IN0\n");                      // Indent first line of paragraph = 0
        header.append((char)0x1b); header.append("MB0\n");                      // Bottom margin in tenths of an inch = 0
        header.append((char)0x1b); header.append("ML0\n");                      // Left margin in characters = 0
        header.append((char)0x1b); header.append("MR0\n");                      // Right margin in characters = 0
        header.append((char)0x1b); header.append("MT0\n");                      // Top margin in tenths of an inch = 0
        header.append((char)0x1b); header.append("NC");
                                   header.append(String.valueOf(numberOfCopies));
                                   header.append('\n');                         // Number of copies
        header.append((char)0x1b); header.append("SC1\n");                      // Collate copies (one full copy at a time)
        header.append((char)0x1b); header.append("PM0\n");                      // Embossing mode = text embossing mode
        header.append((char)0x1b); header.append("PN0\n");                      // Number pages = no
        header.append((char)0x1b); header.append("PI0\n");                      // Parameter influence = only present job
      //header.append((char)0x1b); header.append("SP1\n");                      // Number of first page to emboss = 1

        return header.toString().getBytes();
    }

    @Override
    public void setFeature(String key, Object value) {

        if (EmbosserFeatures.NUMBER_OF_COPIES.equals(key) && maxNumberOfCopies > 1) {
            try {
                int copies = (Integer)value;
                if (copies < 1 || copies > maxNumberOfCopies) {
                    throw new IllegalArgumentException("Unsupported value for number of copies.");
                }
                numberOfCopies = copies;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for number of copies.");
            }
        } else {
            super.setFeature(key, value);
        }
    }

    @Override
    public Object getFeature(String key) {

        if (EmbosserFeatures.NUMBER_OF_COPIES.equals(key) && maxNumberOfCopies > 1) {
            return numberOfCopies;
        } else {
            return super.getFeature(key);
        }
    }

	public boolean supportsZFolding() {
		return false;
	}
	
	//jvm1.6@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return PrintMode.REGULAR == mode;
	}
	//jvm1.6@Override
	public PrintPage getPrintPage(PageFormat pageFormat) {
		return new PrintPage(pageFormat);
	}
}
