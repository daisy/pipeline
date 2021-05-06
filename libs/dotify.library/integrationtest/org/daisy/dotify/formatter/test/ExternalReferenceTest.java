package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Testing the external reference object interaction with framework.
 * In this case an external reference tag will be handled, sent through and
 * added to rows in the output.
 */
public class ExternalReferenceTest extends AbstractFormatterEngineTest {

    @Ignore("Not implemented yet, only supporting references on block level")
    @Test
    public void testExternalReferenceTwoReferencesSameRow() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
                "resource-files/external-reference1-input.obfl",
                "resource-files/external-reference1-expected.pef",
                false
        );
    }

    @Ignore("Not implemented yet, only supporting references on block level")
    @Test
    public void testExternalReferenceWhiteSpaceBeforeNotStripped() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
                "resource-files/external-reference2-input.obfl",
                "resource-files/external-reference2-expected.pef",
                false
        );
    }

    @Ignore("Not implemented yet, only supporting references on block level")
    @Test
    public void testExternalReferenceAttachToTheRightRow() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
                "resource-files/external-reference3-input.obfl",
                "resource-files/external-reference3-expected.pef",
                false
        );
    }

    @Test
    public void testExternalReferenceSimpleReferenceAtStartOfBlock() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
                "resource-files/external-reference4-input.obfl",
                "resource-files/external-reference4-expected.pef",
                false
        );
    }

    @Test
    public void testExternalReferenceTwoReferencesSamePrefix() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
                "resource-files/external-reference5-input.obfl",
                "resource-files/external-reference5-expected.pef",
                false
        );
    }

    @Test
    public void testExternalReferenceDontParseReferencesNotInAnExternalNamespace() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
                "resource-files/external-reference6-input.obfl",
                "resource-files/external-reference6-expected.pef",
                false
        );
    }

    /**
     * Ensure crash if unsupported layout is used. This test can be removed when other use-cases are implemented.
     *
     * @throws LayoutEngineException                    Thrown if layout engine can't be initiated.
     * @throws IOException                              Thrown if files are missing.
     * @throws PagedMediaWriterConfigurationException   Thrown if configuration is invalid.
     */
    @Test(expected = IllegalStateException.class)
    public void testExternalReferenceUnsupportedUseCase() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/external-reference7-input.obfl",
            null,
            false
        );
    }

}
