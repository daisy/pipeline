package org.daisy.dotify.impl.text;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.text.IntegerOutOfRange;
import org.junit.Test;

public class EnInteger2TextTest {
	private BasicInteger2Text int2text = new EnInt2TextLocalization();

	@Test
	public void testNumber_01() throws IntegerOutOfRange {
		assertEquals("ninety-nine", int2text.intToText(99));
	}

	@Test
	public void testNumber_02() throws IntegerOutOfRange {
		assertEquals("one hundred thirty-two", int2text.intToText(132));
	}

	@Test
	public void testNumber_03() throws IntegerOutOfRange {
		assertEquals("one thousand seven hundred fifty-eight", int2text.intToText(1758));
	}

	@Test
	public void testNumber_04() throws IntegerOutOfRange {
		assertEquals("minus twelve", int2text.intToText(-12));
	}

	@Test
	public void testNumber_05() throws IntegerOutOfRange {
		assertEquals("fifteen", int2text.intToText(15));
	}

	@Test
	public void testNumber_06() throws IntegerOutOfRange {
		assertEquals("two hundred thirty-two", int2text.intToText(232));
	}

	@Test
	public void testNumber_07() throws IntegerOutOfRange {
		assertEquals("five", int2text.intToText(5));
	}

	@Test
	public void testNumber_08() throws IntegerOutOfRange {
		assertEquals("twenty-eight", int2text.intToText(28));
	}

	@Test
	public void testNumber_09() throws IntegerOutOfRange {
		assertEquals("one hundred", int2text.intToText(100));
	}

	@Test
	public void testNumber_10() throws IntegerOutOfRange {
		assertEquals("one thousand", int2text.intToText(1000));
	}

}
