package org.daisy.dotify.tools;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StringToolsTest {
	
	@Test
	public void testFillChar() {
		String expected = "     ";
		String actual = StringTools.fill(' ', 5);
		//Test
		assertEquals("Fill a string with char", expected, actual);
	}
	
	@Test
	public void testFillString() {
		String expected = " .  .  .";
		String actual = StringTools.fill(" . ", 8);
		//Test
		assertEquals("Fill a string with string", expected, actual);
	}
	
	@Test
	public void testLength() {
		int expected = 3;		
		int actual = StringTools.length(
					new StringBuilder().append(" ").appendCodePoint(0x20B9F).append(" ").toString()
				);
		//Test
		assertEquals("Fill a string with string", expected, actual);
	}

}
