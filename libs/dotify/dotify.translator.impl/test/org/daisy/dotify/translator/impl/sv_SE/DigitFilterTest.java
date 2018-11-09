package org.daisy.dotify.translator.impl.sv_SE;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.translator.impl.sv_SE.DigitFilter;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class DigitFilterTest {
	
	@Test
	public void testDigit() {
		DigitFilter c = new DigitFilter();

		assertEquals("Test digits.", "⠼1.2", c.filter("1.2"));
	}
	
	@Test
	public void testResetDigit() {
		DigitFilter c = new DigitFilter();

		assertEquals("Test digits.", "⠼1.2⠱a", c.filter("1.2a"));
	}
	
	@Test
	public void testMixedDigit() {
		DigitFilter c = new DigitFilter();

		assertEquals("Test digits.", "Speed: ⠼134 km/h", c.filter("Speed: 134 km/h"));
	}

}
