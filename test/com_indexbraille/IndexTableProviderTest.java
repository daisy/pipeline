package com_indexbraille;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import org.daisy.braille.BrailleConstants;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;
import org.junit.Test;

public class IndexTableProviderTest {
	private IndexTableProvider bt = new IndexTableProvider(); 
	
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
	public void testTransparent6dotTable() {
		String input = BrailleConstants.BRAILLE_PATTERNS_64;
		IndexTableProvider.TableType t = IndexTableProvider.TableType.INDEX_TRANSPARENT_6DOT;
		BrailleConverter ta = bt.newTable(t);
		String text = ta.toText(input);
		String braille = ta.toBraille(text);
		assertEquals("Assert that conversion is reversible", input, braille);
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<64; i++) {
			sb.append((char)((i & 0x07) + ((i & 0x38) << 1)));
		}
		assertEquals("Assert that text has been transformed", sb.toString(), text);
		assertTrue("Assert that table does not support 8-dot", !ta.supportsEightDot());
		assertEquals("Assert that UTF-8 is the preferred charset", Charset.forName("US-ASCII"), ta.getPreferredCharset());
	}

}
