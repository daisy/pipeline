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
package be_interpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.daisy.braille.api.paper.Area;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.api.paper.Paper;
import org.daisy.braille.embosser.AbstractEmbosser;
import org.daisy.braille.embosser.AbstractEmbosserWriter.Padding;
import org.daisy.braille.embosser.ConfigurableEmbosser;
import org.daisy.braille.embosser.Device;
import org.daisy.braille.embosser.EmbosserFactoryException;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.EmbosserWriterProperties;
import org.daisy.braille.embosser.PrintPage;
import org.daisy.braille.embosser.PrintPage.PrintDirection;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.embosser.StandardLineBreaks;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.braille.table.TableFilter;
import org.daisy.factory.FactoryProperties;

/**
 *
 * @author Bert Frees
 */
public class Interpoint55Embosser extends AbstractEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6878058097412223168L;
	private final static PrintDirection PRINT_DIRECTION = PrintDirection.SIDEWAYS;
    private final static TableFilter tableFilter;
    private final static String table_US1 =     "org_daisy.EmbosserTableProvider.TableType.MIT";
    private final static String table_US2 =     "org_daisy.EmbosserTableProvider.TableType.NABCC";
    private final static String table_DE =      "org_daisy.EmbosserTableProvider.TableType.DE_DE";
  //private final static String table_US_8dot = "be_interpoint.InterpointTableProvider.TableType.USA1_8";

    static {
        tableFilter = new TableFilter() {
            @Override
            public boolean accept(FactoryProperties object) {
                if (object == null) { return false; }
                String tableID = object.getIdentifier();
                if (tableID.equals(table_US1))      { return true; }
                if (tableID.equals(table_US2))      { return true; }
                if (tableID.equals(table_DE))       { return true; }
              //if (tableID.equals(table_US_8dot))  { return true; }
                return false;
            }
        };
    }

    private double maxRollWidth = 340d;
    private double minRollWidth = 50d;              // ???
    private double maxCutLength = Double.MAX_VALUE; // ???
    private double minCutLength = 50d;              // ???

    private int marginInner = 0;
    private int marginOuter = 0;
    private int marginTop = 0;
    private int marginBottom = 0;

    private boolean saddleStitchEnabled = false;
    private boolean duplexEnabled = true;
    private boolean eightDotsEnabled = false;
    private int maxPagesInQuire = 0;                  // 0 == no quires
    private int numberOfCopies = 1;

    public Interpoint55Embosser(TableCatalogService service, FactoryProperties props) {

        super(service, props.getDisplayName(), props.getDescription(), props.getIdentifier());

        setTable = service.newTable(table_US1);

        setCellWidth(6d);
        setCellHeight(eightDotsEnabled?12.5d:10d);
    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    @Override
    public boolean supportsPaper(Paper paper) {
        if (paper == null) { return false; }
        try {
            double across = paper.asRollPaper().getLengthAcrossFeed().asMillimeter();
            return (across <= maxRollWidth) && (across >= minRollWidth);
        } catch (ClassCastException e) {
            return false;
        }
    }

    //jvw1.6@Override
    public boolean supportsPageFormat(PageFormat format) {
        if (format == null) { return false; }
        try {
            return supportsPrintPage(getPrintPage(format.asRollPaperFormat()));
        } catch (ClassCastException e) {
            return false;
        }
    }

    public boolean supportsPrintPage(PrintPage dim) {
        if (dim==null) { return false; }
        double across = dim.getLengthAcrossFeed().asMillimeter();
        double along = dim.getLengthAlongFeed().asMillimeter();
        return (across <= maxRollWidth) &&
               (across >= minRollWidth) &&
               (along <= maxCutLength) &&
               (along >= minCutLength);
    }

    public boolean supportsVolumes() {
        return false;
    }

    public boolean supports8dot() {
        return false;
    }

    public boolean supportsDuplex() {
        return true;
    }

    public boolean supportsAligning() {
        return true;
    }

    @Override
    public boolean supportsZFolding() {
        return false;
    }

    @Override
    public boolean supportsPrintMode(PrintMode mode) {
        return true;
    }

    public EmbosserWriter newEmbosserWriter(OutputStream os) {

        PageFormat page = getPageFormat();
        if (!supportsPageFormat(page)) {
            throw new IllegalArgumentException("Unsupported paper");
        }

        EmbosserWriterProperties props =
            new SimpleEmbosserProperties(getMaxWidth(page), getMaxHeight(page))
                .supports8dot(eightDotsEnabled)
                .supportsDuplex(duplexEnabled)
                .supportsAligning(supportsAligning());

        return new ConfigurableEmbosser.Builder(os, setTable.newBrailleConverter())
                            .breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
                            .padNewline(Padding.NONE)
                            .embosserProperties(props)
                            .build();
    }

    public void loadConfigurationFile(File file)
                               throws FileNotFoundException,
                                      IOException {

        Properties properties = new Properties();
        String property;

        InputStream is = new FileInputStream(file);
        properties.load(is);
        if (is != null) { is.close(); }

        if ((property = properties.getProperty("Mode")) != null) {
            if (property.equals("1")) {
                duplexEnabled = false;
                saddleStitchEnabled = false;
            } else if (property.equals("3")) {
                duplexEnabled = true;
                saddleStitchEnabled = false;
            } else if (property.equals("4")) {
                duplexEnabled = true;
                saddleStitchEnabled = true;
            }
        }
        if ((property = properties.getProperty("Copies")) != null) {
            try {
                setFeature(EmbosserFeatures.NUMBER_OF_COPIES, Integer.parseInt(property));
            } catch (Exception e) {
            }
        }
        if ((property = properties.getProperty("MaxPagesInQuire")) != null) {
            try {
                setFeature(EmbosserFeatures.PAGES_IN_QUIRE, Integer.parseInt(property));
            } catch (Exception e) {
            }
        }
    }

    public void saveConfigurationFile(File file)
                               throws FileNotFoundException,
                                      IOException {

        Properties properties = new Properties();

        InputStream is = new FileInputStream(file);
        properties.load(is);
        if (is != null) { is.close(); }

        PageFormat page = getPageFormat();
        if (!supportsPageFormat(page)) {
            throw new IllegalArgumentException("Unsupported paper");
        }

        String tableID = setTable.getIdentifier();

        properties.setProperty("TableName",         tableID.equals(table_DE)      ?"WIEN_6":
                                                  //tableID.equals(table_US_8dot) ?"USA1_8":
                                                                                   "USA1_6");
        properties.setProperty("Mode",              saddleStitchEnabled?"4":duplexEnabled?"3":"1");
        properties.setProperty("MirrorMargins",     "1");
        properties.setProperty("CharactersPerLine", String.valueOf(getMaxWidth(page)));
        properties.setProperty("LinesPerPage",      String.valueOf(getMaxHeight(page)));
        properties.setProperty("LeftMargin",        String.valueOf(marginInner));
        properties.setProperty("RightMargin",       String.valueOf(marginOuter));
        properties.setProperty("TopMargin",         String.valueOf(marginTop));
        properties.setProperty("MaxPagesInQuire",   String.valueOf(maxPagesInQuire));
        properties.setProperty("Copies",            String.valueOf(numberOfCopies));

        OutputStream os = new FileOutputStream(file);
        properties.store(os, null);
        if (os != null) { os.close(); }
    }

    public EmbosserWriter newEmbosserWriter(Device device) {

        throw new IllegalArgumentException(new EmbosserFactoryException(getDisplayName() +
                " does not support printing directly to a Device. " +
                "Write the output to a file with newEmbosserWriter(OutputStream), " +
                "save the settings to a file with saveConfigurationFile(File) " +
                "and then use the wprint55 software to emboss."));
    }

    public EmbosserWriter newX55EmbosserWriter(OutputStream os) {
        // write to ".x55" file format (special interpoint xml file)
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFeature(String key, Object value) {

        if (EmbosserFeatures.TABLE.equals(key)) {
            super.setFeature(key, value);
            eightDotsEnabled = supports8dot() && setTable.newBrailleConverter().supportsEightDot();
            setCellHeight(eightDotsEnabled?12.5d:10d);
        } else if (EmbosserFeatures.SADDLE_STITCH.equals(key)) {
            try {
                saddleStitchEnabled = (Boolean)value;
                duplexEnabled = duplexEnabled || saddleStitchEnabled;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for saddle stitch.");
            }
        } else if (EmbosserFeatures.DUPLEX.equals(key)) {
            try {
                duplexEnabled = (Boolean)value;
                saddleStitchEnabled = saddleStitchEnabled && duplexEnabled;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for duplex.");
            }
        } else if (EmbosserFeatures.NUMBER_OF_COPIES.equals(key)) {
            try {
                int copies = (Integer)value;
                if (copies < 1) {
                    throw new IllegalArgumentException("Unsupported value for number of copies.");
                }
                numberOfCopies = copies;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for number of copies.");
            }
        } else if (EmbosserFeatures.PAGES_IN_QUIRE.equals(key)) {
            try {
                int pages = (Integer)value;
                if (pages < 0) {
                    throw new IllegalArgumentException("Unsupported value for maximum pages in quire.");
                }
                maxPagesInQuire = pages;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for maximum pages in quire.");
            }
        } else {
            super.setFeature(key, value);
        }
    }

    @Override
    public Object getFeature(String key) {

        if (EmbosserFeatures.SADDLE_STITCH.equals(key)) {
            return saddleStitchEnabled;
        } else if (EmbosserFeatures.DUPLEX.equals(key)) {
            return duplexEnabled;
        } else if (EmbosserFeatures.NUMBER_OF_COPIES.equals(key)) {
            return numberOfCopies;
        } else if (EmbosserFeatures.PAGES_IN_QUIRE.equals(key)) {
            return maxPagesInQuire;
        } else {
            return super.getFeature(key);
        }
    }

    @Override
    public PrintPage getPrintPage(PageFormat pageFormat) {
        PrintMode mode = saddleStitchEnabled?PrintMode.MAGAZINE:PrintMode.REGULAR;
        return new PrintPage(pageFormat, PRINT_DIRECTION, mode);
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
