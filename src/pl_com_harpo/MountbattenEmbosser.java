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
package pl_com_harpo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.daisy.braille.embosser.AbstractEmbosser;
import org.daisy.braille.embosser.ConfigurableEmbosser;
import org.daisy.braille.embosser.EmbosserFactoryException;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.EmbosserWriterProperties;
import org.daisy.braille.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.braille.table.TableFilter;
import org.daisy.factory.FactoryProperties;
import org.daisy.paper.Area;
import org.daisy.paper.PageFormat;
import org.daisy.paper.Paper;
import org.daisy.paper.PrintPage;
import org.daisy.paper.SheetPaper;
import org.daisy.paper.SheetPaperFormat;
import org.daisy.paper.SheetPaperFormat.Orientation;
import org.daisy.printing.Device;

public class MountbattenEmbosser extends AbstractEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8609686103308218762L;
	private double maxPageWidth = 330d;
    private double maxPageHeight = 382d;
    private double minPageWidth = 100d;
    private double minPageHeight = 98d;

    //TODO: continuous paper?

    private final static TableFilter tableFilter;
    private final static String table6dot = "org_daisy.EmbosserTableProvider.TableType.NABCC";

    static {
        tableFilter = new TableFilter() {
            @Override
            public boolean accept(FactoryProperties object) {
                if (object == null) { return false; }
                if (object.getIdentifier().equals(table6dot))     { return true; }
                return false;
            }
        };
    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    public MountbattenEmbosser(TableCatalogService service, FactoryProperties props) {

        super(service, props.getDisplayName(), props.getDescription(), props.getIdentifier());

        setTable = service.newTable(table6dot);

        setCellWidth(5.9d);
        setCellHeight(10.1d);
    }

    @Override
    public boolean supportsPaper(Paper paper) {
        if (paper == null) { return false; }
        try {
            SheetPaper p = paper.asSheetPaper();
            if (supportsPageFormat(new SheetPaperFormat(p, Orientation.DEFAULT))) { return true; }
            if (supportsPageFormat(new SheetPaperFormat(p, Orientation.REVERSED))) { return true; }
        } catch (ClassCastException e) {
        }
        return false;
    }

    //jvw1.6@Override
    public boolean supportsPageFormat(PageFormat format) {
        if (format == null) { return false; }
        try {
            return supportsPrintPage(getPrintPage(format.asSheetPaperFormat()));
        } catch (ClassCastException e) {
            return false;
        }
    }

    public boolean supportsPrintPage(PrintPage dim) {
        if (dim==null) { return false; }
        return (dim.getWidth()  <= maxPageWidth)  &&
               (dim.getWidth()  >= minPageWidth)  &&
               (dim.getHeight() <= maxPageHeight) &&
               (dim.getHeight() >= minPageHeight);
    }

    public boolean supportsVolumes() {
        return false;
    }

    public boolean supportsAligning() {
        return true;
    }

    public boolean supports8dot() {
        return false;
    }

    public boolean supportsDuplex() {
        return false;
    }

    public EmbosserWriter newEmbosserWriter(Device device) {

        try {
            File f = File.createTempFile(this.getClass().getCanonicalName(), ".tmp");
            f.deleteOnExit();
            EmbosserWriter ew = newEmbosserWriter(new FileOutputStream(f));
            return new FileToDeviceEmbosserWriter(ew, f, device);
        } catch (IOException e) {
        }
        throw new IllegalArgumentException("Embosser does not support this feature.");
    }

    public EmbosserWriter newEmbosserWriter(OutputStream os) {

        PageFormat page = getPageFormat();

        if (!supportsPageFormat(page)) {
            throw new IllegalArgumentException(new EmbosserFactoryException("Unsupported paper"));
        }

        byte[] header = getMountbattenHeader();
        byte[] footer = getMountbattenFooter();

        EmbosserWriterProperties props =
            new SimpleEmbosserProperties(getMaxWidth(page), getMaxHeight(page))
                .supports8dot(supports8dot())
                .supportsDuplex(supportsDuplex())
                .supportsAligning(supportsAligning());

        return new ConfigurableEmbosser.Builder(os, setTable.newBrailleConverter())
                        .breaks(new MountbattenLineBreaks())
                        .padNewline(ConfigurableEmbosser.Padding.NONE)
                        .footer(footer)
                        .embosserProperties(props)
                        .header(header)
                        .build();
    }

    private byte[] getMountbattenHeader() {

        // Instruct users to
        // * (save settings)
        // * switch Mountbatten to the required state before printing (embossing mode, paper settings)
        // * (restore settings afterwards)

        // PageFormat page = getPageFormat();
        // double paperLenght = page.getHeight();

        StringBuffer header = new StringBuffer();

        // header.append("{std}");                 // Factory settings
        // header.append("{adv}");                 // Advanced mode
        // header.append("{m}");                   // Embossing speed
        // header.append("{cp}");                  // Continuous paper ?
        // header.append("{inch on}");
        // header.append("{fl x.y}");              // Form length in inch

        header.append("{man}");                 // Manual new line
        header.append("{bc 1}");                // Braille table = NABCC
        header.append("{lm 0}");                // Left margin = 0
        header.append("{rm 0}");                // Right margin = 0
        header.append("{tm 0}");                // Top margin = 0

        return header.toString().getBytes();
    }

    private byte[] getMountbattenFooter() {

        StringBuffer footer = new StringBuffer();

        // footer.append("{restore}");                 // Restore the default settings

        return footer.toString().getBytes();
    }

    @Override
    public Area getPrintableArea(PageFormat pageFormat) {

        PrintPage printPage = getPrintPage(pageFormat);

        double unprintableInner = 15d;
        double unprintableTop = 15d;
        double unprintableOuter = 15d;
        double unprintableBottom = 15d;
        double printablePageWidth = printPage.getWidth() - unprintableInner - unprintableOuter;
        double printablePageHeight = printPage.getHeight() - unprintableTop - unprintableBottom;

        return new Area(printablePageWidth,
                        printablePageHeight,
                        unprintableInner,
                        unprintableTop);
    }

	public boolean supportsZFolding() {
		return false;
	}
	
	@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return PrintMode.REGULAR == mode;
	}
	
	@Override
	public PrintPage getPrintPage(PageFormat pageFormat) {
		return new PrintPage(pageFormat);
	}
}
