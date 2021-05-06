package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class MarginTest extends AbstractFormatterEngineTest {

    @Test
    public void testCollapsingMargin_01() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/margin/margin-input.obfl",
            "resource-files/margin/margin-expected.pef",
            false
        );
    }

    @Test
    public void testCollapsingMargin_02() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/margin/margin-nested-input.obfl",
            "resource-files/margin/margin-nested-expected.pef",
            false
        );
    }

    @Test
    public void testCollapsingMargin_03() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/margin/margin2-input.obfl",
            "resource-files/margin/margin2-expected.pef",
            false
        );
    }

    @Test
    public void testCollapsingMargin_04() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/margin/margin-nested2-input.obfl",
            "resource-files/margin/margin-nested2-expected.pef",
            false
        );
    }

    @Test
    public void testUnevenMargin_01() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/margin/margin-uneven-input.obfl",
            "resource-files/margin/margin-uneven-expected.pef",
            false
        );
    }

    @Test
    public void testCollapsingMargin_05() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/margin/margin3-input.obfl",
            "resource-files/margin/margin3-expected.pef",
            false
        );
    }
}
