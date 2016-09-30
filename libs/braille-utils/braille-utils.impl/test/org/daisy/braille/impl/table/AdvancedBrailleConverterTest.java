package org.daisy.braille.impl.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import org.daisy.braille.api.embosser.EightDotFallbackMethod;
import org.daisy.braille.api.table.BrailleConstants;
import org.daisy.braille.impl.table.StringTranslator.MatchMode;
import org.junit.Test;
public class AdvancedBrailleConverterTest {
	private final static String[] glyphs = new String[]{
		"a", "ab", "aba", "c", "d", "e", "f", "g", "h", "i", 
		"a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1", "i1", "j1",
		"a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", "i2", "j2",
		"a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3", "i3", "j3",
		"a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4", "i4", "j4",
		"a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5", "i5", "j5",
		"a6", "b6", "c6", "d6"
		};

	@Test
	public void testGreedyMatch() {
		AdvancedBrailleConverter bc = new AdvancedBrailleConverter(glyphs, Charset.forName("utf-8"), EightDotFallbackMethod.MASK, '\u2800', true, MatchMode.GREEDY);
		assertEquals("Assert greedy match", "⠀⠀⠂⠁⠊", bc.toBraille("aAABaaba1"));
	}
	
	@Test
	public void testReluctantMatch() {
		AdvancedBrailleConverter bc = new AdvancedBrailleConverter(glyphs, Charset.forName("utf-8"), EightDotFallbackMethod.MASK, '\u2800', true, MatchMode.RELUCTANT);
		assertEquals("Assert reluctant match", "⠀⠀⠁⠀⠁⠊", bc.toBraille("aAABaaba1"));
	}
	
	@Test
	public void testProperties() {
		AdvancedBrailleConverter bc = new AdvancedBrailleConverter(glyphs, Charset.forName("utf-8"), EightDotFallbackMethod.MASK, '\u2800', true, MatchMode.GREEDY);
		String input = BrailleConstants.BRAILLE_PATTERNS_64;
		String text = bc.toText(input);
		String braille = bc.toBraille(text);
		assertEquals("Assert that conversion is reversible", input, braille);
		assertEquals("Assert that text has been transformed", "aababacdefghia1b1c1d1e1f1g1h1i1j1a2b2c2d2e2f2g2h2i2j2a3b3c3d3e3f3g3h3i3j3a4b4c4d4e4f4g4h4i4j4a5b5c5d5e5f5g5h5i5j5a6b6c6d6", text);
		assertTrue("Assert that table does not support 8-dot", !bc.supportsEightDot());
		assertEquals("Assert that UTF-8 is the preferred charset", Charset.forName("UTF-8"), bc.getPreferredCharset());

	}
}
