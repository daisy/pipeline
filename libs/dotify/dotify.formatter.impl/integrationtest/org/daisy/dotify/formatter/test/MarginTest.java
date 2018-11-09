package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class MarginTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testCollapsingMargin_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/margin-input.obfl", "resource-files/margin-expected.pef", false);
	}

	@Test
	public void testCollapsingMargin_02() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/margin-nested-input.obfl", "resource-files/margin-nested-expected.pef", false);
	}
	
	@Test
	public void testCollapsingMargin_03() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/margin2-input.obfl", "resource-files/margin2-expected.pef", false);
	}
	
	@Test
	public void testCollapsingMargin_04() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/margin-nested2-input.obfl", "resource-files/margin-nested2-expected.pef", false);
	}
}
