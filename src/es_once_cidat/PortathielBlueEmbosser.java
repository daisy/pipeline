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
import org.daisy.braille.embosser.EmbosserTools;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.embosser.StandardLineBreaks;
import org.daisy.braille.embosser.StandardPageBreaks;
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
public class PortathielBlueEmbosser extends CidatEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2862233116704332024L;
	private final static TableFilter tableFilter;
    private final static String transparentTable = "es_once_cidat.CidatTableProvider.TableType.PORTATHIEL_TRANSPARENT_6DOT";
  //private final static String transparent8dotTable = CidatTableProvider.class.getCanonicalName() + ".TableType.PORTATHIEL_TRANSPARENT_8DOT";
    private final static String mitTable = "org_daisy.EmbosserTableProvider.TableType.MIT";
    private final static String nabccTable = "org_daisy.EmbosserTableProvider.TableType.NABCC";
    private final static String gbTable = "org_daisy.EmbosserTableProvider.TableType.EN_GB";

    static {
        tableFilter = new TableFilter() {
            //jvm1.6@Override
            public boolean accept(FactoryProperties object) {
                if (object == null) { return false; }
                if (object.getIdentifier().equals(transparentTable))     { return true; }
              //if (object.getIdentifier().equals(transparent8dotTable)) { return true; }
                if (object.getIdentifier().equals(mitTable))             { return true; }
                if (object.getIdentifier().equals(nabccTable))           { return true; }
                if (object.getIdentifier().equals(gbTable))              { return true; }
                return false;
            }
        };
    }

    public PortathielBlueEmbosser(TableCatalogService service, EmbosserType props) {
        
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

        try {

            boolean transparentMode = (setTable.getIdentifier().equals(transparentTable));
            byte[] header = getPortathielHeader(duplexEnabled, eightDotsEnabled, transparentMode);

            ConfigurableEmbosser.Builder b = new ConfigurableEmbosser.Builder(os, setTable.newBrailleConverter())
                .padNewline(ConfigurableEmbosser.Padding.NONE)
                .embosserProperties(
                    new SimpleEmbosserProperties(getMaxWidth(page), getMaxHeight(page))
                        .supportsDuplex(duplexEnabled)
                        .supportsAligning(true)
                        .supports8dot(eightDotsEnabled)
                )
                .header(header);

            if (transparentMode) {
                b = b.breaks(new CidatLineBreaks(CidatLineBreaks.Type.PORTATHIEL_TRANSPARENT))
                     .pagebreaks(new CidatPageBreaks(CidatPageBreaks.Type.PORTATHIEL_TRANSPARENT));
            } else {
                b = b.breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
                     .pagebreaks(new StandardPageBreaks());
            }

            return b.build();

        } catch (EmbosserFactoryException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private byte[] getPortathielHeader(boolean duplex,
                                       boolean eightDots,
                                       boolean transparentMode)
                                throws EmbosserFactoryException {

        PrintPage page = getPrintPage(getPageFormat());
        int pageLength = (int)Math.ceil(page.getHeight()/EmbosserTools.INCH_IN_MM);
        int charsPerLine = EmbosserTools.getWidth(page, getCellWidth());
        int linesPerPage = EmbosserTools.getHeight(page, getCellHeight()); // depends on rowgap

        if (pageLength   < 8  || pageLength   > 13) { throw new UnsupportedPaperException("Paper height = " + pageLength + " inches, must be in [8,13]"); }
        if (charsPerLine < 12 || charsPerLine > 42) { throw new UnsupportedPaperException("Characters per line = " + charsPerLine + ", must be in [12,42]"); }
        if (linesPerPage < 10 || linesPerPage > 31) { throw new UnsupportedPaperException("Lines per page = " + linesPerPage + ", must be in [10,31]"); }

        StringBuffer header = new StringBuffer();
        byte[] bytes;

        if (transparentMode) {
            header.append("\u001b!TP");                                                 // Transparent mode ON
        } else {
            header.append("\u001b!CS2");                                                // Character set = MIT
        }
        header.append("\r\u001b!DT");  header.append(eightDots?'8':'6');                // 6 or 8 dots
        header.append("\r\u001b!DS");  header.append(duplex?'1':'0');                   // Front-side or double-sided embossing
        header.append("\r\u001b!LM0");                                                  // Left margin
        header.append("\r\u001b!SL1");                                                  // Interline space = 1/10 inch
        header.append("\r\u001b!PL");  bytes = EmbosserTools.toBytes(pageLength, 2);
                                       header.append((char)bytes[0]);
                                       header.append((char)bytes[1]);                   // Page length in inches
        header.append("\r\u001b!LP");  bytes = EmbosserTools.toBytes(linesPerPage, 2);
                                       header.append((char)bytes[0]);
                                       header.append((char)bytes[1]);                   // Lines per page
        header.append("\r\u001b!CL");  bytes = EmbosserTools.toBytes(charsPerLine, 2);
                                       header.append((char)bytes[0]);
                                       header.append((char)bytes[1]);                   // Characters per line
        header.append("\r\u001b!CT1");                                                  // Cut off words
        header.append("\r\u001b!NI1");                                                  // No indent
        header.append("\r\u001b!JB0");                                                  // Jumbo mode OFF
        header.append("\r\u001b!FF1");                                                  // Form feeds ON
        header.append('\r');

        return header.toString().getBytes();
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
