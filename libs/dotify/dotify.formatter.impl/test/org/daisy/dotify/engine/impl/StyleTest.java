package org.daisy.dotify.engine.impl;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
public class StyleTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testEmptyStyle() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/style-empty-input.obfl", "resource-files/style-empty-expected.pef", false);
	}

}
