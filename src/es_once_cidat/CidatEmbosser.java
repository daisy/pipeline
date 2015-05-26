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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.daisy.braille.embosser.AbstractEmbosser;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserTools;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.paper.PageFormat;
import org.daisy.paper.Paper;
import org.daisy.paper.PrintPage;
import org.daisy.paper.SheetPaper;
import org.daisy.paper.SheetPaperFormat;
import org.daisy.paper.SheetPaperFormat.Orientation;
import org.daisy.printing.Device;

import es_once_cidat.CidatEmbosserProvider.EmbosserType;

/**
 *
 * @author Bert Frees
 */
public abstract class CidatEmbosser extends AbstractEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1441333837477401994L;

	protected EmbosserType type;

    private double maxPageWidth = Double.MAX_VALUE;
    private double maxPageHeight = Double.MAX_VALUE;
    private double minPageWidth = 50d;
    private double minPageHeight = 50d;

    protected boolean duplexEnabled = true;
    protected boolean eightDotsEnabled = false;

    public CidatEmbosser(TableCatalogService service, EmbosserType props) {

        super(service, props.getDisplayName(), props.getDescription(), props.getIdentifier());

        type = props;

        setCellWidth(0.25*EmbosserTools.INCH_IN_MM);
        setCellHeight((eightDotsEnabled?0.5:0.4)*EmbosserTools.INCH_IN_MM);
        
        switch (type) {
            case IMPACTO_600:
            case IMPACTO_TEXTO:
                maxPageWidth = 42*getCellWidth();
                maxPageHeight = 13*EmbosserTools.INCH_IN_MM;
                minPageWidth = 12*getCellWidth();
                minPageHeight = 6*EmbosserTools.INCH_IN_MM;
                break;
            case PORTATHIEL_BLUE:
                maxPageWidth = 42*getCellWidth();
                maxPageHeight = 13*EmbosserTools.INCH_IN_MM;
                minPageWidth = 10*getCellWidth();
                minPageHeight = 8*EmbosserTools.INCH_IN_MM;
                break;
            default:
                throw new IllegalArgumentException("Unsupported embosser type");
        }
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

    public boolean supports8dot() {
        return false;
    }

    public boolean supportsDuplex() {
        return true;
    }

    public boolean supportsAligning() {
        return true;
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
          //setCellHeight((eightDotsEnabled?0.5:0.4)*EmbosserTools.INCH_IN_MM);
        } else if (EmbosserFeatures.DUPLEX.equals(key) && supportsDuplex()) {
            try {
                duplexEnabled = (Boolean)value;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for duplex.");
            }
        } else {
            super.setFeature(key, value);
        }
    }

    @Override
    public Object getFeature(String key) {

        if (EmbosserFeatures.DUPLEX.equals(key) && supportsDuplex()) {
            return duplexEnabled;
        } else {
            return super.getFeature(key);
        }
    }
}
