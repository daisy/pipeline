package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Test;

import java.io.IOException;

/**
 * Unit tests for toc-entry-on-resumed functionality.
 * 
 * @author Paul Rambags
 */
@SuppressWarnings("javadoc")
public class TocEntryOnResumedTest extends AbstractFormatterEngineTest {

    @Test
    public void testTocEntryOnResumedWithVolumeRange() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/toc-entry-on-resumed/toc-entry-on-resumed-volume-range-input.obfl",
            "resource-files/toc-entry-on-resumed/toc-entry-on-resumed-volume-range-expected.pef",
            false
        );
    }
    @Test
    public void testTocEntryOnResumedWithDocumentRange() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/toc-entry-on-resumed/toc-entry-on-resumed-document-range-input.obfl",
            "resource-files/toc-entry-on-resumed/toc-entry-on-resumed-document-range-expected.pef",
            false
        );
    }
    @Test
    public void testTocEntryOnResumedWithMultipleVolumeBreaks() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/toc-entry-on-resumed/toc-entry-on-resumed-multiple-volume-breaks-input.obfl",
            "resource-files/toc-entry-on-resumed/toc-entry-on-resumed-multiple-volume-breaks-expected.pef",
            false
        );
    }
    @Test
    public void testTocEntryOnResumedWithLongChapter() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/toc-entry-on-resumed/toc-entry-on-resumed-long-chapter-input.obfl",
            "resource-files/toc-entry-on-resumed/toc-entry-on-resumed-long-chapter-expected.pef",
            false
        );
    }

}
