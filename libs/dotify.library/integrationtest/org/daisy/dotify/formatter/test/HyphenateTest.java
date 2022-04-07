package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineMaker;
import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.translator.TranslatorType;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMaker;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class HyphenateTest extends AbstractFormatterEngineTest {

    private FormatterEngine configureEngine() throws PagedMediaWriterConfigurationException {
        return FormatterEngineMaker.newInstance().getFactory().newFormatterEngine(
                new FormatterConfiguration.Builder("sv-SE",
                        TranslatorType.UNCONTRACTED.toString())
                .allowsEndingPageOnHyphen(true)
                .allowsEndingVolumeOnHyphen(false)
                .build(),
                PagedMediaWriterFactoryMaker.newInstance().newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE));
    }

    @Test
    public void testHyphenation_01() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/hyphenate/hyphenate-input.obfl",
            "resource-files/hyphenate/hyphenate-expected.pef",
            false
        );
    }

    @Test
    public void testHyphenateLastLine_01() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {

        testPEF(
            configureEngine(),
            "resource-files/hyphenate/hyphenate-last-line-input.obfl",
            "resource-files/hyphenate/hyphenate-last-line-expected.pef",
            null
        );
    }

    @Test
    public void testHyphenateLastLine_02() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            configureEngine(),
            "resource-files/hyphenate/hyphenate-last-line2-input.obfl",
            "resource-files/hyphenate/hyphenate-last-line2-expected.pef",
            null
        );
    }

    @Test
    public void testHyphenateLastLine_03() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            configureEngine(),
            "resource-files/hyphenate/hyphenate-last-line3-input.obfl",
            "resource-files/hyphenate/hyphenate-last-line3-expected.pef",
            null
        );
    }

}
