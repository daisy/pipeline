package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class PageBorderTest extends AbstractFormatterEngineTest {

	@Test
	public void testPageBorder() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/page-border-input.obfl", "resource-files/page-border-expected.pef", false);
	}

}
