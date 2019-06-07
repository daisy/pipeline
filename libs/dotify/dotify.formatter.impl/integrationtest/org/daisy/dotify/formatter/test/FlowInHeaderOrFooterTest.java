package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class FlowInHeaderOrFooterTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testFlowInFooter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-footer-input.obfl",
		        "resource-files/flow-in/flow-in-footer-expected.pef",
		        false);
	}
	
	@Test
	public void testFlowInFooterWithMarkerReference() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-footer-with-marker-reference-input.obfl",
		        "resource-files/flow-in/flow-in-footer-with-marker-reference-expected.pef",
		        false);
	}
	
	@Test
	public void testFlowInHeader() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-input.obfl",
		        "resource-files/flow-in/flow-in-header-expected.pef",
		        false);
	}
	
	
	@Test
	public void testFlowInHeaderFooter_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer1-input.obfl", "resource-files/flow-in/flow-in-header-footer1-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_02() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer2-input.obfl", "resource-files/flow-in/flow-in-header-footer2-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_03() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer3-input.obfl", "resource-files/flow-in/flow-in-header-footer3-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_04() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer4-input.obfl", "resource-files/flow-in/flow-in-header-footer4-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_05() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer5-input.obfl", "resource-files/flow-in/flow-in-header-footer5-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_06() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer6-input.obfl", "resource-files/flow-in/flow-in-header-footer6-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_07() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer7-input.obfl", "resource-files/flow-in/flow-in-header-footer7-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_08() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer8-input.obfl", "resource-files/flow-in/flow-in-header-footer8-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_09() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer9-input.obfl", "resource-files/flow-in/flow-in-header-footer9-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_10() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer10-input.obfl", "resource-files/flow-in/flow-in-header-footer10-expected.pef", false);
	}
	
	@Test
	public void testFlowInHeaderFooter_11() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/flow-in/flow-in-header-footer11-input.obfl", "resource-files/flow-in/flow-in-header-footer11-expected.pef", false);
	}
}
