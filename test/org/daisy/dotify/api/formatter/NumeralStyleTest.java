package org.daisy.dotify.api.formatter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumeralStyleTest {

	@Test
	public void testNumeralStyleDefault() {
		assertEquals("1", NumeralStyle.DEFAULT.format(1));
	}

	@Test
	public void testNumeralStyleRoman() {
		assertEquals("III", NumeralStyle.ROMAN.format(3));
	}

	@Test
	public void testNumeralStyleAlpha() {
		assertEquals("C", NumeralStyle.ALPHA.format(3));
	}
}
