package org.daisy.dotify.formatter.impl.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class VolumeKeepPriorityTest {
	
	@Test
	public void testInBounds() {
		VolumeKeepPriority.of(1);
		VolumeKeepPriority.of(9);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testOutOfBounds_01() {
		VolumeKeepPriority.of(0);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testOutOfBounds_02() {
		VolumeKeepPriority.of(10);
	}
	
	@Test
	public void testOrElse() {
		assertEquals(1, VolumeKeepPriority.empty().orElse(1), 0);
	}

	@Test
	public void testCompare_01() {
		assertTrue(VolumeKeepPriority.naturalOrder().compare(VolumeKeepPriority.empty(), VolumeKeepPriority.of(9)) < 0);
	}
	
	@Test
	public void testCompare_02() {
		assertTrue(VolumeKeepPriority.naturalOrder().compare(VolumeKeepPriority.of(9), VolumeKeepPriority.of(3)) < 0);
	}
	
	@Test
	public void testCompare_03() {
		assertTrue(VolumeKeepPriority.naturalOrder().compare(VolumeKeepPriority.of(9), VolumeKeepPriority.empty()) > 0);
	}
	
	@Test
	public void testCompare_04() {
		assertTrue(VolumeKeepPriority.naturalOrder().compare(VolumeKeepPriority.of(1), VolumeKeepPriority.of(9)) > 0);
	}
	
	@Test
	public void testCompare_05() {
		assertEquals(0, VolumeKeepPriority.naturalOrder().compare(VolumeKeepPriority.of(4), VolumeKeepPriority.of(4)));
	}
	
	@Test
	public void testCompare_06() {
		assertEquals(0, VolumeKeepPriority.naturalOrder().compare(VolumeKeepPriority.empty(), VolumeKeepPriority.empty()));
	}
}
