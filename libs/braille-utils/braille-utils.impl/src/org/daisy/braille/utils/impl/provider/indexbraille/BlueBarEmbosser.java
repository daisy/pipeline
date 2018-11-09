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
package org.daisy.braille.utils.impl.provider.indexbraille;

import java.io.OutputStream;

import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.dotify.api.table.TableFilter;
import org.daisy.braille.utils.impl.provider.indexbraille.IndexEmbosserProvider.EmbosserType;
import org.daisy.braille.utils.impl.tools.embosser.SimpleEmbosserProperties;

public class BlueBarEmbosser extends IndexEmbosser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2619451994009139923L;
	private static final TableFilter tableFilter;
	private static final String table6dot = IndexTableProvider.TableType.IDENTIFIER_PREFIX + "INDEX_TRANSPARENT_6DOT";

	static {
		tableFilter = new TableFilter() {
			@Override
			public boolean accept(FactoryProperties object) {
				if (object == null) { return false; }
				String tableID = object.getIdentifier();
				if (tableID.equals(table6dot)) { return true; }
				return false;
			}
		};
	}

	public BlueBarEmbosser(TableCatalogService service, EmbosserType props) {

		super(service, props);
		setTable = service.newTable(table6dot);
	}
	
	@Override
	public boolean supportsDuplex() {
		return false;
	}

	@Override
	public TableFilter getTableFilter() {
		return tableFilter;
	}

	@Override
	public EmbosserWriter newEmbosserWriter(OutputStream os) {

		PageFormat page = getPageFormat();

		if (!supportsPageFormat(page)) {
			throw new IllegalArgumentException("Unsupported paper for embosser " + getDisplayName());
		}

		SimpleEmbosserProperties props =
				SimpleEmbosserProperties.with(getMaxWidth(page), getMaxHeight(page))
				.supportsDuplex(duplexEnabled)
				.supportsAligning(supportsAligning())
				.build();

		return new IndexTransparentEmbosserWriter(os,
				setTable.newBrailleConverter(),
				false,
				null,
				null,
				props);
	}
}
