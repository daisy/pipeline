package org.daisy.pipeline.braille.pef.impl;

import java.util.Iterator;

import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.dotify.api.table.TableProvider;
import org.daisy.common.spi.ServiceLoader;

// wrapper class for org.daisy.dotify.api.table.TableCatalog that can be instantiated using SPI
public class TableCatalog_SPI extends TableCatalog {
	
	public TableCatalog_SPI() {
		super();
		Iterator<TableProvider> i = ServiceLoader.load(TableProvider.class).iterator();
		while (i.hasNext()) {
			addFactory(i.next());
		}
	}
}
