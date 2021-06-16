package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class ListOfReferencesTest extends AbstractFormatterEngineTest {

    @Test
    public void testListOfReferencesVolumeRange() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/list-of-references-input.obfl",
            "resource-files/list-of-references-expected.pef",
            false
        );
    }

    @Test
    public void testListOfReferencesDocumentRange() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/list-of-references-document-range-input.obfl",
            "resource-files/list-of-references-document-range-expected.pef",
            false
        );
    }

    @Test @Ignore // this test is ignored because it demonstrates a bug
    public void testAnchorInEmptyBlock() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/list-of-references-anchor-in-empty-block-input.obfl",
            "resource-files/list-of-references-anchor-in-empty-block-expected.pef",
            true
        );
    }
}
