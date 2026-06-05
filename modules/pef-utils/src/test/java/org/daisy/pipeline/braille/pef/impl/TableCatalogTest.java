package org.daisy.pipeline.braille.pef.impl;

import java.util.Collection;
import java.util.Iterator;

import org.daisy.common.spi.ServiceLoader;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.TableProvider;

import org.junit.Test;
import org.junit.Assert;

public class TableCatalogTest {
	
	@Test
	public void listAllTables() {
		TableCatalog catalog = new TableCatalog();
		Iterator<TableProvider> providers = ServiceLoader.load(TableProvider.class).iterator();
		while (providers.hasNext()) catalog.addTableProvider(providers.next());
		catalog.list();
		Collection<FactoryProperties> allTables = catalog.listAll();
		// for (FactoryProperties t : allTables)
		// 	System.err.println(t.getIdentifier() + ": " + t.getDisplayName());
		Assert.assertEquals(25, allTables.size());
	}
}
