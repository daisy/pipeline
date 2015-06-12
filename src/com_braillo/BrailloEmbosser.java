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

import org.daisy.braille.api.embosser.EmbosserFeatures;
import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.table.DefaultTableProvider;
import org.daisy.braille.api.table.TableCatalogService;
import org.daisy.braille.api.table.TableFilter;
import org.daisy.braille.impl.embosser.AbstractEmbosser;

public abstract class BrailloEmbosser extends AbstractEmbosser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7640218914742790228L;
	private final static TableFilter tableFilter;
	static {
		tableFilter = new TableFilter() {
			public boolean accept(FactoryProperties object) {
				if (object.getIdentifier().equals(DefaultTableProvider.class.getCanonicalName() + ".TableType.EN_US")) { return true; }
				if (object.getIdentifier().startsWith(BrailloTableProvider.class.getCanonicalName() + ".TableType.")) { return true; }
				return false;
			}
		};
	}

	public BrailloEmbosser(TableCatalogService service, FactoryProperties props) {
		super(service, props.getDisplayName(), props.getDescription(), props.getIdentifier());
		//TODO: fix this, width is 6.0325
		setFeature(EmbosserFeatures.CELL_WIDTH, 6);
		setFeature(EmbosserFeatures.CELL_HEIGHT, 10);
	}

	@Override
	public TableFilter getTableFilter() {
		return tableFilter;
	}
        
}
