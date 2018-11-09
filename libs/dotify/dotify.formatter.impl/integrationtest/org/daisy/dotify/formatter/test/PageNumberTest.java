package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class PageNumberTest extends AbstractFormatterEngineTest {

	@Test
	public void testPageNumber_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/page/page-number-input.obfl", "resource-files/page/page-number-expected.pef", false);
	}

	@Test
	public void testPageNumber_02() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/page/page-number2-input.obfl", "resource-files/page/page-number2-expected.pef", false);
	}

	@Test
	public void testPageNumber_03() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/page/page-number3-input.obfl", "resource-files/page/page-number3-expected.pef", false);
	}

	@Test
	public void testPageNumber_04() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/page/page-number4-input.obfl", "resource-files/page/page-number4-expected.pef", false);
	}

	@Test
	public void testPageNumber_05() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/page/page-number5-input.obfl", "resource-files/page/page-number5-expected.pef", false);
	}

	@Test
	public void testPageNumber_06() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/page/page-number6-input.obfl", "resource-files/page/page-number6-expected.pef", false);
	}

	@Test
	public void testPageNumber_07() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/page/page-number7-input.obfl", "resource-files/page/page-number7-expected.pef", false);
	}

	@Test
	public void testPageNumber_08() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/page/page-number8-input.obfl", "resource-files/page/page-number8-expected.pef", false);
	}

}
