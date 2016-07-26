package org.daisy.dotify.engine.impl;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
public class TranslationTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testTranslation_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/translate-input.obfl", "resource-files/translate-expected.pef", false);
	}

}
