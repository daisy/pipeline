package org.daisy.braille.impl.spi;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SPIHelperTest {

	@Test
	public void testSPIHelper() {
		assertNotNull(SPIHelper.getTableCatalog());
	}
}
