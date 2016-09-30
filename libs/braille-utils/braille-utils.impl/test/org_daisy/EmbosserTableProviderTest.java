package org_daisy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import org.daisy.braille.api.table.BrailleConstants;
import org.daisy.braille.api.table.BrailleConverter;
import org.junit.Test;

public class EmbosserTableProviderTest {
	
	/*
	@Test
	public void testFeatureReplacement() {
		EmbosserTableProvider bt = new EmbosserTableProvider();
		assertEquals('\u2800', bt.getFeature("replacement"));
	}
	
	@Test
	public void testFeatureFallback() {
		EmbosserTableProvider bt = new EmbosserTableProvider();
		assertEquals(EightDotFallbackMethod.MASK, bt.getFeature("fallback"));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetUnknownFeature() {
		EmbosserTableProvider bt = new EmbosserTableProvider();
		bt.getFeature("unknown-feature");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testSetUnknownFeature() {
		EmbosserTableProvider bt = new EmbosserTableProvider();
		bt.setFeature("unknown-feature", null);
	}*/
	/*
	@Test
	public void testListLength() {
		EmbosserTableProvider bt = new EmbosserTableProvider();
		assertEquals("Assert that all tables have tests by counting the list length", 7, bt.list().size());
	}*/
/*
	@Test
	public void testTableEN_US() {
		EmbosserTableProvider bt = new EmbosserTableProvider();
		String input = BrailleConstants.BRAILLE_PATTERNS_64;
		EmbosserTableProvider.TableType t = EmbosserTableProvider.TableType.EN_US;
		BrailleConverter ta = bt.newTable(t);
		String text = ta.toText(input);
		String braille = ta.toBraille(text);
		assertEquals("Assert that conversion is reversible", input, braille);
		assertEquals("Assert that text has been transformed", " A1B'K2L@CIF/MSP\"E3H9O6R^DJG>NTQ,*5<-U8V.%[$+X!&;:4\\0Z7(_?W]#Y)=", text);
		assertTrue("Assert that table does not support 8-dot", !ta.supportsEightDot());
		assertEquals("Assert that UTF-8 is the preferred charset", Charset.forName("UTF-8"), ta.getPreferredCharset());
	}*/
	
	//TODO: more tests
	
	@Test
	public void testTableUnicode() {
		EmbosserTableProvider bt = new EmbosserTableProvider();
		String input = BrailleConstants.BRAILLE_PATTERNS_256;
		EmbosserTableProvider.TableType t = EmbosserTableProvider.TableType.UNICODE_BRAILLE;
		BrailleConverter ta = bt.newTable(t);
		String text = ta.toText(input);
		String braille = ta.toBraille(text);
		assertEquals("Assert that conversion is reversible", input, braille);
		assertEquals("Assert that text has been transformed", BrailleConstants.BRAILLE_PATTERNS_256, text);
		assertTrue("Assert that table supports 8-dot", ta.supportsEightDot());
		assertEquals("Assert that UTF-8 is the preferred charset", Charset.forName("UTF-8"), ta.getPreferredCharset());
	}

	@Test
	public void testNABCC() {
		EmbosserTableProvider bt = new EmbosserTableProvider();
		String input = BrailleConstants.BRAILLE_PATTERNS_64;
		BrailleConverter nabcc = bt.newTable(EmbosserTableProvider.TableType.NABCC);
		BrailleConverter nabcc8dot = bt.newTable(EmbosserTableProvider.TableType.NABCC_8DOT);
		assertEquals("Assert that first 64 characters of NABCC and NABCC_8DOT are equal",
				nabcc.toText(input), nabcc8dot.toText(input));
	}
}
