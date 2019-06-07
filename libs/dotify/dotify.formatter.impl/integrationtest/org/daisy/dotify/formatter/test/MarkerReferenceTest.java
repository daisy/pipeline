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
		testPEF("resource-files/marker-ref/marker-ref-page-input.obfl", "resource-files/marker-ref/marker-ref-page-expected.pef", false);
	}

	@Test
	public void testPageVolMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-page-vol-input.obfl", "resource-files/marker-ref/marker-ref-page-vol-expected.pef", false);
	}

	@Test
	public void testPageContentMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-page-content-input.obfl", "resource-files/marker-ref/marker-ref-page-content-expected.pef", false);
	}

	@Test
	public void testSpreadMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-spread-input.obfl", "resource-files/marker-ref/marker-ref-spread-expected.pef", false);
	}

	@Test
	public void testSpreadContentMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-spread-content-input.obfl", "resource-files/marker-ref/marker-ref-spread-content-expected.pef", false);
	}

	@Test
	public void testSpreadSequenceMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-spread-sequence-input.obfl", "resource-files/marker-ref/marker-ref-spread-sequence-expected.pef", false);
	}

	@Test
	public void testSpreadVolumeMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-spread-volume-input.obfl", "resource-files/marker-ref/marker-ref-spread-volume-expected.pef", false);
	}

	@Test
	public void testSheetMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-sheet-input.obfl", "resource-files/marker-ref/marker-ref-sheet-expected.pef", false);
	}

	@Test
	public void testMarkerAfterLeader() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-after-leader-input.obfl", "resource-files/marker-ref/marker-ref-after-leader-expected.pef", false);
	}

	@Test
	public void testDocumentMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-sequence-input.obfl", "resource-files/marker-ref/marker-ref-sequence-expected.pef", false);
	}

	@Test
	public void testSequenceVolMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-sequence-vol-input.obfl", "resource-files/marker-ref/marker-ref-sequence-vol-expected.pef", false);
	}

	@Test
	@Ignore("This is an open issue, see https://github.com/brailleapps/dotify.formatter.impl/issues/48")
	public void testMarkerMultiple() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-ref-multiple-input.obfl", "resource-files/marker-ref/marker-ref-multiple-expected.pef", false);
	}

	@Test
	public void testMarkerReferenceSequenceBackward() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-sequence-backward-input.obfl",
				"resource-files/marker-ref/marker-reference-sequence-backward-expected.pef", false);
	}
	@Test
	public void testMarkerReferencePageForwardBackward() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-page-forward-backward-input.obfl",
				"resource-files/marker-ref/marker-reference-page-forward-backward-expected.pef", false);
	}
	@Test
	public void testMarkerReferencePageContentForwardBackward() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-page-content-forward-backward-input.obfl",
				"resource-files/marker-ref/marker-reference-page-content-forward-backward-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceAcrossSequenceWorkaround() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-across-sequence-workaround-input.obfl",
				"resource-files/marker-ref/marker-reference-across-sequence-workaround-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceSpreadAcrossSequence() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-spread-across-sequence-input.obfl",
				"resource-files/marker-ref/marker-reference-spread-across-sequence-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceSpreadAcrossVolume() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-spread-across-volume-input.obfl",
				"resource-files/marker-ref/marker-reference-spread-across-volume-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceStartOffsetFirstPage() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-start-offset-first-page-input.obfl",
				"resource-files/marker-ref/marker-reference-start-offset-first-page-expected.pef", false);
	}
	@Test
	public void testMarkerReferencePageFirstWorkaround() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-page-first-workaround-input.obfl",
				"resource-files/marker-ref/marker-reference-page-first-workaround-expected.pef", false);
	}
	@Test
	public void testMarkerReferencePageStartWorkaround() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-page-start-workaround-input.obfl",
				"resource-files/marker-ref/marker-reference-page-start-workaround-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceSpreadStartWorkaround() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-spread-start-workaround-input.obfl",
				"resource-files/marker-ref/marker-reference-spread-start-workaround-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceSpreadInitialPageNumber() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-spread-initial-page-number-input.obfl",
				"resource-files/marker-ref/marker-reference-spread-initial-page-number-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceIssue39() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/marker-ref/marker-reference-issue-39-input.obfl",
		        "resource-files/marker-ref/marker-reference-issue-39-expected.pef", false);
	}

}
