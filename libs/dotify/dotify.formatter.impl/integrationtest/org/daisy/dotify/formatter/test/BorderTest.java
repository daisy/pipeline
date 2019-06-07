package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class BorderTest extends AbstractFormatterEngineTest {

	@Test
	public void testBordersCenter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/border/border-center-input.obfl", "resource-files/border/border-center-expected.pef", false);
	}

	@Test
	public void testBordersOuter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/border/border-outer-input.obfl", "resource-files/border/border-outer-expected.pef", false);
	}

	@Test
	public void testBordersInner() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/border/border-inner-input.obfl", "resource-files/border/border-inner-expected.pef", false);
	}
	@Test
	public void testNestedBlocksWithBorders() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/border/nested-blocks-with-borders-input.obfl",
				"resource-files/border/nested-blocks-with-borders-expected.pef", false);
	}
	@Test
	public void testBorderAlignCenter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/border/border-align-center-input.obfl",
				"resource-files/border/border-align-center-expected.pef", false);
	}

}
