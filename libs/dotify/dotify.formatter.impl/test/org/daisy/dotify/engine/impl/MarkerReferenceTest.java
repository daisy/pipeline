package org.daisy.dotify.engine.impl;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
public class MarkerReferenceTest extends AbstractFormatterEngineTest {

	@Test
	public void testPageMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-page-input.obfl", "resource-files/marker-ref-page-expected.pef", false);
	}
	
	@Test
	public void testSpreadMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-spread-input.obfl", "resource-files/marker-ref-spread-expected.pef", false);
	}
	
	@Test
	public void testSpreadSequenceMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-spread-sequence-input.obfl", "resource-files/marker-ref-spread-sequence-expected.pef", false);
	}
	
	@Test
	public void testSpreadVolumeMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-spread-volume-input.obfl", "resource-files/marker-ref-spread-volume-expected.pef", false);
	}
	
	@Test
	public void testSheetMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-sheet-input.obfl", "resource-files/marker-ref-sheet-expected.pef", false);
	}
	
	@Test
	public void testMarkerAfterLeader() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-after-leader-input.obfl", "resource-files/marker-ref-after-leader-expected.pef", false);
	}

}
