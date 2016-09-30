package org.daisy.dotify.impl.text;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.text.IntegerOutOfRange;
import org.junit.Test;

public class FiInteger2TextTest {
	private BasicInteger2Text int2text = new FiInt2TextLocalization();

	@Test
	public void testNumber_01() throws IntegerOutOfRange {
		assertEquals("yhdeksänkymmentäyhdeksän", int2text.intToText(99));
	}

	@Test
	public void testNumber_02() throws IntegerOutOfRange {
		assertEquals("sata ja kolmekymmentäkaksi", int2text.intToText(132));
	}

	@Test
	public void testNumber_03() throws IntegerOutOfRange {
		assertEquals("tuhatseitsemänsataaviisikymmentäkahdeksan", int2text.intToText(1758));
	}

	@Test
	public void testNumber_04() throws IntegerOutOfRange {
		assertEquals("miinus kaksitoista", int2text.intToText(-12));
	}

	@Test
	public void testNumber_05() throws IntegerOutOfRange {
		assertEquals("viisitoista", int2text.intToText(15));
	}

	@Test
	public void testNumber_06() throws IntegerOutOfRange {
		assertEquals("kaksisataakolmekymmentäkaksi", int2text.intToText(232));
	}

	@Test
	public void testNumber_07() throws IntegerOutOfRange {
		assertEquals("viisi", int2text.intToText(5));
	}

	@Test
	public void testNumber_08() throws IntegerOutOfRange {
		assertEquals("kaksikymmentäkahdeksan", int2text.intToText(28));
	}

	@Test
	public void testNumber_09() throws IntegerOutOfRange {
		assertEquals("sata", int2text.intToText(100));
	}

	@Test
	public void testNumber_10() throws IntegerOutOfRange {
		assertEquals("tuhat", int2text.intToText(1000));
	}
}
