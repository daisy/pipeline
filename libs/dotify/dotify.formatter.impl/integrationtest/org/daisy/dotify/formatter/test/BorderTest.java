package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class BorderTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testBordersCenter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/border-center-input.obfl", "resource-files/border-center-expected.pef", false);
	}
	
	@Test
	public void testBordersOuter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/border-outer-input.obfl", "resource-files/border-outer-expected.pef", false);
	}
	
	@Test
	public void testBordersInner() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/border-inner-input.obfl", "resource-files/border-inner-expected.pef", false);
	}

}
