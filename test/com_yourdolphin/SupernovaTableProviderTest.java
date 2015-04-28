package com_yourdolphin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import org.daisy.braille.BrailleConstants;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.junit.Test;

public class SupernovaTableProviderTest {
	private static SupernovaTableProvider bt = new SupernovaTableProvider(); 
	
	/*
	@Test
	public void testFeatureReplacement() {
		assertEquals('\u2800', bt.getFeature("replacement"));
	}
	
	@Test
	public void testFeatureFallback() {
		assertEquals(EightDotFallbackMethod.MASK, bt.getFeature("fallback"));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetUnknownFeature() {
		bt.getFeature("unknown-feature");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testSetUnknownFeature() {
		bt.setFeature("unknown-feature", null);
	}*/
	
	@Test
	public void testListLength() {
		assertEquals("Assert that all tables have tests by counting the list length", 1, bt.list().size());
	}
	
	@Test
	public void testTableSV_SE_6DOT() {
		//BrailloTableProvider bt = new BrailloTableProvider();
		String input = BrailleConstants.BRAILLE_PATTERNS_64;
		SupernovaTableProvider.TableType t = SupernovaTableProvider.TableType.SV_SE_6DOT;
		BrailleConverter ta = bt.newTable(t);
		String text = ta.toText(input);
		String braille = ta.toBraille(text);
		assertEquals("Assert that conversion is reversible", input, braille);
		assertEquals("Assert that text has been transformed", " a,b.k;l@cif/msp'e:h*o!rødjgäntq_å?ê-u(v`îöë§xèç\"û+ü)z=àœôwï#yùé", text);
		assertTrue("Assert that table does not support 8-dot", !ta.supportsEightDot());
		assertEquals("Assert that UTF-8 is the preferred charset", Charset.forName("UTF-8"), ta.getPreferredCharset());
	}

}
