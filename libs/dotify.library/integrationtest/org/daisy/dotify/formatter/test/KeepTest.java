package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class KeepTest extends AbstractFormatterEngineTest {

    @Test
    public void testKeep() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/keep-input.obfl",
            "resource-files/keep-expected.pef",
            false
        );
    }

    @Test
    public void testViolateKeep() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/keep-violate-input.obfl",
            "resource-files/keep-violate-expected.pef",
            false
        );
    }
}
