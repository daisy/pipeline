package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Ignore;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class PaddingTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testPadding_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/padding-input.obfl", "resource-files/padding-expected.pef", false);
	}
	
	@Test
	public void testPadding_02() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/padding-sides-only-input.obfl", "resource-files/padding-sides-only-expected.pef", false);
	}
	
	@Test
	public void testPadding_03() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/padding-nested-input.obfl", "resource-files/padding-nested-expected.pef", false);
	}
	
	@Test
	@Ignore ("Fails with an IndexOutOfBoundsException. A more descriptive error would be good, see issue #141")
	public void testPadding_04() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/padding-height-input.obfl", "resource-files/padding-height-expected.pef", false);
	}

}
