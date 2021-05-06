package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class OrphansWidowsTest extends AbstractFormatterEngineTest {

    @Test
    public void testOrphans() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/orphans-input.obfl",
            "resource-files/orphans-expected.pef",
            false
        );
    }

    @Test
    public void testWidows() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/widows-input.obfl",
            "resource-files/widows-expected.pef",
            false
        );
    }

}
