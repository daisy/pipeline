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
package org.daisy.braille.utils.impl.provider.braillo;

import org.daisy.braille.utils.impl.tools.embosser.ConfigurableEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.EmbosserTools;
import org.daisy.braille.utils.impl.tools.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.utils.impl.tools.embosser.SimpleEmbosserProperties;
import org.daisy.dotify.api.embosser.Device;
import org.daisy.dotify.api.embosser.EmbosserFactoryException;
import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.PrintPage;
import org.daisy.dotify.api.embosser.PrintPage.PrintDirection;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.embosser.UnsupportedPaperException;
import org.daisy.dotify.api.paper.Area;
import org.daisy.dotify.api.paper.Dimensions;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableCatalogService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Provides an Embosser for Braillo 200/400S/400SR.
 *
 * @author Joel Håkansson
 */
public abstract class AbstractBraillo200Embosser extends BrailloEmbosser {

    private static final long serialVersionUID = 2846669580514452253L;
    private boolean zFoldingEnabled;

    @Override
    public boolean supportsPrintPage(PrintPage dim) {
        int height = (int) Math.ceil(2 * dim.getHeight() / EmbosserTools.INCH_IN_MM);
        int width = EmbosserTools.getWidth(dim, 6);
        // removed upper bounds check (width > 42), since paper might be larger than printable area
        if (height > 28) {
            return false;
        }
        if (width < 10 || height < 8) {
            return false;
        }
        return true;
    }

    /**
     * Creates a new Braillo 200 embosser.
     *
     * @param service the table catalog
     * @param props   the embosser properties
     */
    public AbstractBraillo200Embosser(TableCatalogService service, EmbosserFactoryProperties props) {
        super(service, props);
        this.zFoldingEnabled = false;
    }

    @Override
    public EmbosserWriter newEmbosserWriter(OutputStream os) {
        try {
            Table tc = tableCatalogService.newTable(setTable.getIdentifier());
            tc.setFeature("fallback", getFeature("fallback"));
            tc.setFeature("replacement", getFeature("replacement"));
            PrintPage printPage = getPrintPage(getPageFormat());
            SimpleEmbosserProperties ep = SimpleEmbosserProperties.with(
                    Math.min(EmbosserTools.getWidth(printPage, getCellWidth()), 42),
                    EmbosserTools.getHeight(printPage, getCellHeight()))
                    .supportsDuplex(true)
                    .supportsAligning(true)
                    .build();
            ConfigurableEmbosser.Builder b = new ConfigurableEmbosser.Builder(os, tc.newBrailleConverter())
                    .breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
                    .padNewline(ConfigurableEmbosser.Padding.NONE) // JH100408: changed from BEFORE
                    .embosserProperties(ep)
                    .header(getBrailloHeader(ep.getMaxWidth(), printPage))
                    .fillSheet(true)
                    .autoLineFeedOnEmptyPage(true);
            return b.build();
        } catch (EmbosserFactoryException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public EmbosserWriter newEmbosserWriter(Device device) {
        if (!supportsPrintPage(getPrintPage(getPageFormat()))) {
            throw new IllegalArgumentException("Unsupported paper for embosser " + getDisplayName());
        }
        try {
            File f = File.createTempFile(this.getClass().getCanonicalName(), ".tmp");
            f.deleteOnExit();
            EmbosserWriter ew = newEmbosserWriter(new FileOutputStream(f));
            return new FileToDeviceEmbosserWriter(ew, f, device);
        } catch (IOException e) {
            // do nothing, fail
        }
        throw new IllegalArgumentException("Embosser does not support this feature.");
    }

    // B200, B400S, B400SR
    // Supported paper width (chars): 10 <= width <= 42
    // Supported paper height (inches): 4 <= height <= 14
    private byte[] getBrailloHeader(int width, Dimensions pageFormat) throws UnsupportedPaperException {
        // Round to the closest possible higher value, so that all characters fit on the page
        int height = (int) Math.ceil(2 * pageFormat.getHeight() / EmbosserTools.INCH_IN_MM);
        if (width > 42 || height > 28) {
            throw new UnsupportedPaperException(
                "Paper too wide or high: " + width + " chars x " + height / 2d + " inches."
            );
        }
        if (width < 10 || height < 8) {
            throw new UnsupportedPaperException(
                "Paper too narrow or short: " + width + " chars x " + height / 2d + " inches."
            );
        }
        byte[] w = EmbosserTools.toBytes(width, 2);
        byte[] h = EmbosserTools.toBytes(height, 2);
        return new byte[]{
                0x1b, 'S', '1',        // Form Feed Mode. n can be 0 or 1, no form feed (0) or normal form feed (1).
                0x1b, 'C', '1',        // Print Format. n can be 0 or 1, single-sided (0) or interpoint (1).
                0x1b, 'J', '0',            // 6 / 8 dot braille. n can be 0 or 1, 6 (0) or 8 (1) dot braille.
                0x1b, 'A', h[0], h[1],    // Sheet length. nn can be from 08 to 28 (4 to14 inch)
                0x1b, 'B', w[0], w[1],    // Line length. nn can be from 10 to 42 characters
                0x1b, 'H', (byte) (zFoldingEnabled ? '1' : '0'),
                                        // Page Layout. n can be 0 or 1, Normal (0) or Z-fold printing (1).
                0x1b, 'N', '0',            // Line Single/Double. n can be 0 or 1, single (0) or double line spacing (1)
                0x1b, 'R', '0'            // Page adjust. n can be from 0 to 9 lines
        };
    }

    @Override
    public void setFeature(String key, Object value) {
        if (EmbosserFeatures.Z_FOLDING.equals(key)) {
            try {
                zFoldingEnabled = (Boolean) value;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for z-folding.");
            }
        } else {
            super.setFeature(key, value);
        }
    }

    @Override
    public boolean supports8dot() {
        return false;
    }

    @Override
    public boolean supportsDuplex() {
        return true;
    }

    @Override
    public boolean supportsAligning() {
        return true;
    }

    @Override
    public boolean supportsVolumes() {
        return false;
    }

    @Override
    public Area getPrintableArea(PageFormat pageFormat) {
        PrintPage printPage = getPrintPage(pageFormat);
        return new Area(printPage.getWidth(), printPage.getHeight(), 0, 0);
    }

    @Override
    public PrintPage getPrintPage(PageFormat pageFormat) {
        return new PrintPage(pageFormat, PrintDirection.UPRIGHT, PrintMode.REGULAR);
    }

    @Override
    public boolean supportsZFolding() {
        // even 400SR supports z-folding! But since it uses roll paper,
        // that option will not be presented to a user at this moment.
        return true;
    }

    @Override
    public boolean supportsPrintMode(PrintMode mode) {
        return mode == PrintMode.REGULAR;
    }
}
