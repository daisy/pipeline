package org.daisy.dotify.common.split;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.mockito.Mockito.mock;

@SuppressWarnings("javadoc")
public class SplitPointDataListTest {
	
	@Test
	public void testEmpty() {
		SplitPointDataSource<SplitPointUnit> m = new SplitPointDataList<>();
		assertTrue(m.isEmpty());
	}
	
	@Test
	public void testSizeAfterTruncating_01() {
		SplitPointDataSource<SplitPointUnit> m = new SplitPointDataList<>(mock(SplitPointUnit.class))
				.tail(1);
		assertTrue(m.isEmpty());
	}

	@Test
	public void testSizeAfterTruncating_02() {
		SplitPointDataSource<SplitPointUnit> m = new SplitPointDataList<>(mock(SplitPointUnit.class), mock(SplitPointUnit.class))
				.tail(1);
		assertFalse(m.isEmpty());
		assertEquals(1, m.getSize(10));
	}
	
	@Test
	public void testSize() {
		SplitPointDataSource<SplitPointUnit> m = new SplitPointDataList<>(mock(SplitPointUnit.class));
		assertEquals(0, m.getSize(0));
		assertEquals(1, m.getSize(1));
		assertEquals(1, m.getSize(2));

	}
	
	@Test
	public void testHasElementAt() {
		SplitPointDataSource<SplitPointUnit> m = new SplitPointDataList<>(mock(SplitPointUnit.class));
		assertTrue(m.hasElementAt(0));
		assertFalse(m.hasElementAt(1));
	}
	
	@Test
	public void testGetUntil() {
		SplitPointDataSource<SplitPointUnit> m = new SplitPointDataList<>(mock(SplitPointUnit.class), mock(SplitPointUnit.class));
		assertEquals(1, m.head(1).size());
	}


}
