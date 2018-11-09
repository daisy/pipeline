package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class XMLDataTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testXMLData_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/xml-data-input.obfl", "resource-files/xml-data-expected.pef", false);
	}

	@Test
	public void testXMLData_02() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/xml-data2-input.obfl", "resource-files/xml-data2-expected.pef", false);
	}
	
	@Test
	public void testXMLData_03() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/xml-data3-input.obfl", "resource-files/xml-data3-expected.pef", false);
	}
	
	@Test
	public void testXMLData_04() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/xml-data4-input.obfl", "resource-files/xml-data4-expected.pef", false);
	}
	
	@Test
	public void testXMLData_05() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/xml-data5-input.obfl", "resource-files/xml-data5-expected.pef", false);
	}
	
	@Test
	public void testXMLData_06() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/xml-data6-input.obfl", "resource-files/xml-data6-expected.pef", false);
	}

	@Test
	public void testXMLData_07() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/xml-data7-input.obfl", "resource-files/xml-data7-expected.pef", false);
	}
	
	@Test
	public void testXMLDataInBlock_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/xml-data-in-block-input.obfl", "resource-files/xml-data-in-block-expected.pef", false);
	}

}
