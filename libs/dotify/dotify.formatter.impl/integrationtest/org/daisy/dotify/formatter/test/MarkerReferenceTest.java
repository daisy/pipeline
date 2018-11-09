package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Ignore;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class MarkerReferenceTest extends AbstractFormatterEngineTest {

	@Test
	public void testPageMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-page-input.obfl", "resource-files/marker-ref-page-expected.pef", false);
	}
	
	@Test
	public void testPageVolMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-page-vol-input.obfl", "resource-files/marker-ref-page-vol-expected.pef", false);
	}
	
	@Test
	public void testPageContentMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-page-content-input.obfl", "resource-files/marker-ref-page-content-expected.pef", false);
	}
	
	@Test
	public void testSpreadMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-spread-input.obfl", "resource-files/marker-ref-spread-expected.pef", false);
	}
	
	@Test
	public void testSpreadContentMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-spread-content-input.obfl", "resource-files/marker-ref-spread-content-expected.pef", false);
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
	
	@Test
	public void testDocumentMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-sequence-input.obfl", "resource-files/marker-ref-sequence-expected.pef", false);
	}

	@Test
	public void testSequenceVolMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-sequence-vol-input.obfl", "resource-files/marker-ref-sequence-vol-expected.pef", false);
	}
	
	@Test
	@Ignore("This is an open issue, see https://github.com/brailleapps/dotify.formatter.impl/issues/48")
	public void testMarkerMultiple() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref-multiple-input.obfl", "resource-files/marker-ref-multiple-expected.pef", false);
	}

}
