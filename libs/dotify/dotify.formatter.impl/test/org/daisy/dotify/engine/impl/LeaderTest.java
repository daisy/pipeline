package org.daisy.dotify.engine.impl;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Ignore;
import org.junit.Test;
public class LeaderTest extends AbstractFormatterEngineTest {
	
	@Test @Ignore("This is a known issue, ignore until fixed")
	public void testLeaderSequence() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/leader-right-input.obfl", "resource-files/leader-right-expected.pef", false);
	}
	
	@Test
	public void testLeaderSimpleRight() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/leader-right-simple-input.obfl", "resource-files/leader-right-simple-expected.pef", false);
	}
	
	@Test
	public void testLeaderMultipleRight() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/leader-right-multiple-input.obfl", "resource-files/leader-right-multiple-expected.pef", false);
	}

}
