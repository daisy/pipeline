package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class ListTest extends AbstractFormatterEngineTest {

    @Test
    public void testContinueList() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/list/list-continue-input.obfl",
            "resource-files/list/list-continue-expected.pef",
            false
        );
    }

    @Test
    public void testAlphaList() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/list/list-alpha-input.obfl",
            "resource-files/list/list-alpha-expected.pef",
            false
        );
    }

    @Test
    public void testCustomSymbolList_01() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/list/list-custom-symbol-input.obfl",
            "resource-files/list/list-custom-symbol-expected.pef",
            false
        );
    }

    @Test
    public void testCustomSymbolList_02() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/list/list-custom-symbol2-input.obfl",
            "resource-files/list/list-custom-symbol2-expected.pef",
            false
        );
    }

}
