package org.daisy.dotify.text.impl;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.text.IntegerOutOfRange;
import org.daisy.dotify.text.impl.BasicInteger2Text;
import org.daisy.dotify.text.impl.NoInt2TextLocalization;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class NoInteger2TextTest {
	private BasicInteger2Text int2text = new NoInt2TextLocalization();

	@Test
	public void testNumber_01() throws IntegerOutOfRange {
		assertEquals("nittini", int2text.intToText(99));
	}

	@Test
	public void testNumber_02() throws IntegerOutOfRange {
		assertEquals("ett hundre og trettito", int2text.intToText(132));
	}

	@Test
	public void testNumber_03() throws IntegerOutOfRange {
		assertEquals("ett tusen sju hundre og femtiåtte", int2text.intToText(1758));
	}

	@Test
	public void testNumber_04() throws IntegerOutOfRange {
		assertEquals("minus tolv", int2text.intToText(-12));
	}

	@Test
	public void testNumber_05() throws IntegerOutOfRange {
		assertEquals("femten", int2text.intToText(15));
	}

	@Test
	public void testNumber_06() throws IntegerOutOfRange {
		assertEquals("to hundre og trettito", int2text.intToText(232));
	}

	@Test
	public void testNumber_07() throws IntegerOutOfRange {
		assertEquals("fem", int2text.intToText(5));
	}

	@Test
	public void testNumber_08() throws IntegerOutOfRange {
		assertEquals("tjueåtte", int2text.intToText(28));
	}

	@Test
	public void testNumber_09() throws IntegerOutOfRange {
		assertEquals("ett hundre", int2text.intToText(100));
	}

	@Test
	public void testNumber_10() throws IntegerOutOfRange {
		assertEquals("ett tusen", int2text.intToText(1000));
	}

}
