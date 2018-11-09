package org.daisy.braille.utils.pef;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class RangeTest {

	@Test (expected=IllegalArgumentException.class)
	public void testZeroFrom() {
		new Range(0);
	}

	@Test (expected=IllegalArgumentException.class)
	public void testNegativeFrom() {
		new Range(-1);
	}

	@Test (expected=IllegalArgumentException.class)
	public void testNegativeTo() {
		new Range(-3,-1);
	}

	@Test (expected=IllegalArgumentException.class)
	public void testToSmallerThanFrom() {
		new Range(2, 1);
	}

	@Test
	public void testSimpleRange() {
		Range r = Range.parseRange("1");
		assertTrue("Assert that value is not in range.", !r.inRange(0));
		assertTrue("Assert that value is not in range.", !r.inRange(2));
	}

	@Test
	public void testOpenEndRange() {
		Range r = Range.parseRange("2-");
		assertTrue("Assert that value is not in range.", !r.inRange(1));
		assertTrue("Assert that value is in range.", r.inRange(8033));
	}

	@Test
	public void testOpenStartRange() {
		Range r = Range.parseRange("-10");
		assertTrue("Assert that value is not in range.", !r.inRange(0));
		assertTrue("Assert that value is in range.", r.inRange(1));
		assertTrue("Assert that value is in range.", r.inRange(10));
		assertTrue("Assert that value is not in range.", !r.inRange(11));
	}

	@Test
	public void tesClosedRange() {
		Range r = Range.parseRange("2-5");
		assertTrue("Assert that value is not in range.", !r.inRange(1));
		assertTrue("Assert that value is in range.", r.inRange(2));
		assertTrue("Assert that value is in range.", r.inRange(5));
		assertTrue("Assert that value is not in range.", !r.inRange(6));
	}

	@Test
	public void testToString_01() {
		Range r = new Range(1, 2);
		assertEquals("1-2", r.toString());
	}

	@Test
	public void testToString_02() {
		Range r = new Range(1);
		assertEquals("1-", r.toString());
	}

	@Test
	public void testGetFrom() {
		Range r = new Range(1, 2);
		assertEquals(1, r.getFrom());
	}

	@Test
	public void testGetTo() {
		Range r = new Range(1, 2);
		assertEquals(2, r.getTo());
	}

}
