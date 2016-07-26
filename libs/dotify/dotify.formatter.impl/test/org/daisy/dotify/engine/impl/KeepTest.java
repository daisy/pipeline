package org.daisy.dotify.engine.impl;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
public class KeepTest extends AbstractFormatterEngineTest {

	@Test
	public void testKeep() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/keep-input.obfl", "resource-files/keep-expected.pef", false);
	}

}
