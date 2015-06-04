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

import java.io.OutputStream;

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.paper.Area;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.api.table.TableCatalogService;
import org.daisy.braille.embosser.BufferedVolumeEmbosser;
import org.daisy.braille.embosser.Device;
import org.daisy.braille.embosser.EmbosserFactoryException;
import org.daisy.braille.embosser.EmbosserTools;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.PrintPage;
import org.daisy.braille.embosser.PrintPage.PrintDirection;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.embosser.StandardLineBreaks;

import com_braillo.Braillo440VolumeWriter.Mode;
import com_braillo.BrailloEmbosserProvider.EmbosserType;

public abstract class AbstractBraillo440Embosser extends BrailloEmbosser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3735464395595074473L;
	private final static double cellWidth = 19*EmbosserTools.INCH_IN_MM/80d; //6;
	private final static double cellHeight = 10;
	private final static double constant = 11*EmbosserTools.INCH_IN_MM/80d;
	
	protected boolean saddleStitchEnabled;

	public AbstractBraillo440Embosser(TableCatalogService service, FactoryProperties props) {
		super(service, props);
		saddleStitchEnabled = false;
		setCellWidth(cellWidth);
	}

	@Override
	public boolean supportsPrintPage(PrintPage dim) {
		int width = (int)Math.floor((dim.getWidth()+constant-EmbosserTools.INCH_IN_MM) / cellWidth);
		int inchHeight = (int)Math.ceil(dim.getHeight()/EmbosserTools.INCH_IN_MM);
		if (width > 44 || inchHeight > 13 || width < 10) { 
			return false; 
		}
		return true;
	}

	@Override
	public EmbosserWriter newEmbosserWriter(OutputStream os) {
		throw  new IllegalArgumentException(new EmbosserFactoryException(getDisplayName() + " does not support writing to file."));
	}

	@Override
	public EmbosserWriter newEmbosserWriter(Device device) {
		Table tc = tableCatalogService.newTable(setTable.getIdentifier());
		tc.setFeature("fallback", getFeature("fallback"));
		tc.setFeature("replacement", getFeature("replacement"));
		Braillo440VolumeWriter bvw;
		EmbosserType t = EmbosserType.valueOf(getIdentifier().substring(1+getIdentifier().lastIndexOf('.')));
		PrintPage printPage = getPrintPage(getPageFormat());
		int width = (int)Math.floor((printPage.getWidth()+constant-EmbosserTools.INCH_IN_MM) / cellWidth);
		int height = EmbosserTools.getHeight(printPage, cellHeight);
		double columnWidthMM = width * cellWidth - constant;
		if (t==EmbosserType.BRAILLO_440_SW && saddleStitchEnabled) {
			bvw = new Braillo440VolumeWriter(printPage, Mode.SW_FOUR_PAGE, width, height, columnWidthMM);
		} else if (t==EmbosserType.BRAILLO_440_SW) {
			bvw = new Braillo440VolumeWriter(printPage, Mode.SW_TWO_PAGE, width, height, columnWidthMM);
		} else if (t==EmbosserType.BRAILLO_440_SWSF) {
			bvw = new Braillo440VolumeWriter(printPage, Mode.SWSF, width, height, columnWidthMM);
		} else {
			throw new RuntimeException("Unexpected error.");
		}
		/*
		 * 	public boolean supports8dot() {return false;}

	public boolean supportsAligning() {	return true;}

	public boolean supportsDuplex() {return true;}

	public int getMaxHeight() {return height; }
		public int getMaxWidth() {	return width;}
		 */
		SimpleEmbosserProperties ep = new SimpleEmbosserProperties(width, height)
			.supports8dot(false)
			.supportsAligning(true)
			.supportsDuplex(true);
		BufferedVolumeEmbosser.Builder b = new BufferedVolumeEmbosser.Builder(device, tc.newBrailleConverter(), bvw, ep)
			.breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
			.padNewline(BufferedVolumeEmbosser.Padding.NONE) // JH100408: changed from BEFORE
			.autoLineFeedOnEmptyPage(true);
		return b.build();
	}
	/*
	public int getMaxWidth(Paper paper) {
		return (int)Math.floor((paper.getWidth()+constant-EmbosserTools.INCH_IN_MM) / getCellWidth());
	}*/

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
        return true;
    }

	public PrintPage getPrintPage(PageFormat pageFormat) {
		return new PrintPage(pageFormat,
				PrintDirection.SIDEWAYS,
				(saddleStitchEnabled?PrintMode.MAGAZINE:PrintMode.REGULAR)
			);
	}

	public Area getPrintableArea(PageFormat pageFormat) {
		PrintPage printPage = getPrintPage(pageFormat);
		return new Area(printPage.getWidth(), printPage.getHeight(), 0, 0);
	}

	public boolean supportsZFolding() {
		return false;
	}

}
