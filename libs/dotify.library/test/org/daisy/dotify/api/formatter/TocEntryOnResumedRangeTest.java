package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.obfl.ObflParserException;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for class TocEntryOnResumedRange.
 *
 * @author Paul Rambags
 */
public class TocEntryOnResumedRangeTest {

    @Test
    public void testStandardRange() throws ObflParserException {
        TocEntryOnResumedRange range = new TocEntryOnResumedRange("[ch_1,ch_2)");
        assertEquals("ch_1", range.getStartRefId());
        Optional<String> endRefId = range.getEndRefId();
        assertTrue(endRefId.isPresent());
        assertEquals("ch_2", endRefId.get());
    }

    @Test
    public void testUnboundedRange() throws ObflParserException {
        TocEntryOnResumedRange range = new TocEntryOnResumedRange("[ch_1,)");
        assertEquals("ch_1", range.getStartRefId());
        Optional<String> endRefId = range.getEndRefId();
        assertFalse(endRefId.isPresent());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedRange() throws ObflParserException {
        TocEntryOnResumedRange range = new TocEntryOnResumedRange("[ch_1,ch_2]");
        assertNotNull(range);
    }

    @Test(expected = ObflParserException.class)
    public void testInvalidRange() throws ObflParserException {
        TocEntryOnResumedRange range = new TocEntryOnResumedRange("[,)");
        assertNotNull(range);
    }

}

