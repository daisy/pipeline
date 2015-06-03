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
package com_braillo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.paper.Area;
import org.daisy.braille.api.paper.Dimensions;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.api.paper.Paper;
import org.daisy.braille.embosser.ConfigurableEmbosser;
import org.daisy.braille.embosser.Device;
import org.daisy.braille.embosser.EmbosserFactoryException;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserTools;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.EmbosserWriterProperties;
import org.daisy.braille.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.embosser.PrintPage;
import org.daisy.braille.embosser.PrintPage.PrintDirection;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.embosser.StandardLineBreaks;
import org.daisy.braille.embosser.UnsupportedPaperException;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableCatalogService;


/**
 * Provides an Embosser for Braillo 200/270/400 firmware 12-16
 * @author Joel Håkansson
 */
public class Braillo200_270_400_v12_16Embosser extends BrailloEmbosser {
	/**
	 * 
	 */
	private static final long serialVersionUID = -793439533297371375L;

	private final static byte[] halfInchToSheetLength = new byte[]{	'0', '1', '1', '2', '2', '3', '3', '4', '4', '5',
																'5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	private boolean zFoldingEnabled;
	
	@Override
	public boolean supportsPrintPage(PrintPage dim) {
		int height = (int)Math.ceil(dim.getHeight()/EmbosserTools.INCH_IN_MM);
		int width = EmbosserTools.getWidth(dim, getCellWidth());
		// removed upper bounds check (width > 42), since paper might be larger than printable area
		if (width < 27) {
			return false;
		}
		if (height < 4 || height > 14) { 
			return false; 
		}
		return true;
	}
	
	public Braillo200_270_400_v12_16Embosser(TableCatalogService service, FactoryProperties props) {
		super(service, props);
		zFoldingEnabled = false;
	}

	@Override
	public EmbosserWriter newEmbosserWriter(OutputStream os) {
		try {
			Table tc = tableCatalogService.newTable(setTable.getIdentifier());
			tc.setFeature("fallback", getFeature("fallback"));
			tc.setFeature("replacement", getFeature("replacement"));
			PrintPage printPage = getPrintPage(getPageFormat());
			EmbosserWriterProperties ep = new SimpleEmbosserProperties(
					Math.min(EmbosserTools.getWidth(printPage, getCellWidth()), 42),
					EmbosserTools.getHeight(printPage, getCellHeight()))
				.supportsDuplex(true)
				.supportsAligning(true);
			ConfigurableEmbosser.Builder b = new ConfigurableEmbosser.Builder(os, tc.newBrailleConverter())
				.breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
				.padNewline(ConfigurableEmbosser.Padding.NONE)
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
	
	// B200, B270, B400
	// Supported paper width (chars): 27 <= width <= 42
	// Supported paper height (inches): 4 <= height <= 14
	private byte[] getBrailloHeader(int width, Dimensions pageFormat) throws UnsupportedPaperException {
		// Round to the closest possible higher value, so that all characters fit on the page
		int height = (int)Math.ceil(2*pageFormat.getHeight()/EmbosserTools.INCH_IN_MM);
		if (width > 42 || height > 28) { 
			throw new UnsupportedPaperException("Paper too wide or high: " + width + " chars x " + height / 2d + " inches."); 
		}
		if (width < 27 || height < 8) {
			throw new UnsupportedPaperException("Paper too narrow or short: " + width + " chars x " + height / 2d + " inches.");
		}
		return new byte[] {
			0x1b, 'E',					// Normal form feed
			0x1b, 'S', '1',				// Print format interpoint
			0x1b, '6',					// 6 dot
			0x1b, 0x1F,	(byte)Integer.toHexString(width-27).toUpperCase().charAt(0),
										// Line length
			0x1b, 0x1E,	halfInchToSheetLength[height-8],
										// Sheet length
			0x1b, 'A',					// Single line spacing
			0x1b, 'Q', (byte)(zFoldingEnabled?'1':'0') // Page Layout. n can be 0 or 1, Normal (0) or Z-fold printing (1)
		};
	}
	
	@Override
	public void setFeature(String key, Object value) {
		if (EmbosserFeatures.Z_FOLDING.equals(key)) {
            try {
                zFoldingEnabled = (Boolean)value;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for z-folding.");
            }
		} else {
			super.setFeature(key, value);
		}
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
	
	public boolean supportsZFolding() {
		return true;
	}

	@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return mode == PrintMode.REGULAR;
	}

	public boolean supportsPageFormat(PageFormat pageFormat) {
		return pageFormat.getPageFormatType() == PageFormat.Type.TRACTOR
		&& pageFormat.asTractorPaperFormat().getLengthAcrossFeed().asInches() >= 4
		&& pageFormat.asTractorPaperFormat().getLengthAcrossFeed().asInches() <= 14
		&& pageFormat.asTractorPaperFormat().getLengthAlongFeed().asInches() >= 4
		&& pageFormat.asTractorPaperFormat().getLengthAlongFeed().asInches() <= 14;
	}

	public boolean supportsPaper(Paper paper) {
		return paper.getType() == Paper.Type.TRACTOR
		&& paper.asTractorPaper().getLengthAcrossFeed().asInches() >= 4
		&& paper.asTractorPaper().getLengthAcrossFeed().asInches() <= 14
		&& paper.asTractorPaper().getLengthAlongFeed().asInches() >= 4
		&& paper.asTractorPaper().getLengthAlongFeed().asInches() <= 14;
	}
}
