package org.daisy.dotify.common.splitter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * TODO: write java doc.
 */
public class SplitPointDataListTest {

    @Test
    public void testEmpty() {
        SplitPointDataList<SplitPointUnit> m = new SplitPointDataList<>();
        assertTrue(m.isEmpty());
    }

    @Test
    public void testSizeAfterTruncating_01() {
        SplitPointDataList<SplitPointUnit> m = new SplitPointDataList<>(mock(SplitPointUnit.class))
                .tail(1);
        assertTrue(m.isEmpty());
    }

    @Test
    public void testSizeAfterTruncating_02() {
        SplitPointDataList<SplitPointUnit> m = new SplitPointDataList<>(
            mock(SplitPointUnit.class), mock(SplitPointUnit.class)
        ).tail(1);
        assertFalse(m.isEmpty());
        assertEquals(1, m.getSize(10));
    }

    @Test
    public void testSize() {
        SplitPointDataList<SplitPointUnit> m = new SplitPointDataList<>(mock(SplitPointUnit.class));
        assertEquals(0, m.getSize(0));
        assertEquals(1, m.getSize(1));
        assertEquals(1, m.getSize(2));

    }

    @Test
    public void testHasElementAt() {
        SplitPointDataList<SplitPointUnit> m = new SplitPointDataList<>(mock(SplitPointUnit.class));
        assertTrue(m.hasElementAt(0));
        assertFalse(m.hasElementAt(1));
    }

    @Test
    public void testGetUntil() {
        SplitPointDataList<SplitPointUnit> m = new SplitPointDataList<>(
            mock(SplitPointUnit.class), mock(SplitPointUnit.class)
        );
        assertEquals(1, m.head(1).size());
    }

    @Test
    public void testSplitOutOfRange_01() {
        SplitPointDataList<SplitPointUnit> m = new SplitPointDataList<>(
            mock(SplitPointUnit.class), mock(SplitPointUnit.class)
        );
        SplitResult<?, ?> res = m.split(4);
        assertEquals(2, res.head().size());
        assertEquals(0, res.tail().getRemaining().size());
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testSplitOutOfRange_02() {
        SplitPointDataList<SplitPointUnit> m = new SplitPointDataList<>(
            mock(SplitPointUnit.class), mock(SplitPointUnit.class)
        );
        SplitResult<?, ?> res = m.splitInRange(4);
        assertEquals(2, res.head().size());
        assertEquals(0, res.tail().getRemaining().size());
    }

    @Test
    public void testSplitOutOfRange_03() {
        SplitPointDataList<SplitPointUnit> m = new SplitPointDataList<>(
            mock(SplitPointUnit.class), mock(SplitPointUnit.class)
        );
        SplitResult<?, ?> res = m.splitInRange(0);
        assertEquals(0, res.head().size());
        assertEquals(2, res.tail().getRemaining().size());
    }

}
