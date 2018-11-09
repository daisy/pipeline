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
package org.daisy.braille.utils.impl.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.daisy.dotify.api.embosser.Device;
import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.PrintPage;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.Paper;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.dotify.api.table.TableFilter;
import org.daisy.braille.utils.impl.tools.embosser.AbstractEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.ConfigurableEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.EmbosserTools;
import org.daisy.braille.utils.impl.tools.embosser.FileToDeviceEmbosserWriter;
import org.daisy.braille.utils.impl.tools.embosser.SimpleEmbosserProperties;

public class GenericEmbosser extends AbstractEmbosser {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5756220386304696977L;
	private static final TableFilter tableFilter;

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

	public GenericEmbosser(TableCatalogService service, EmbosserFactoryProperties props) {
		super(service, props);
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
		SimpleEmbosserProperties.Builder sep;
		if (getPageFormat()!=null) {
			sep = SimpleEmbosserProperties.with(
					EmbosserTools.getWidth(pp, getCellWidth()), 
					EmbosserTools.getHeight(pp, getCellHeight())
					).supportsAligning(supportsAligning());
		} else {
			//using Integer.MAX_VALUE because SimpleEmbosserProperties/EmbosserWriterProperties
			//does not support null width/height
			sep =SimpleEmbosserProperties.with(
					Integer.MAX_VALUE, 
					Integer.MAX_VALUE
					//cannot support aligning for an unspecified page format
					).supportsAligning(false);
		}
		b.embosserProperties(sep.supportsDuplex(supportsDuplex()).build());
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

	@Override
	public boolean supportsVolumes() {
		return false;
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
		//should this be true?
		return true;
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

	@Override
	protected double getCellWidth() {
		return 6;
	}

	@Override
	protected double getCellHeight() {
		return 10;
	}

}
