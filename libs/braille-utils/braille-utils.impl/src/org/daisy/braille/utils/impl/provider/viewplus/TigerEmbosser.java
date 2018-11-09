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
package org.daisy.braille.utils.impl.provider.viewplus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.daisy.dotify.api.embosser.Device;
import org.daisy.dotify.api.embosser.EmbosserFactoryException;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.PrintPage;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.embosser.UnsupportedPaperException;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.paper.Area;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.Paper;
import org.daisy.dotify.api.paper.SheetPaper;
import org.daisy.dotify.api.paper.SheetPaperFormat;
import org.daisy.dotify.api.paper.SheetPaperFormat.Orientation;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.dotify.api.table.TableFilter;
import org.daisy.braille.utils.impl.provider.viewplus.ViewPlusEmbosserProvider.EmbosserType;
import org.daisy.braille.utils.impl.tools.embosser.AbstractEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.ConfigurableEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.EmbosserTools;
import org.daisy.braille.utils.impl.tools.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.utils.impl.tools.embosser.SimpleEmbosserProperties;

public class TigerEmbosser extends AbstractEmbosser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5130422423415613716L;

	protected EmbosserType type;

	private double maxPageWidth = Double.MAX_VALUE;
	private double maxPageHeight = Double.MAX_VALUE;
	private double minPageWidth = 50d;
	private double minPageHeight = 50d;

	private boolean duplexEnabled = false;

	private static final TableFilter tableFilter;
	private static final String table6dot = "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US";
	//private static final String table8dot = "com_viewplusViewPlusTableProvider.TableType.TIGER_INLINE_SUBSTITUTION_8DOT";

	static {
		tableFilter = new TableFilter() {
			@Override
			public boolean accept(FactoryProperties object) {
				if (object == null) { return false; }
				String tableID = object.getIdentifier();
				if (tableID.equals(table6dot)) { return true; }
				//if (tableID.equals(table8dot)) { return true; }
				return false;
			}
		};
	}

	private static final int maxLinesInHeight = 42;

	private int marginInner = 0;
	private int marginOuter = 0;
	private int marginTop = 0;
	private int marginBottom = 0;

	public TigerEmbosser(TableCatalogService service, EmbosserType props) {

		super(service, props);

		type = props;

		setTable = service.newTable(table6dot);

		minPageWidth = 176d;  // B5
		minPageHeight = 250d;

		switch (type) {
		case PREMIER_80:
		case PREMIER_100:
		case ELITE_150:
		case ELITE_200:
			maxPageWidth = 12*EmbosserTools.INCH_IN_MM;
			maxPageHeight = 22*EmbosserTools.INCH_IN_MM;
			break;
		case PRO_GEN_II:
			maxPageWidth = 16*EmbosserTools.INCH_IN_MM;
			maxPageHeight = 22*EmbosserTools.INCH_IN_MM;
			break;
		case CUB:
		case CUB_JR:
		case EMPRINT_SPOTDOT:
			maxPageWidth = 8.5*EmbosserTools.INCH_IN_MM;
			maxPageHeight = 14*EmbosserTools.INCH_IN_MM;
			break;
		case MAX:
			maxPageWidth = 14*EmbosserTools.INCH_IN_MM;
			maxPageHeight = 22*EmbosserTools.INCH_IN_MM;
			break;
		case EMFUSE:
			maxPageWidth =  Math.max(297d, 11*EmbosserTools.INCH_IN_MM);   // A3, Tabloid
			maxPageHeight = Math.max(420d, 17*EmbosserTools.INCH_IN_MM);
			break;
		default:
			throw new IllegalArgumentException("Unsupported embosser type");

		}
	}
	
	@Override
	protected double getCellWidth() {
		return 0.25*EmbosserTools.INCH_IN_MM;
	}

	@Override
	protected double getCellHeight() {
		return 0.4*EmbosserTools.INCH_IN_MM;
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

	@Override
	public boolean supportsPageFormat(PageFormat format) {
		if (format == null) { return false; }
		try {
			return supportsPrintPage(getPrintPage(format.asSheetPaperFormat()));
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public boolean supportsPrintPage(PrintPage dim) {
		if (dim==null) { return false; }
		return (dim.getWidth()  <= maxPageWidth)  &&
				(dim.getWidth()  >= minPageWidth)  &&
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
	public boolean supportsAligning() {
		return true;
	}

	@Override
	public boolean supports8dot() {

		switch (type) {
		default:
			return false;
		}
	}

	@Override
	public boolean supportsDuplex() {

		switch (type) {
		case PREMIER_80:
		case PREMIER_100:
		case ELITE_150:
		case ELITE_200:
		case EMFUSE:
			return true;
		case PRO_GEN_II:
		case CUB:
		case CUB_JR:
		case MAX:
		case EMPRINT_SPOTDOT:
		default:
			return false;
		}
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

		PageFormat page = getPageFormat();

		if (!supportsPageFormat(page)) {
			throw new IllegalArgumentException(new UnsupportedPaperException("Unsupported paper"));
		}

		try {

			byte[] header = getHeader(duplexEnabled, false);
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

		} catch (EmbosserFactoryException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private byte[] getHeader(boolean duplex,
			boolean eightDots)
					throws EmbosserFactoryException {

		// Legacy printing mode

		PageFormat page = getPageFormat();
		PrintPage printPage = getPrintPage(page);
		int linesPerPage = getMaxHeight(page);

		int formWidth = (int)Math.ceil(printPage.getWidth()/EmbosserTools.INCH_IN_MM*2);
		int formLength = (int)Math.ceil(printPage.getHeight()/EmbosserTools.INCH_IN_MM*2);
		int topOffset = (int)Math.floor(getPrintableArea(page).getOffsetY()/EmbosserTools.INCH_IN_MM*20);

		if (formWidth > 42)  { throw new UnsupportedPaperException("Form width cannot > 21 inch"); }
		if (formLength > 42) { throw new UnsupportedPaperException("Form lenght cannot > 21 inch"); }

		StringBuffer header = new StringBuffer();

		header.append((char)0x1b); header.append('@');                              // System reset
		header.append((char)0x1b); header.append("W@");                             // Word wrap = OFF
		header.append((char)0x1b); header.append('K');
		header.append((char)(40+topOffset));             // Top margin
		header.append((char)0x1b); header.append('L');
		header.append((char)(40+marginInner));           // Left margin
		header.append((char)0x1b); header.append('Q');
		header.append((char)(40+linesPerPage));          // Lines per page
		header.append((char)0x1b); header.append('S');            
		header.append((char)(40+formWidth));             // Form width
		header.append((char)0x1b); header.append('T');
		header.append((char)(40+formLength));            // Form length
		header.append((char)0x1b); header.append('F');
		header.append(eightDots?'B':'@');                // 6/8 dot
		if (supportsDuplex()) {
			header.append((char)0x1b); header.append('I');
			header.append(duplex?'A':'@');                   // Interpoint
		}
		header.append((char)0x1b); header.append("AA");                             // US Braille table
		//      header.append((char)0x1b); header.append("M@");                             // Media type = Braille paper
		//      header.append((char)0x1b); header.append("BA");                             // Dot height = NORMAL
		//      header.append((char)0x1b); header.append("C@");                             // Auto perforation = OFF
		//      header.append((char)0x1b); header.append("J@");                             // Standard Braille dot quality
		//      header.append((char)0x1b); header.append("H@");                             // Standard Ink text quality

		return header.toString().getBytes();
	}

	@Override
	public Area getPrintableArea(PageFormat pageFormat) {

		PrintPage printPage = getPrintPage(pageFormat);

		double cellWidth = getCellWidth();
		double cellHeight = getCellHeight();
		double printablePageHeight = Math.min(printPage.getHeight(), maxLinesInHeight * getCellHeight());

		return new Area(printPage.getWidth() - (marginInner + marginOuter) * cellWidth,
				printablePageHeight - (marginTop + marginBottom) * cellHeight,
				marginInner * cellWidth,
				marginTop * cellHeight);
	}

	@Override
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
