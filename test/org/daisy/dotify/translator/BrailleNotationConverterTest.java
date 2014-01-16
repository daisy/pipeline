package org.daisy.dotify.translator;

import org.junit.Test;

public class BrailleNotationConverterTest {

	@Test
	public void testBrailleNotationConversion() {
		String input = "p12456p1p4p0p12345678p2p3p4p5p6p7p8p1";
		BrailleNotationConverter bnc = new BrailleNotationConverter("p");
		org.junit.Assert.assertEquals("⠻⠁⠈⠀⣿⠂⠄⠈⠐⠠⡀⢀⠁", bnc.parseBrailleNotation(input));
	}
}
