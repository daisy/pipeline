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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.factory.AbstractFactory;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.dotify.api.table.TableFilter;
import org.daisy.braille.utils.impl.provider.BrailleEditorsFileFormatProvider.FileType;
import org.daisy.braille.utils.impl.tools.embosser.AbstractEmbosserWriter.Padding;
import org.daisy.braille.utils.impl.tools.embosser.ConfigurableEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.SimpleEmbosserProperties;


/**
 *
 * @author Bert Frees
 */
public class BrailleEditorsFileFormat extends AbstractFactory implements FileFormat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4217010913717769350L;
	private FileType type;
	private Table table;
	private final TableCatalogService tableCatalog;
	private TableFilter tableFilter;
	private final Collection<String> supportedTableIds = new ArrayList<>();

	private final boolean duplexEnabled = false;

	public BrailleEditorsFileFormat(FileType type, TableCatalogService tableCatalog) {
		super(type.getDisplayName(), type.getDescription(), type.getIdentifier());
		this.type = type;
		switch (type) {
		case BRF:
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.MIT");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.NABCC");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.NABCC_8DOT");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.EN_GB");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.NL_NL");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.EN_GB");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.DA_DK");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.DE_DE");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.CS_CZ");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.IT_IT_FIRENZE");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.ES_ES");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.ES_ES_TABLE_2");
			break;
		case BRA:
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.ES_ES");
			supportedTableIds.add("org_daisy.EmbosserTableProvider.TableType.ES_ES_TABLE_2");
			break;
		case BRL:
			supportedTableIds.add("org_daisy.BrailleEditorsTableProvider.TableType.BRL");
			break;
		default:
			throw new IllegalArgumentException("Unsupported filetype");
		}

		tableFilter = new TableFilter() {
			@Override
			public boolean accept(FactoryProperties object) {
				return supportedTableIds.contains(object.getIdentifier());
			}
		};

		this.tableCatalog = tableCatalog;
		table = tableCatalog.newTable("org_daisy.EmbosserTableProvider.TableType.MIT");
	}

	@Override
	public TableFilter getTableFilter() {
		return tableFilter;
	}

	@Override
	public boolean supportsTable(Table table) {
		return getTableFilter().accept(table);
	}

	@Override
	public boolean supportsDuplex() {
		return false;
	}

	@Override
	public boolean supports8dot() {
		return false;
	}

	@Override
	public EmbosserWriter newEmbosserWriter(OutputStream os) {

		if (!supportsTable(table)) {
			throw new IllegalArgumentException("Unsupported table: " + table.getDisplayName());
		}

		int maxCols = 1000;
		int maxRows = 1000;

		SimpleEmbosserProperties props =
				SimpleEmbosserProperties.with(maxCols, maxRows)
				.supportsDuplex(duplexEnabled)
				.supportsAligning(false)
				.build();

		switch (type) {
		case BRF:
			return new ConfigurableEmbosser.Builder(os, table.newBrailleConverter())
					.breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
					.padNewline(Padding.BEFORE)
					.embosserProperties(props)
					.build();
		case BRA:
			return new ConfigurableEmbosser.Builder(os, table.newBrailleConverter())
					.breaks(new StandardLineBreaks(StandardLineBreaks.Type.UNIX))
					.pagebreaks(new NoPageBreaks())
					.padNewline(Padding.AFTER)
					.embosserProperties(props)
					.build();
		case BRL:
			return new MicroBrailleFileFormatWriter(os);
		default:
			return null;
		}
	}

	@Override
	public String getFileExtension() {
		return "." + type.name().toLowerCase();
	}

	@Override
	public void setFeature(String key, Object value) {

		if (EmbosserFeatures.TABLE.equals(key)) {
			if (value == null) {
				throw new IllegalArgumentException("Unsupported value for table");
			}
			Table t;
			try {
				t = (Table)value;
			} catch (ClassCastException e) {
				t = tableCatalog.newTable(value.toString());
			}
			if (getTableFilter().accept(t)) {
				table = t;
			} else {
				throw new IllegalArgumentException("Unsupported value for table.");
			}
		} else {
			throw new IllegalArgumentException("Unsupported feature " + key);
		}
	}

	@Override
	public Object getFeature(String key) {

		if (EmbosserFeatures.TABLE.equals(key)) {
			return table;
		} else {
			throw new IllegalArgumentException("Unsupported feature " + key);
		}
	}

	@Override
	public Object getProperty(String key) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
