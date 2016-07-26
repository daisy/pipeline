package org.daisy.dotify.engine.impl;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
public class HyphenationTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testHyphenation_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/hyphenate-input.obfl", "resource-files/hyphenate-expected.pef", false);
	}

}
