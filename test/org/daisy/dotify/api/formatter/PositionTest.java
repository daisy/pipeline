package org.daisy.dotify.api.formatter;
import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.formatter.Position;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class PositionTest {

	@Test
	public void testParsePosition_01() {
		assertEquals(new Position(0.33d, true), Position.parsePosition("33%"));
	}
	@Test
	public void testParsePosition_02() {
		assertEquals(new Position(15d, false), Position.parsePosition("15"));
	}
	@Test
	public void testParsePosition_03() {
		assertEquals(new Position(0.2d, true), Position.parsePosition("20%"));
	}
	@Test
	public void testParsePosition_04() {
		assertEquals(new Position(0d, true), Position.parsePosition("0%"));
	}
	@Test
	public void testParsePosition_05() {
		assertEquals(new Position(1d, true), Position.parsePosition("100%"));
	}

	@Test
	public void testMakeAbsolute_01() {
		assertEquals(10, new Position(0.33d, true).makeAbsolute(30));
	}
	@Test
	public void testMakeAbsolute_02() {
		assertEquals(15, new Position(15d, false).makeAbsolute(30));
	}
	@Test
	public void testMakeAbsolute_03() {
		assertEquals(6, new Position(0.2d, true).makeAbsolute(28));
	}
	@Test
	public void testMakeAbsolute_04() {
		assertEquals(0, new Position(0d, true).makeAbsolute(28));
	}
	@Test
	public void testMakeAbsolute_05() {
		assertEquals(28, new Position(1d, true).makeAbsolute(28));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testNonIntegerAbsoluteValue() {
		new Position(1.1, false);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testNegativeValue() {
		new Position(-0.1, false);
	}

}
