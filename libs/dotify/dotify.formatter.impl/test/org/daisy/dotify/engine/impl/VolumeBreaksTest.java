package org.daisy.dotify.engine.impl;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Ignore;
import org.junit.Test;
public class VolumeBreaksTest extends AbstractFormatterEngineTest {

	@Test
	public void testUnevenVolumeBreak() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/volume-breaks-uneven-input.obfl", "resource-files/volume-breaks-uneven-expected.pef", false);
	}

	@Test
	public void testForcedVolumeBreak() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/volume-breaks1-input.obfl", "resource-files/volume-breaks1-expected.pef", false);
	}
	
	@Test
	public void testVolumeKeepPriority() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/volume-breaks-priority-input.obfl", "resource-files/volume-breaks-priority-expected.pef", false);
	}
	
	@Test
	public void testVolumeKeepPriority_2() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/volume-breaks-priority2-input.obfl", "resource-files/volume-breaks-priority2-expected.pef", false);
	}
	
	@Test
	public void testVolumeKeepNext() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/volume-breaks-keep-next-input.obfl", "resource-files/volume-breaks-keep-next-expected.pef", false);
	}
	
	@Test @Ignore
	public void testKeeVolumeKeepPrevious() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/volume-breaks-keep-previous-input.obfl", "resource-files/volume-breaks-keep-previous-expected.pef", true);
	}

}
