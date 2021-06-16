package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class PageAreaTest extends AbstractFormatterEngineTest {

    @Test
    public void testAnchorInEmptyBlock() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/page-area-anchor-in-empty-block-input.obfl",
            "resource-files/page-area-anchor-in-empty-block-expected.pef",
            false
        );
    }
}
