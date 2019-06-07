package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class VerticalPositionTest extends AbstractFormatterEngineTest {

	@Test
	public void testVerticalPosition() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/vertical-position/vertical-position-input.obfl", "resource-files/vertical-position/vertical-position-expected.pef", false);
	}

	@Test
	public void testVerticalPositionWithBorders() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/vertical-position/vertical-position-borders-input.obfl", "resource-files/vertical-position/vertical-position-borders-expected.pef", false);
	}

	@Test
	public void testVerticalPositionBefore() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/vertical-position/vertical-position-before-input.obfl", "resource-files/vertical-position/vertical-position-before-expected.pef", false);
	}

	@Test
	public void testVerticalPositionDLS() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/vertical-position/vertical-position-dls-input.obfl", "resource-files/vertical-position/vertical-position-dls-expected.pef", false);
	}

	@Test
	public void testVerticalPositionWithHeader() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/vertical-position/vertical-position-header-input.obfl", "resource-files/vertical-position/vertical-position-header-expected.pef", false);
	}
	@Test
	public void testVerticalPositionAlignBefore() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/vertical-position/vertical-position-align-before-input.obfl",
				"resource-files/vertical-position/vertical-position-align-before-expected.pef", false);
	}
	@Test
	public void testVerticalPositionAlignBeforeNestedBlocks() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/vertical-position/vertical-position-align-before-nested-blocks-input.obfl",
				"resource-files/vertical-position/vertical-position-align-before-nested-blocks-expected.pef", false);
	}

	@Test
	public void testVerticalPositionFirstBlock() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/vertical-position/vertical-position-first-block-input.obfl",
				"resource-files/vertical-position/vertical-position-first-block-expected.pef", false);
	}
	
	@Test
	public void testVerticalPositionPassed() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/vertical-position/vertical-position-passed-input.obfl",
				"resource-files/vertical-position/vertical-position-passed-expected.pef", false);
	}
}
