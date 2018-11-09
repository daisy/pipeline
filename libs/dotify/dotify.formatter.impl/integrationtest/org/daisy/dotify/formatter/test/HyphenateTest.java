package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineMaker;
import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMaker;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class HyphenateTest extends AbstractFormatterEngineTest {

	private FormatterEngine configureEngine() throws PagedMediaWriterConfigurationException {
		return FormatterEngineMaker.newInstance().getFactory().newFormatterEngine(
					new FormatterConfiguration.Builder("sv-SE",
						BrailleTranslatorFactory.MODE_UNCONTRACTED)
					.allowsEndingVolumeOnHyphen(false)
					.build(), 
				PagedMediaWriterFactoryMaker.newInstance().newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE));
	}

	@Test
	public void testHyphenateLastLine_01() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF(configureEngine(), "resource-files/hyphenate-last-line-input.obfl", "resource-files/hyphenate-last-line-expected.pef", null);
	}

	@Test
	public void testHyphenateLastLine_02() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF(configureEngine(), "resource-files/hyphenate-last-line2-input.obfl", "resource-files/hyphenate-last-line2-expected.pef", null);
	}
	
	@Test
	public void testHyphenateLastLine_03() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF(configureEngine(), "resource-files/hyphenate-last-line3-input.obfl", "resource-files/hyphenate-last-line3-expected.pef", null);
	}

}
