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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.daisy.braille.embosser.AbstractEmbosser;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserTools;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.paper.Area;
import org.daisy.paper.PageFormat;
import org.daisy.paper.Paper;
import org.daisy.paper.PrintPage;
import org.daisy.paper.PrintPage.PrintDirection;
import org.daisy.paper.SheetPaper;
import org.daisy.paper.SheetPaperFormat;
import org.daisy.paper.SheetPaperFormat.Orientation;
import org.daisy.paper.TractorPaperFormat;
import org.daisy.printing.Device;

import com_indexbraille.IndexEmbosserProvider.EmbosserType;

public abstract class IndexEmbosser extends AbstractEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4942671719606902452L;

	protected EmbosserType type;

    private double minPageLengthAlongFeed = 50d;
    private double maxPageLengthAlongFeed = Double.MAX_VALUE;
    private double minPageLengthAcrossFeed = 50d;
    private double maxPageLengthAcrossFeed = Double.MAX_VALUE;
    private double minPrintPageWidth = 50d;
    private double maxPrintPageWidth = Double.MAX_VALUE;
    private double minPrintPageHeight = 50d;
    private double maxPrintPageHeight = Double.MAX_VALUE;

    protected int numberOfCopies = 1;
    protected boolean zFoldingEnabled = false;
    protected boolean saddleStitchEnabled = false;
    protected boolean duplexEnabled = false;
    protected boolean eightDotsEnabled = false;

    protected int maxNumberOfCopies = 1;

    protected int marginInner = 0;
    protected int marginOuter = 0;
    protected int marginTop = 0;
    protected int marginBottom = 0;

    protected int minMarginInner = 0;
    protected int minMarginOuter = 0;
    protected int minMarginTop = 0;
    protected int minMarginBottom = 0;

    protected int maxMarginInner = 0;
    protected int maxMarginOuter = 0;
    protected int maxMarginTop = 0;
    protected int maxMarginBottom = 0;
    
    protected int minCellsInWidth = 0;
    protected int minLinesInHeight = 0;
    protected int maxCellsInWidth = Integer.MAX_VALUE;
    protected int maxLinesInHeight = Integer.MAX_VALUE;
  
    public IndexEmbosser(TableCatalogService service, EmbosserType props) {

        super(service, props.getDisplayName(), props.getDescription(), props.getIdentifier());

        type = props;

        setCellWidth(6d);
        setCellHeight(eightDotsEnabled?12.5d:10d);

        switch (type) {
            case INDEX_BASIC_BLUE_BAR:
                maxPrintPageWidth = 280d;
                maxPrintPageHeight = 12*EmbosserTools.INCH_IN_MM;
                break;
            case INDEX_BASIC_S_V2:
            case INDEX_BASIC_D_V2:
                minPrintPageWidth = 138d; // = 23*6
                minPrintPageHeight = 1*EmbosserTools.INCH_IN_MM;
                maxPrintPageHeight = (20+2/3)*EmbosserTools.INCH_IN_MM;
                break;
            case INDEX_EVEREST_D_V2:
                minPrintPageWidth = 138d; // = 23*6
                minPrintPageHeight = 100d;
                maxPrintPageHeight = 350d;
                break;
            case INDEX_4X4_PRO_V2:
                minPrintPageWidth = 138d; // = 23*6
                minPrintPageHeight = 100d;
                maxPrintPageHeight = 297d;
                minPageLengthAlongFeed = 110d;
                maxPageLengthAlongFeed = 500d;
                break;
            case INDEX_BASIC_S_V3:
            case INDEX_BASIC_D_V3:
                minPrintPageWidth = 90d;
                minPrintPageHeight = 1*EmbosserTools.INCH_IN_MM;
                maxPrintPageWidth = 295d;
                maxPrintPageHeight = 17*EmbosserTools.INCH_IN_MM;
                break;
            case INDEX_BASIC_D_V4:
                minPrintPageWidth = 120d;
                minPrintPageHeight = 1*EmbosserTools.INCH_IN_MM;
                maxPrintPageWidth = 325d;
                maxPrintPageHeight = 17*EmbosserTools.INCH_IN_MM;
                break;
            case INDEX_EVEREST_D_V3:
            case INDEX_4X4_PRO_V3:
                minPageLengthAcrossFeed = 130d;
                maxPageLengthAcrossFeed = 297d;
                minPageLengthAlongFeed = 120d;
                maxPageLengthAlongFeed = 585d;
                break;
            case INDEX_EVEREST_D_V4:
                minPageLengthAcrossFeed = 130d;
                maxPageLengthAcrossFeed = 297d;
                minPageLengthAlongFeed = 120d;
                maxPageLengthAlongFeed = 590d;
                break;
            case INDEX_4WAVES_PRO_V3:
                minPrintPageWidth = 90d;
                minPrintPageHeight = 11*EmbosserTools.INCH_IN_MM;
                maxPrintPageWidth = 295d;
                maxPrintPageHeight = 12*EmbosserTools.INCH_IN_MM;
                break;
            case INDEX_BRAILLE_BOX_V4:
                break;
            default:
                throw new IllegalArgumentException("Unsupported embosser type");
        }
    }

    @Override
    public boolean supportsPaper(Paper paper) {
        if (paper == null) { return false; }
        try {
            switch (getPaperType()) {
                case SHEET:
                    SheetPaper p = paper.asSheetPaper();
                    if (supportsPageFormat(new SheetPaperFormat(p, Orientation.DEFAULT))) { return true; }
                    if (supportsPageFormat(new SheetPaperFormat(p, Orientation.REVERSED))) { return true; }
                    break;
                case TRACTOR:
                    return supportsPageFormat(new TractorPaperFormat(paper.asTractorPaper()));
				default:
					break;
            }
        } catch (ClassCastException e) {
        }
        return false;
    }

    //jvw1.6@Override
    public boolean supportsPageFormat(PageFormat format) {
        if (format == null) { return false; }
        try {
            switch (getPaperType()) {
                case SHEET:
                    return supportsPrintPage(getPrintPage(format.asSheetPaperFormat()));
                case TRACTOR:
                    return supportsPrintPage(getPrintPage(format.asTractorPaperFormat()));
				default:
					break;
            }
        } catch (ClassCastException e) {
        }
        return false;
    }

    public boolean supportsPrintPage(PrintPage dim) {

        if (dim==null) { return false; }
        double w = dim.getWidth();
        double h = dim.getHeight();
        double across = dim.getLengthAcrossFeed().asMillimeter();
        double along = dim.getLengthAlongFeed().asMillimeter();

        return (w <= maxPrintPageWidth) &&
               (w >= minPrintPageWidth) &&
               (h <= maxPrintPageHeight) &&
               (h >= minPrintPageHeight) &&
               (across <= maxPageLengthAcrossFeed) &&
               (across >= minPageLengthAcrossFeed) &&
               (along <= maxPageLengthAlongFeed) &&
               (along >= minPageLengthAlongFeed);
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
        switch (type) {
            case INDEX_BASIC_D_V2:
            case INDEX_EVEREST_D_V2:
            case INDEX_4X4_PRO_V2:
            case INDEX_BASIC_D_V3:
            case INDEX_EVEREST_D_V3:
            case INDEX_4X4_PRO_V3:
            case INDEX_4WAVES_PRO_V3:
            case INDEX_BASIC_D_V4:
            case INDEX_EVEREST_D_V4:
            case INDEX_BRAILLE_BOX_V4:
                return true;
            case INDEX_BASIC_BLUE_BAR:
            case INDEX_BASIC_S_V2:
            case INDEX_BASIC_S_V3:
            default:
                return false;
        }
    }

    @Override
    public boolean supportsZFolding() {
        switch (type) {
            case INDEX_BASIC_S_V3:
            case INDEX_BASIC_D_V2:
            case INDEX_BASIC_D_V3:
            case INDEX_BASIC_D_V4:
            case INDEX_4WAVES_PRO_V3:
                return true;
            case INDEX_BASIC_BLUE_BAR:
            case INDEX_BASIC_S_V2:
            case INDEX_EVEREST_D_V2:
            case INDEX_EVEREST_D_V3:
            case INDEX_EVEREST_D_V4:
            case INDEX_4X4_PRO_V2:
            case INDEX_4X4_PRO_V3:
            case INDEX_BRAILLE_BOX_V4:
            default:
                return false;
        }
    }

    @Override
    public boolean supportsPrintMode(PrintMode mode) {
        switch (type) {
            case INDEX_4X4_PRO_V2:
            case INDEX_4X4_PRO_V3:
            case INDEX_EVEREST_D_V4:
            case INDEX_BRAILLE_BOX_V4:
                return true;
            case INDEX_BASIC_D_V2:
            case INDEX_EVEREST_D_V2:
            case INDEX_BASIC_D_V3:
            case INDEX_EVEREST_D_V3:
            case INDEX_4WAVES_PRO_V3:
            case INDEX_BASIC_D_V4:
            case INDEX_BASIC_BLUE_BAR:
            case INDEX_BASIC_S_V2:
            case INDEX_BASIC_S_V3:
            default:
                return PrintMode.REGULAR == mode;
        }
    }

    private Paper.Type getPaperType() {
        switch (type) {
            case INDEX_BASIC_BLUE_BAR:
            case INDEX_BASIC_D_V2:
            case INDEX_BASIC_D_V3:
            case INDEX_BASIC_D_V4:
            case INDEX_BASIC_S_V2:
            case INDEX_BASIC_S_V3:
            case INDEX_4WAVES_PRO_V3:
                return Paper.Type.TRACTOR;
            case INDEX_EVEREST_D_V2:
            case INDEX_EVEREST_D_V3:
            case INDEX_EVEREST_D_V4:
            case INDEX_4X4_PRO_V2:
            case INDEX_4X4_PRO_V3:
            case INDEX_BRAILLE_BOX_V4:
            default:
                return Paper.Type.SHEET;
        }
    }

    private PrintDirection getPrintDirection() {
        switch (type) {
            case INDEX_4X4_PRO_V2:
                return PrintDirection.SIDEWAYS;
            case INDEX_4X4_PRO_V3:
              //return saddleStitchEnabled?PrintDirection.SIDEWAYS:PrintDirection.UPRIGHT;
                return PrintDirection.SIDEWAYS;
            case INDEX_EVEREST_D_V4:
                return saddleStitchEnabled?PrintDirection.SIDEWAYS:PrintDirection.UPRIGHT;
          //case INDEX_BASIC_D_V4:
              //return swZFoldingEnabled?PrintDirection.SIDEWAYS:PrintDirection.UPRIGHT;
            case INDEX_BRAILLE_BOX_V4:
                return PrintDirection.SIDEWAYS;
            default:
                return PrintDirection.UPRIGHT;
        }
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

    @Override
    public void setFeature(String key, Object value) {

        if (EmbosserFeatures.TABLE.equals(key)) {
            super.setFeature(key, value);
          //eightDotsEnabled = supports8dot() && setTable.newBrailleConverter().supportsEightDot();
          //setCellHeight(eightDotsEnabled?12.5d:10d);
        } else if (EmbosserFeatures.NUMBER_OF_COPIES.equals(key) && maxNumberOfCopies > 1) {
            try {
                int copies = (Integer)value;
                if (copies < 1 || copies > maxNumberOfCopies) {
                    throw new IllegalArgumentException("Unsupported value for number of copies.");
                }
                numberOfCopies = copies;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for number of copies.");
            }
        } else if (EmbosserFeatures.SADDLE_STITCH.equals(key) && supportsPrintMode(PrintMode.MAGAZINE)) {
            try {
                saddleStitchEnabled = (Boolean)value;
//              if (!(type==EmbosserType.INDEX_EVEREST_D_V4 ||
//                    type==EmbosserType.INDEX_BRAILLE_BOX_V4)) {
                  duplexEnabled = duplexEnabled || saddleStitchEnabled;
//              }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for saddle stitch.");
            }
        } else if (EmbosserFeatures.Z_FOLDING.equals(key) && supportsZFolding()) {
            try {
                zFoldingEnabled = (Boolean)value;
                if (type==EmbosserType.INDEX_BASIC_D_V2) {
                    duplexEnabled = duplexEnabled || zFoldingEnabled;
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for z-folding.");
            }
        } else if (EmbosserFeatures.DUPLEX.equals(key) && supportsDuplex()) {
            try {
                duplexEnabled = (Boolean)value;
//              if (!(type==EmbosserType.INDEX_EVEREST_D_V4 ||
//                    type==EmbosserType.INDEX_BRAILLE_BOX_V4)) {
                  duplexEnabled = duplexEnabled || saddleStitchEnabled;
//              }
                if (type==EmbosserType.INDEX_BASIC_D_V2) {
                    zFoldingEnabled = zFoldingEnabled && duplexEnabled;
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for duplex.");
            }
        } else {
            super.setFeature(key, value);
        }
    }

    @Override
    public Object getFeature(String key) {

        if (EmbosserFeatures.NUMBER_OF_COPIES.equals(key) && maxNumberOfCopies > 1) {
            return numberOfCopies;
        } else if (EmbosserFeatures.SADDLE_STITCH.equals(key) && supportsPrintMode(PrintMode.MAGAZINE)) {
            return saddleStitchEnabled;
        } else if (EmbosserFeatures.Z_FOLDING.equals(key) && supportsZFolding()) {
            return zFoldingEnabled;
        } else if (EmbosserFeatures.DUPLEX.equals(key) && supportsDuplex()) {
            return duplexEnabled;
        } else {
            return super.getFeature(key);
        }
    }

    @Override
    public PrintPage getPrintPage(PageFormat pageFormat) {
        PrintMode mode = saddleStitchEnabled?PrintMode.MAGAZINE:PrintMode.REGULAR;
        PrintDirection direction = getPrintDirection();
        return new PrintPage(pageFormat, direction, mode);
    }

    @Override
    public Area getPrintableArea(PageFormat pageFormat) {

        Area maxArea = getPrintArea(pageFormat);

        double cellWidth = getCellWidth();
        double cellHeight = getCellHeight();
        
     /* marginInner =  Math.min(maxMarginInner,  Math.max(minMarginInner,  marginInner));
        marginOuter =  Math.min(maxMarginOuter,  Math.max(minMarginOuter,  marginOuter));
        marginTop =    Math.min(maxMarginTop,    Math.max(minMarginTop,    marginTop));
        marginBottom = Math.min(maxMarginBottom, Math.max(minMarginBottom, marginBottom)); */

        return new Area(maxArea.getWidth() - (marginInner + marginOuter) * cellWidth,
                        maxArea.getHeight() - (marginTop + marginBottom) * cellHeight,
                        maxArea.getOffsetX() + marginInner * cellWidth,
                        maxArea.getOffsetY() + marginTop * cellHeight);
    }

    protected Area getPrintArea(PageFormat pageFormat) {

        PrintPage printPage = getPrintPage(pageFormat);

        double cellWidth = getCellWidth();
        double cellHeight = getCellHeight();
        double lengthAcrossFeed = printPage.getLengthAcrossFeed().asMillimeter();

        double printablePageWidth = printPage.getWidth();
        double printablePageHeight = printPage.getHeight();

        switch (type) {
            case INDEX_4X4_PRO_V2:
                printablePageHeight = Math.min(lengthAcrossFeed, 248.5);
                break;
            case INDEX_BASIC_D_V4:
                printablePageWidth = Math.min(lengthAcrossFeed, 301.152);
                break;
            case INDEX_EVEREST_D_V2:
            case INDEX_EVEREST_D_V3:
            case INDEX_BASIC_S_V2:
            case INDEX_BASIC_D_V2:
            case INDEX_BASIC_S_V3:
            case INDEX_BASIC_D_V3:
            case INDEX_4WAVES_PRO_V3:
                printablePageWidth = Math.min(lengthAcrossFeed, 248.5);
                break;
			default:
				break;
        }

        printablePageWidth  = Math.min(printablePageWidth,  maxCellsInWidth  * cellWidth);
        printablePageHeight = Math.min(printablePageHeight, maxLinesInHeight * cellHeight);

        double unprintableInner = 0;
        double unprintableTop = 0;

        switch (type) {
            case INDEX_BASIC_S_V3:
            case INDEX_BASIC_D_V3:
                unprintableInner = Math.max(0, lengthAcrossFeed - 276.4);
                break;
            case INDEX_EVEREST_D_V3:
                unprintableInner = Math.max(0, lengthAcrossFeed - 272.75);
                break;
			default:
				break;
        }

        return new Area(printablePageWidth, printablePageHeight, unprintableInner, unprintableTop);
    }
}
