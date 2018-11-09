package org.daisy.dotify.formatter.impl.datatype;

import static org.junit.Assert.assertEquals;

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
		// This appears counter-intuitive because the *priority* of the first argument is lower, 
		// but the value is in fact higher.
		assertEquals(1, VolumeKeepPriority.compare(VolumeKeepPriority.empty(), VolumeKeepPriority.of(9)));
	}
	
	@Test
	public void testCompare_02() {
		// This appears counter-intuitive because the *priority* of the first argument is lower, 
		// but the value is higher.
		assertEquals(1, VolumeKeepPriority.compare(VolumeKeepPriority.of(9), VolumeKeepPriority.of(3)));
	}
	
	@Test
	public void testCompare_03() {
		// This appears counter-intuitive because the *priority* of the first argument is higher, 
		// but the value is in fact lower.
		assertEquals(-1, VolumeKeepPriority.compare(VolumeKeepPriority.of(9), VolumeKeepPriority.empty()));
	}
	
	@Test
	public void testCompare_04() {
		// This appears counter-intuitive because the *priority* of the first argument is higher, 
		// but the value is lower. 
		assertEquals(-1, VolumeKeepPriority.compare(VolumeKeepPriority.of(1), VolumeKeepPriority.of(9)));
	}
	
	@Test
	public void testCompare_05() {
		assertEquals(0, VolumeKeepPriority.compare(VolumeKeepPriority.of(4), VolumeKeepPriority.of(4)));
	}
	
	@Test
	public void testCompare_06() {
		assertEquals(0, VolumeKeepPriority.compare(VolumeKeepPriority.empty(), VolumeKeepPriority.empty()));
	}
}
