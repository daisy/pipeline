package org.daisy.pipeline.braille.pef.impl;

import java.util.Iterator;
import javax.imageio.spi.ServiceRegistry;

import org.daisy.braille.api.table.TableProvider;
import org.daisy.braille.consumer.table.TableCatalog;

// wrapper class for org.daisy.braille.consumer.table.TableCatalog that can be instantiated using SPI
public class TableCatalog_SPI extends TableCatalog {
	
	public TableCatalog_SPI() {
		super();
		// copied from TableCatalog.newInstance()
		Iterator<TableProvider> i = ServiceRegistry.lookupProviders(TableProvider.class);
		while (i.hasNext()) {
			addFactory(i.next());
		}
	}
}
