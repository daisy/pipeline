package com_braillo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.daisy.braille.api.embosser.Device;
import org.daisy.braille.api.embosser.EmbosserFactoryException;
import org.daisy.braille.api.embosser.EmbosserFeatures;
import org.daisy.braille.api.embosser.EmbosserWriter;
import org.daisy.braille.api.embosser.EmbosserWriterProperties;
import org.daisy.braille.api.embosser.PrintPage;
import org.daisy.braille.api.embosser.StandardLineBreaks;
import org.daisy.braille.api.embosser.UnsupportedPaperException;
import org.daisy.braille.api.embosser.PrintPage.PrintDirection;
import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.paper.Dimensions;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.api.paper.Paper;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.api.table.TableCatalogService;
import org.daisy.braille.impl.embosser.ConfigurableEmbosser;
import org.daisy.braille.impl.embosser.EmbosserTools;
import org.daisy.braille.impl.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.impl.embosser.SimpleEmbosserProperties;

/**
 * Provides an Embosser for Braillo 300
 * 
 * @author alra
 *
 */
public class Braillo300Embosser extends BrailloEmbosser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8583526053587599469L;
	private boolean zFoldingEnabled;
	private final static String EMBOSSER_FEATURE_FALLBACK = "fallback";
	private final static String EMBOSSER_FEATURE_REPLACEMENT = "replacement";
	private final static String TEMPORAL_EXTENSION_FILE = ".tmp";
	private final static String EMBOSSER_UNSUPPORTED_MESSAGE = "Unsupported paper for embosser ";
	private final static String EMBOSSER_UNSUPPORTED_Z_FOLDING = "Unsupported value for z-folding.";
	private final static String EMBOSSER_UNSUPPORTED_FEATURE = "Embosser does not support this feature.";
	private final static String HEADER_TOO_WIDE_HIGH_MSG = "Paper too wide or high: ";
	private final static String HEADER_TOO_NARROW_SHORT_MSG = "Paper too wide or high: ";
	private final static String HEADER_INCHES_MSG = " inches.";
	private final static String HEADER_CHARS_MSG = " chars x ";
	

	/**
	 * 
	 * @param service
	 * @param props
	 */
	public Braillo300Embosser(TableCatalogService service, FactoryProperties props) {
		super(service, props);
		this.zFoldingEnabled = false;
	}

	@Override
	public boolean supportsPrintPage(PrintPage printPage) {
		int height = (int)Math.ceil(printPage.getHeight()/EmbosserTools.INCH_IN_MM);
		int width = EmbosserTools.getWidth(printPage, getCellWidth());
		if (width < 140) {
			return false;
		}
		if (height < 4 || height > 14) { 
			return false; 
		}
		return true;
	}

	@Override
	public boolean supportsPageFormat(PageFormat pageFormat) {
		return pageFormat.getPageFormatType() == PageFormat.Type.TRACTOR
				&& pageFormat.asTractorPaperFormat().getLengthAcrossFeed().asInches() >= 4
				&& pageFormat.asTractorPaperFormat().getLengthAcrossFeed().asInches() <= 14
				&& pageFormat.asTractorPaperFormat().getLengthAlongFeed().asMillimeter() >= 140
				&& pageFormat.asTractorPaperFormat().getLengthAlongFeed().asMillimeter() <= 330;
	}

	@Override
	public boolean supportsPaper(Paper paper) {
		return paper.getType() == Paper.Type.TRACTOR
				&& paper.asTractorPaper().getLengthAcrossFeed().asInches() >= 4
				&& paper.asTractorPaper().getLengthAcrossFeed().asInches() <= 14
				&& paper.asTractorPaper().getLengthAlongFeed().asMillimeter() >= 140
				&& paper.asTractorPaper().getLengthAlongFeed().asMillimeter() <= 330;
	}

	@Override
	public EmbosserWriter newEmbosserWriter(OutputStream os) {
		try {
			Table tc = tableCatalogService.newTable(setTable.getIdentifier());
			tc.setFeature(EMBOSSER_FEATURE_FALLBACK, getFeature(EMBOSSER_FEATURE_FALLBACK));
			tc.setFeature(EMBOSSER_FEATURE_REPLACEMENT, getFeature(EMBOSSER_FEATURE_REPLACEMENT));
			PrintPage printPage = getPrintPage(getPageFormat());
			
			EmbosserWriterProperties ep = new SimpleEmbosserProperties(Math.min(EmbosserTools.getWidth(printPage, getCellWidth()), 42), 
																	   EmbosserTools.getHeight(printPage, getCellHeight())).supportsDuplex(true).supportsAligning(true);
			
			ConfigurableEmbosser.Builder b = new ConfigurableEmbosser.Builder(os, tc.newBrailleConverter())
																	 .breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
																	 .padNewline(ConfigurableEmbosser.Padding.NONE)
																	 .embosserProperties(ep)
																	 .header(getBrailloHeader(ep.getMaxWidth(), printPage))
																	 .fillSheet(true)
																	 .autoLineFeedOnEmptyPage(true);
			return b.build();
		} 
		catch (EmbosserFactoryException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * This specific features apply to Braillo 300 Printer. For more information refer 
	 * to "Brochure B300.pdf" available at <http://www.braillo.com/products.php>
	 * 
	 * @param width
	 * @param pageFormat
	 * @return
	 * @throws UnsupportedPaperException
	 */
	private byte[] getBrailloHeader(int width, Dimensions pageFormat) throws UnsupportedPaperException {
		// Round to the closest possible higher value, so that all characters fit on the page
		int height = (int)Math.ceil(2*pageFormat.getHeight()/EmbosserTools.INCH_IN_MM);
		//System.err.println(height);
		if (width > 330 || height > 14) { 
			throw new UnsupportedPaperException(HEADER_TOO_WIDE_HIGH_MSG + width + HEADER_CHARS_MSG + height / 2d + HEADER_INCHES_MSG); 
		}
		if (width < 140 || height < 4) {
			throw new UnsupportedPaperException(HEADER_TOO_NARROW_SHORT_MSG + width + HEADER_CHARS_MSG + height / 2d + HEADER_INCHES_MSG);
		}
		
		byte[] w = EmbosserTools.toBytes(width, 2);
		byte[] h = EmbosserTools.toBytes(height, 2);
		return new byte[] {
			
			0x1b, 'A', h[0], h[1],	// Sheet length. nn can be from 08 to 28 (4 to14 inches).
			0x1b, 'B', w[0], w[1],	// Line length. nn can be from 10 to 42 characters.
			0x1b, 'C', '1', 		// Print Format. n can be 0 or 1, single-sided (0) or interpoint (1).
			
			0x1b, 'H', (byte)(zFoldingEnabled?'1':'0'), // Page Layout. n can be 0 or 1, Normal (0) or Z-fold printing (1).
			0x1b, 'I', '0',			// Page 1 up / down. n can be 0 or 1, up (0) or down (1).
			0x1b, 'J', '0',			// 6 / 8 dot braille. n can be 0 or 1, 6 (0) or 8 (1) dot Braille.
			
			0x1b, 'M', '1', '6',	// Line spacing. nn can be from 0 to 16.
			0x1b, 'N', '0',			// Line single / double. n can be 0 or 1, single (0) or double line spacing (1).
			0x1b, 'R', '0',			// Page adjust. n can be from 0 to 9 lines
			
			0x1b, 'S', '1',			// Form feed mode. n can be 0 or 1, form feed (0) or normal form feed (1).
		};
	}

	@Override
	public EmbosserWriter newEmbosserWriter(Device device) {
		if (!supportsPrintPage(getPrintPage(getPageFormat()))) {
			throw new IllegalArgumentException(EMBOSSER_UNSUPPORTED_MESSAGE + getDisplayName());
		}
		try {
			File f = File.createTempFile(this.getClass().getCanonicalName(), TEMPORAL_EXTENSION_FILE);
			f.deleteOnExit();
			EmbosserWriter ew = newEmbosserWriter(new FileOutputStream(f));
			return new FileToDeviceEmbosserWriter(ew, f, device);
		} catch (IOException e) {
			// do nothing, fail
		}
		throw new IllegalArgumentException(EMBOSSER_UNSUPPORTED_FEATURE);
	}

	@Override
	public PrintPage getPrintPage(PageFormat pageFormat) {
		return new PrintPage(pageFormat, PrintDirection.UPRIGHT, PrintMode.REGULAR);
	}

	@Override
	public void setFeature(String key, Object value) {
		if (EmbosserFeatures.Z_FOLDING.equals(key)) {
            try {
                zFoldingEnabled = (Boolean)value;
            } 
            catch (ClassCastException e) {
                throw new IllegalArgumentException(EMBOSSER_UNSUPPORTED_Z_FOLDING);
            }
		} 
		else {
			super.setFeature(key, value);
		}
	}
	
	@Override
	public boolean supportsVolumes() {
		return true;
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
	public boolean supportsZFolding() {
		return true;
	}

	@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return mode == PrintMode.REGULAR;
	}
}