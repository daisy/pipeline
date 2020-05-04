package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class LeaderTest extends AbstractFormatterEngineTest {

    @Test
    public void testLeaderSequence() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/leader-right-input.obfl",
            "resource-files/leader-right-expected.pef",
            false
        );
    }

    @Test
    public void testLeaderSimpleRight() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/leader-right-simple-input.obfl",
            "resource-files/leader-right-simple-expected.pef",
            false
        );
    }

    @Test
    public void testLeaderMultipleRight() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/leader-right-multiple-input.obfl",
            "resource-files/leader-right-multiple-expected.pef",
            false
        );
    }

    @Test
    public void testLeaderRightTextIndent() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/leader-right-text-indent-input.obfl",
            "resource-files/leader-right-text-indent-expected.pef",
            false
        );
    }
}
