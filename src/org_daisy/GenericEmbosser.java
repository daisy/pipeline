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
package org_daisy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.daisy.braille.embosser.AbstractEmbosser;
import org.daisy.braille.embosser.ConfigurableEmbosser;
import org.daisy.braille.embosser.Device;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserTools;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.embosser.PrintPage;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.braille.table.TableFilter;
import org.daisy.factory.FactoryProperties;
import org.daisy.paper.PageFormat;
import org.daisy.paper.Paper;

public class GenericEmbosser extends AbstractEmbosser {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5756220386304696977L;
	private final static TableFilter tableFilter;
	
	static {
		tableFilter = new TableFilter() {
			@Override
			public boolean accept(FactoryProperties object) {
				if (object!=null) {
					return true; 
				} else { return false; }
			}
		};
	}
	
	public GenericEmbosser(TableCatalogService service, FactoryProperties props) {
		super(service, props.getDisplayName(), props.getDescription(), props.getIdentifier());
		setFeature(EmbosserFeatures.CELL_WIDTH, 6);
		setFeature(EmbosserFeatures.CELL_HEIGHT, 10);
	}

	@Override
	public boolean supportsPrintPage(PrintPage dim) {
		return true;
	}
	
	@Override
	public boolean supportsPageFormat(PageFormat pageFormat) {
		return true;
	}
	
    @Override
	public boolean supportsPaper(Paper paper) {
		return true;
	}

	@Override
	public TableFilter getTableFilter() {
		return tableFilter;
	}
	
	@Override
	public EmbosserWriter newEmbosserWriter(OutputStream os) {
		PrintPage pp = new PrintPage(getPageFormat());

		Table tc = tableCatalogService.newTable(setTable.getIdentifier());
		tc.setFeature("fallback", getFeature("fallback"));
		tc.setFeature("replacement", getFeature("replacement"));
		ConfigurableEmbosser.Builder b = new ConfigurableEmbosser.Builder(os, tc.newBrailleConverter());
		b.breaks((String)getFeature("breaks"));
        // b.padNewline(Padding.NONE);
		b.padNewline((String)getFeature("padNewline"));
		SimpleEmbosserProperties sep;
		if (getPageFormat()!=null) {
			sep = new SimpleEmbosserProperties(
					EmbosserTools.getWidth(pp, getCellWidth()), 
					EmbosserTools.getHeight(pp, getCellHeight())
					).supportsAligning(supportsAligning());
		} else {
			//using Integer.MAX_VALUE because SimpleEmbosserProperties/EmbosserWriterProperties
			//does not support null width/height
			sep = new SimpleEmbosserProperties(
					Integer.MAX_VALUE, 
					Integer.MAX_VALUE
					//cannot support aligning for an unspecified page format
					).supportsAligning(false);
		}
		b.embosserProperties(sep.supports8dot(supports8dot())
				.supportsDuplex(supportsDuplex()));
		return b.build();
	}

	@Override
	public EmbosserWriter newEmbosserWriter(Device device) {
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
    	//should this be true?
        return true;
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
