package com_braillo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import org.daisy.braille.BrailleConstants;
import org.daisy.braille.table.BrailleConverter;
import org.junit.Test;

public class BrailloTableProviderTest {
	private static BrailloTableProvider bt = new BrailloTableProvider(); 
	/*
	@Test
	public void testFeatureReplacement() {
		//bt = new BrailloTableProvider();
		assertEquals('\u2800', bt.getFeature("replacement"));
	}
	
	@Test
	public void testFeatureFallback() {
		//BrailloTableProvider bt = new BrailloTableProvider();
		assertEquals(EightDotFallbackMethod.MASK, bt.getFeature("fallback"));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetUnknownFeature() {
		//BrailloTableProvider bt = new BrailloTableProvider();
		bt.getFeature("unknown-feature");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testSetUnknownFeature() {
		//BrailloTableProvider bt = new BrailloTableProvider();
		bt.setFeature("unknown-feature", null);
	}*/
	
	@Test
	public void testListLength() {
		//BrailloTableProvider bt = new BrailloTableProvider();
		assertEquals("Assert that all tables have tests by counting the list length", 4, bt.list().size());
	}
	
	@Test
	public void testTable6Dot_001_00() {
		//BrailloTableProvider bt = new BrailloTableProvider();
		String input = BrailleConstants.BRAILLE_PATTERNS_64;
		BrailloTableProvider.TableType t = BrailloTableProvider.TableType.BRAILLO_6DOT_001_00;
		BrailleConverter ta = bt.newTable(t);
		String text = ta.toText(input);
		String braille = ta.toBraille(text);
		assertEquals("Assert that conversion is reversible", input, braille);
		assertEquals("Assert that text has been transformed", " A1B'K2L@CIF/MSP\"E3H9O6R^DJG>NTQ,*5<-U8V.%[$+X!&;:4\\0Z7(_?W]#Y)=", text);
		assertTrue("Assert that table does not support 8-dot", !ta.supportsEightDot());
		assertEquals("Assert that UTF-8 is the preferred charset", Charset.forName("UTF-8"), ta.getPreferredCharset());
	}
	
	@Test
	public void testTable6Dot_044_00() {
		//BrailloTableProvider bt = new BrailloTableProvider();
		String input = BrailleConstants.BRAILLE_PATTERNS_64;
		BrailloTableProvider.TableType t = BrailloTableProvider.TableType.BRAILLO_6DOT_044_00;
		BrailleConverter ta = bt.newTable(t);
		String text = ta.toText(input);
		String braille = ta.toBraille(text);
		assertEquals("Assert that conversion is reversible", input, braille);
		assertEquals("Assert that text has been transformed", " a1b'k2l@cif/msp\"e3h9o6r^djg>ntq,*5<-u8v.%[$+x!&;:4\\0z7(_?w]#y)=", text);
		assertTrue("Assert that table does not support 8-dot", !ta.supportsEightDot());
		assertEquals("Assert that UTF-8 is the preferred charset", Charset.forName("UTF-8"), ta.getPreferredCharset());
	}

	@Test
	public void testTable6Dot_046_01() {
		//BrailloTableProvider bt = new BrailloTableProvider();
		String input = BrailleConstants.BRAILLE_PATTERNS_64;
		BrailloTableProvider.TableType t = BrailloTableProvider.TableType.BRAILLO_6DOT_046_01;
		BrailleConverter ta = bt.newTable(t);
		String text = ta.toText(input);
		String braille = ta.toBraille(text);
		assertEquals("Assert that conversion is reversible", input, braille);
		assertEquals("Assert that text has been transformed", " a,b'k;l^cif/msp!e:h*o+r\"djg[ntq_1?2-u<v%396]x\\&#5.8>z=($4w70y)@", text);
		assertTrue("Assert that table does not support 8-dot", !ta.supportsEightDot());
		assertEquals("Assert that UTF-8 is the preferred charset", Charset.forName("UTF-8"), ta.getPreferredCharset());
	}
	
	@Test
	public void testTable6Dot_047_01() {
		//BrailloTableProvider bt = new BrailloTableProvider();
		String input = BrailleConstants.BRAILLE_PATTERNS_64;
		BrailloTableProvider.TableType t = BrailloTableProvider.TableType.BRAILLO_6DOT_047_01;
		BrailleConverter ta = bt.newTable(t);
		String text = ta.toText(input);
		String braille = ta.toBraille(text);
		assertEquals("Assert that conversion is reversible", input, braille);
		assertEquals("Assert that text has been transformed", " A,B.K;L`CIF/MSP'E:H@O!RaDJG[NTQ*]?r-U\"Vqm\\h&Xli_e%u$Z=k|dWg#Ynj", text);
		assertTrue("Assert that table does not support 8-dot", !ta.supportsEightDot());
		assertEquals("Assert that UTF-8 is the preferred charset", Charset.forName("UTF-8"), ta.getPreferredCharset());
	}

}
