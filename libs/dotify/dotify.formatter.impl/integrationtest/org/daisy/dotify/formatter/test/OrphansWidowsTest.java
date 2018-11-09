package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class OrphansWidowsTest extends AbstractFormatterEngineTest {

	@Test
	public void testOrphans() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/orphans-input.obfl", "resource-files/orphans-expected.pef", false);
	}
	
	@Test
	public void testWidows() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/widows-input.obfl", "resource-files/widows-expected.pef", false);
	}

}
