package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class FooterTest extends AbstractFormatterEngineTest {

    @Test
    public void testMultipleHeader() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/multi-line-footer-input.obfl",
            "resource-files/multi-line-footer-expected.pef",
            false
        );
    }

}
