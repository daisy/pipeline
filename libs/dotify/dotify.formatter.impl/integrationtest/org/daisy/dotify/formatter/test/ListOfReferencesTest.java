package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class ListOfReferencesTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testListOfReferencesVolumeRange() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/list-of-references-input.obfl",
		        "resource-files/dp2/list-of-references-expected.pef", false);
	}
	
	@Test
	public void testListOfReferencesDocumentRange() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/list-of-references-document-range-input.obfl",
		        "resource-files/dp2/list-of-references-document-range-expected.pef", false);
	}
}
