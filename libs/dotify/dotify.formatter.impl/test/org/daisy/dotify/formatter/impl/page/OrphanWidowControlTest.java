package org.daisy.dotify.formatter.impl.page;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class OrphanWidowControlTest {

	@Test
	public void testOrphansWidows_01() {
		OrphanWidowControl owc = new OrphanWidowControl(2, 2, 6);
		assertFalse(owc.allowsBreakAfter(0));
		assertTrue(owc.allowsBreakAfter(1));
		assertTrue(owc.allowsBreakAfter(2));
		assertTrue(owc.allowsBreakAfter(3));
		assertFalse(owc.allowsBreakAfter(4));
		assertTrue(owc.allowsBreakAfter(5));
	}
	
	@Test
	public void testOrphansWidows_02() {
		OrphanWidowControl owc = new OrphanWidowControl(3, 1, 6);
		assertFalse(owc.allowsBreakAfter(0));
		assertFalse(owc.allowsBreakAfter(1));
		assertTrue(owc.allowsBreakAfter(2));
		assertTrue(owc.allowsBreakAfter(3));
		assertTrue(owc.allowsBreakAfter(4));
		assertTrue(owc.allowsBreakAfter(5));
	}
	
	@Test
	public void testOrphansWidows_03() {
		OrphanWidowControl owc = new OrphanWidowControl(3, 3, 6);
		assertFalse(owc.allowsBreakAfter(0));
		assertFalse(owc.allowsBreakAfter(1));
		assertTrue(owc.allowsBreakAfter(2));
		assertFalse(owc.allowsBreakAfter(3));
		assertFalse(owc.allowsBreakAfter(4));
		assertTrue(owc.allowsBreakAfter(5));
	}
	
	@Test
	public void testOrphansWidows_04() {
		OrphanWidowControl owc = new OrphanWidowControl(12, 12, 6);
		assertFalse(owc.allowsBreakAfter(0));
		assertFalse(owc.allowsBreakAfter(1));
		assertFalse(owc.allowsBreakAfter(2));
		assertFalse(owc.allowsBreakAfter(3));
		assertFalse(owc.allowsBreakAfter(4));
		assertTrue(owc.allowsBreakAfter(5));
	}
	
	@Test
	public void testOrphansWidows_05() {
		OrphanWidowControl owc = new OrphanWidowControl(1, 1, 6);
		assertTrue(owc.allowsBreakAfter(0));
		assertTrue(owc.allowsBreakAfter(1));
		assertTrue(owc.allowsBreakAfter(2));
		assertTrue(owc.allowsBreakAfter(3));
		assertTrue(owc.allowsBreakAfter(4));
		assertTrue(owc.allowsBreakAfter(5));
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
    @Ignore ("Ignored because it's currently not possible to check the upper index.")
	public void testOrphansWidows_06() {
		OrphanWidowControl owc = new OrphanWidowControl(1, 1, 6);
		owc.allowsBreakAfter(6);
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void testOrphansWidows_07() {
		OrphanWidowControl owc = new OrphanWidowControl(1, 1, 6);
		owc.allowsBreakAfter(-1);
	}
	
	@Test
	public void testOrphansWidows_08() {
		OrphanWidowControl owc = new OrphanWidowControl(0, 0, 6);
		assertTrue(owc.allowsBreakAfter(0));
		assertTrue(owc.allowsBreakAfter(1));
		assertTrue(owc.allowsBreakAfter(2));
		assertTrue(owc.allowsBreakAfter(3));
		assertTrue(owc.allowsBreakAfter(4));
		assertTrue(owc.allowsBreakAfter(5));
	}
}
