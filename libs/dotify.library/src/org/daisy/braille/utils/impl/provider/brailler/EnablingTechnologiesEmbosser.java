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
package org.daisy.braille.utils.impl.provider.brailler;

import org.daisy.braille.utils.impl.provider.brailler.EnablingTechnologiesEmbosserProvider.EmbosserType;
import org.daisy.braille.utils.impl.tools.embosser.AbstractEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.ConfigurableEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.EmbosserTools;
import org.daisy.braille.utils.impl.tools.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.utils.impl.tools.embosser.SimpleEmbosserProperties;
import org.daisy.dotify.api.embosser.Device;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.PrintPage;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.paper.Area;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.Paper;
import org.daisy.dotify.api.paper.SheetPaper;
import org.daisy.dotify.api.paper.SheetPaperFormat;
import org.daisy.dotify.api.paper.SheetPaperFormat.Orientation;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.dotify.api.table.TableFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Bert Frees
 */
public abstract class EnablingTechnologiesEmbosser extends AbstractEmbosser {

    private static final long serialVersionUID = 2167979908869652946L;

    protected EmbosserType type;

    private double maxPageWidth = Double.MAX_VALUE;
    private double maxPageHeight = Double.MAX_VALUE;
    private double minPageWidth = 50d;
    private double minPageHeight = 50d;

    private int marginInner = 0;
    private int marginOuter = 0;
    private int marginTop = 0;
    private int marginBottom = 0;

    private int minMarginInner = 0;
    private int minMarginOuter = 0;
    private int minMarginTop = 0;
    private int minMarginBottom = 0;

    private int maxMarginInner = 0;
    private int maxMarginOuter = 0;
    private int maxMarginTop = 0;
    private int maxMarginBottom = 0;

    protected boolean duplexEnabled = false;

    private static final TableFilter tableFilter;
    private static final String table6dot = "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US";
    //private static final String table8dot = "";

    static {
        tableFilter = new TableFilter() {
            @Override
            public boolean accept(FactoryProperties object) {
                if (object == null) {
                    return false;
                }
                String tableID = object.getIdentifier();
                if (tableID.equals(table6dot)) {
                    return true;
                }
                //if (tableID.equals(table8dot))  { return true; }
                return false;
            }
        };
    }

    public EnablingTechnologiesEmbosser(TableCatalogService service, EmbosserType props) {

        super(service, props);

        type = props;

        setTable = service.newTable(table6dot);

        minPageWidth = 1.5 * EmbosserTools.INCH_IN_MM;
        minPageHeight = 3 * EmbosserTools.INCH_IN_MM;
        maxPageHeight = 14 * EmbosserTools.INCH_IN_MM;

        switch (type) {
            case ROMEO_ATTACHE:
            case ROMEO_ATTACHE_PRO:
                maxPageWidth = 8.5 * EmbosserTools.INCH_IN_MM;
                break;
            case ROMEO_PRO_LE_NARROW:
                maxPageWidth = 8.5 * EmbosserTools.INCH_IN_MM;
                minPageHeight = 0.5 * EmbosserTools.INCH_IN_MM;
                maxPageHeight = 4 * EmbosserTools.INCH_IN_MM;
                break;
            case ROMEO_25:
            case ROMEO_PRO_50:
            case THOMAS:
            case THOMAS_PRO:
            case MARATHON:
            case ET:
            case JULIET_PRO_60:
            case BOOKMAKER:
            case BRAILLE_EXPRESS_100:
            case BRAILLE_EXPRESS_150:
                maxPageWidth = 13.25 * EmbosserTools.INCH_IN_MM;
                break;
            case ROMEO_PRO_LE_WIDE:
                maxPageWidth = 13.25 * EmbosserTools.INCH_IN_MM;
                minPageHeight = 0.5 * EmbosserTools.INCH_IN_MM;
                maxPageHeight = 4 * EmbosserTools.INCH_IN_MM;
                break;
            case JULIET_PRO:
            case JULIET_CLASSIC:
                maxPageWidth = 15 * EmbosserTools.INCH_IN_MM;
                break;
            case BRAILLE_PLACE:
                minPageWidth = 11.5 * EmbosserTools.INCH_IN_MM;
                maxPageWidth = 11.5 * EmbosserTools.INCH_IN_MM;
                minPageHeight = 11 * EmbosserTools.INCH_IN_MM;
                maxPageHeight = 11 * EmbosserTools.INCH_IN_MM;
                break;
            default:
                throw new IllegalArgumentException("Unsupported embosser type");
        }

        maxMarginTop = (int) Math.floor(9.9 * EmbosserTools.INCH_IN_MM / getCellHeight());

        marginInner = Math.min(maxMarginInner, Math.max(minMarginInner, marginInner));
        marginOuter = Math.min(maxMarginOuter, Math.max(minMarginOuter, marginOuter));
        marginTop = Math.min(maxMarginTop, Math.max(minMarginTop, marginTop));
        marginBottom = Math.min(maxMarginBottom, Math.max(minMarginBottom, marginBottom));

    }

    @Override
    protected double getCellWidth() {
        return 0.24 * EmbosserTools.INCH_IN_MM;
    }

    @Override
    protected double getCellHeight() {
        return 0.4 * EmbosserTools.INCH_IN_MM;
    }

    @Override
    public boolean supportsPaper(Paper paper) {
        if (paper == null) {
            return false;
        }
        try {
            SheetPaper p = paper.asSheetPaper();
            if (supportsPageFormat(new SheetPaperFormat(p, Orientation.DEFAULT))) {
                return true;
            }
            if (supportsPageFormat(new SheetPaperFormat(p, Orientation.REVERSED))) {
                return true;
            }
        } catch (ClassCastException e) {
        }
        return false;
    }

    @Override
    public boolean supportsPageFormat(PageFormat format) {
        if (format == null) {
            return false;
        }
        try {
            return supportsPrintPage(getPrintPage(format.asSheetPaperFormat()));
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean supportsPrintPage(PrintPage dim) {
        if (dim == null) {
            return false;
        }
        return (dim.getWidth() <= maxPageWidth) &&
                (dim.getWidth() >= minPageWidth) &&
                (dim.getHeight() <= maxPageHeight) &&
                (dim.getHeight() >= minPageHeight);
    }

    @Override
    public TableFilter getTableFilter() {
        return tableFilter;
    }

    @Override
    public boolean supportsVolumes() {
        return false;
    }

    @Override
    public boolean supports8dot() {
        return false;
    }

    @Override
    public boolean supportsAligning() {
        return true;
    }

    @Override
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

    @Override
    public EmbosserWriter newEmbosserWriter(OutputStream os) {

        boolean eightDots = supports8dot() && false;       // examine PEF file: rowgap / char > 283F
        PageFormat page = getPageFormat();

        if (!supportsPageFormat(page)) {
            throw new IllegalArgumentException("Unsupported paper");
        }

        byte[] header = getHeader(duplexEnabled, eightDots);
        byte[] footer = new byte[0];

        ConfigurableEmbosser.Builder b = new ConfigurableEmbosser.Builder(os, setTable.newBrailleConverter())
                .breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
                .padNewline(ConfigurableEmbosser.Padding.NONE)
                .footer(footer)
                .embosserProperties(
                        SimpleEmbosserProperties.with(getMaxWidth(page), getMaxHeight(page))
                                .supportsDuplex(duplexEnabled)
                                .supportsAligning(supportsAligning())
                                .build()
                )
                .header(header);
        return b.build();
    }

    private byte[] getHeader(boolean duplex,
                             boolean eightDots) {

        PageFormat page = getPageFormat();
        int cellsPerLine = getMaxWidth(page);
        int linesPerPage = getMaxHeight(page);
        double paperLenght = getPrintPage(page).getHeight();

        int pageLenght = (int) Math.ceil(paperLenght / EmbosserTools.INCH_IN_MM - 3);
        int topOffFormOffset = (int) Math.ceil(marginTop * getCellHeight() / EmbosserTools.INCH_IN_MM * 10);

        StringBuffer header = new StringBuffer();

        header.append(new char[]{0x1b, 0x00});                                                   // Reset system
        header.append(new char[]{0x1b, 0x01});
        header.append(new char[]{0x00, 0x00});             // Alpha character set
        header.append(new char[]{0x1b, 0x0b});
        header.append((char) (eightDots ? 1 : 0));             // 6/8 dot braille mode
        header.append(new char[]{0x1b, 0x0c});
        header.append((char) (1 + marginInner));             // Left margin
        header.append(new char[]{0x1b, 0x0f});
        header.append((char) 1);                           // Paper sensor = ON
        header.append(new char[]{0x1b, 0x11});
        header.append((char) linesPerPage);                // Lines per page
        header.append(new char[]{0x1b, 0x12});
        header.append((char) (cellsPerLine + marginInner));  // Right margin
        header.append(new char[]{0x1b, 0x14});
        header.append((char) pageLenght);                  // Page length
        header.append(new char[]{0x1b, 0x17});
        header.append((char) 0);                           // Word wrap = OFF
        header.append(new char[]{0x1b, 0x23});
        header.append((char) 0);                           // Horizontal page centering = OFF
        if (supportsDuplex()) {
            header.append(new char[]{0x1b, 0x29});
            header.append((char) (duplex ? 0 : 1));
        }          // Interpoint mode
        header.append(new char[]{0x1b, 0x33});
        header.append((char) (eightDots ? 3 : 0));             // DBS mode
        header.append(new char[]{0x1b, 0x34});
        header.append((char) topOffFormOffset);            // Top of form offset

        return header.toString().getBytes();
    }

    @Override
    public Area getPrintableArea(PageFormat pageFormat) {

        PrintPage printPage = getPrintPage(pageFormat);

        double cellWidth = getCellWidth();
        double cellHeight = getCellHeight();

        return new Area(printPage.getWidth() - (marginInner + marginOuter) * cellWidth,
                printPage.getHeight() - (marginTop + marginBottom) * cellHeight,
                marginInner * cellWidth,
                marginTop * cellHeight);
    }
}
