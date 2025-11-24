package org.daisy.dotify.formatter.impl.page;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class OrphanWidowControlTest {

    @Test
    public void testOrphansWidows_01() {
        OrphanWidowControl owc = new OrphanWidowControl(2, 2, 6, null, null);
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
    }

    @Test
    public void testOrphansWidows_02() {
        OrphanWidowControl owc = new OrphanWidowControl(3, 1, 6, null, null);
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
    }

    @Test
    public void testOrphansWidows_03() {
        OrphanWidowControl owc = new OrphanWidowControl(3, 3, 6, null, null);
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
    }

    @Test
    public void testOrphansWidows_04() {
        OrphanWidowControl owc = new OrphanWidowControl(12, 12, 6, null, null);
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertFalse(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
    }

    @Test
    public void testOrphansWidows_05() {
        OrphanWidowControl owc = new OrphanWidowControl(1, 1, 6, null, null);
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
    }

    @Test
    public void testOrphansWidows_08() {
        OrphanWidowControl owc = new OrphanWidowControl(0, 0, 6, null, null);
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
        owc.increaseRowCount();
        assertTrue(owc.allowsBreakAfter());
    }
}
