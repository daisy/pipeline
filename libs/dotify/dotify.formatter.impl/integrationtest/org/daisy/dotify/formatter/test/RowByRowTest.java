package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class RowByRowTest extends AbstractFormatterEngineTest {

	@Test
	public void testCurrentPage_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/current-page-input.obfl", "resource-files/current-page-expected.pef", false);
	}
	
	@Test
	public void testCurrentPage_02() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/current-page2-input.obfl", "resource-files/current-page2-expected.pef", false);
	}

}
