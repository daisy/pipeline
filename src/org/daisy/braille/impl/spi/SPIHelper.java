package org.daisy.braille.impl.spi;

import java.lang.reflect.Method;

import org.daisy.braille.api.table.TableCatalogService;

/**
 * Provides methods to access services in an SPI context without
 * breaking compatibility with OSGi. This class cannot be used in an OSGi context,
 * but it can be accessed from classes that are OSGi compatible if it is only
 * accessed when the runtime context is known not to be OSGi.
 * 
 * @author Joel Håkansson
 */
public class SPIHelper {
	private static TableCatalogService tableCatalog;
	
	/**
	 * <p>Gets a table catalog instance, or null if not found.</p> 
	 * 
	 * <p>Note: This method uses reflexion to get the table catalog implementation.</p>
	 * @return returns a table catalog
	 */
	public static TableCatalogService getTableCatalog() {
		if (tableCatalog==null) {
			try {
				Class<?> cls = Class.forName("org.daisy.braille.consumer.table.TableCatalog");
				Method m = cls.getMethod("newInstance");
				tableCatalog = (TableCatalogService)m.invoke(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tableCatalog;
	}

}
