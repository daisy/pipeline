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
package org.daisy.braille.utils.impl.tools.embosser;

import java.util.HashMap;

import org.daisy.dotify.api.embosser.Embosser;
import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.embosser.PrintPage;
import org.daisy.dotify.api.factory.AbstractFactory;
import org.daisy.dotify.api.paper.Area;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.braille.utils.impl.tools.table.DefaultTableProvider;

/**
 * Provides an abstract base for Embossers, implementing basic features
 * such as the ability to set page format, table, and cell height and width.
 * @author Joel Håkansson
 */
public abstract class AbstractEmbosser extends AbstractFactory implements Embosser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 374888389077716688L;
	private final String make;
	private final String model;
	private final HashMap<String, Object> props;
	private final HashMap<String, String> settings;
	protected final TableCatalogService tableCatalogService;
	protected final Table defaultTable;
	private PageFormat pageFormat;
	protected Table setTable;

	/**
	 * Creates a new AbstractEmbosser with the supplied name, description and identifier
	 * @param service the table catalog
	 * @param emProps the properties
	 */
	public AbstractEmbosser(TableCatalogService service, EmbosserFactoryProperties emProps) {
		super(emProps.getDisplayName(), emProps.getDescription(), emProps.getIdentifier());
		this.make = emProps.getMake();
		this.model = emProps.getModel();
		this.props = new HashMap<>();
		this.settings = new HashMap<>();
		this.tableCatalogService = service;
		defaultTable = service.newTable(DefaultTableProvider.TableType.EN_US.getIdentifier());
		setTable = defaultTable;
	}

	@Override
	public String getMake() {
		return make;
	}

	@Override
	public String getModel() {
		return model;
	}



	/**
	 * Gets the page format
	 * @return returns the page format
	 */
	protected PageFormat getPageFormat() {
		return pageFormat;
	}

	/**
	 * Gets cell width (2 x the horizontal dot-to-dot distance), in millimeters.
	 * @return returns cell width, in millimeters
	 */
	protected abstract double getCellWidth();

	/**
	 * Gets cell height (4 x the vertical dot-to-dot distance), in millimeters.
	 * This value should not include any line spacing.
	 * @return returns cell height, in millimeters
	 */
	protected abstract double getCellHeight();

	@Override
	public int getMaxHeight(PageFormat pageFormat) {
		return EmbosserTools.getHeight(getPrintableArea(pageFormat), getCellHeight());
	}

	@Override
	public int getMaxWidth(PageFormat pageFormat) {
		return EmbosserTools.getWidth(getPrintableArea(pageFormat), getCellWidth());
	}

	@Override
	public Area getPrintableArea(PageFormat pageFormat) {
		PrintPage printPage = getPrintPage(pageFormat);
		return new Area(printPage.getWidth(), printPage.getHeight(), 0, 0);
	}

	@Override
	public Object getFeature(String key) {
		if (EmbosserFeatures.PAGE_FORMAT.equals(key)) {
			return pageFormat;
		} else if (EmbosserFeatures.TABLE.equals(key)) {
			return setTable;
		}
		return settings.get(key);
	}

	@Override
	public Object getProperty(String key) {
		return props.get(key);
	}

	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for this key, the old
	 * value is replaced.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 */
	@Override
	public void setFeature(String key, Object value) {
		if (EmbosserFeatures.PAGE_FORMAT.equals(key)) {
			try {
				if (!supportsPageFormat((PageFormat)value)) {
					throw new IllegalArgumentException("Page format is not supported by the embosser: " + (PageFormat)value);
				}
				pageFormat = (PageFormat)value;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Unsupported value for pageFormat: '" + value + "'", e);
			}
		} else if (EmbosserFeatures.TABLE.equals(key)) {
			Table t;
			if (value == null) {
				throw new IllegalArgumentException("Unsupported value for table: value = null");
			}
			try {
				t = (Table)value;
			} catch (ClassCastException e) {
				t = tableCatalogService.newTable(value.toString());
				if (t == null) {
					throw new IllegalArgumentException("Unsupported value for table: '" + value + "'");
				}
			}
			setTable = t;
		}
		else {
			if (EmbosserFeatures.CELL_WIDTH.equals(key) && !"6".equals(value.toString())) {
				throw new IllegalArgumentException("Changing cell width has not been implemented.");
			} else if (EmbosserFeatures.CELL_HEIGHT.equals(key) && !"10".equals(value.toString())) {
				throw new IllegalArgumentException("Changing cell height has not been implemented.");
			} else if (EmbosserFeatures.NUMBER_OF_COPIES.equals(key)) {
				throw new IllegalArgumentException("Unsupported feature 'number of copies'.");
			} else if (EmbosserFeatures.SADDLE_STITCH.equals(key)) {
				throw new IllegalArgumentException("Unsupported feature 'saddle stich mode'.");
			} else if (EmbosserFeatures.Z_FOLDING.equals(key)) {
				throw new IllegalArgumentException("Unsupported feature 'z folding mode'.");
			} else if (EmbosserFeatures.DUPLEX.equals(key)) {
				throw new IllegalArgumentException("Unsupported feature 'duplex'.");
			} else if (EmbosserFeatures.PAGES_IN_QUIRE.equals(key)) {
				throw new IllegalArgumentException("Unsupported feature 'pages in quire'.");
			}
			settings.put(key, value.toString());
		}
	}

	@Override
	public boolean supportsTable(Table table) {
		return getTableFilter().accept(table);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractEmbosser [props=" + props + ", settings=" + settings
				+ ", defaultTable=" + defaultTable + ", pageFormat="
				+ pageFormat + ", setTable=" + setTable + ", toString()="
				+ super.toString() + "]";
	}

}
