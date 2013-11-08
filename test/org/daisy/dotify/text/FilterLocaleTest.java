package org.daisy.dotify.text;
import static org.junit.Assert.assertTrue;

import org.daisy.dotify.text.FilterLocale;
import org.junit.Test;

public class FilterLocaleTest {
	
	@Test
	public void testIsA_01() {
		FilterLocale inLoc = FilterLocale.parse("sv-SE");
		FilterLocale refLoc = FilterLocale.parse("sv");
		assertTrue("Assert sv-SE is a sv", inLoc.isA(refLoc));
	}
	@Test
	public void testIsA_02() {
		FilterLocale inLoc = FilterLocale.parse("sv-se-test");
		FilterLocale refLoc = FilterLocale.parse("sv-SE-test");
		assertTrue("Assert sv-SE-test is a sv-SE-test", inLoc.isA(refLoc));
	}
	
	@Test
	public void testIsNotA_01() {
		FilterLocale inLoc = FilterLocale.parse("sv-FI");
		FilterLocale refLoc = FilterLocale.parse("da-DK");
		assertTrue("Assert sv-FI is not a da-DK", !inLoc.isA(refLoc));
	}
	@Test
	public void testIsNotA_02() {
		FilterLocale inLoc = FilterLocale.parse("sv-SE");
		FilterLocale refLoc = FilterLocale.parse("sv-SE-test");
		assertTrue("Assert sv-SE is not a sv-SE-test", !inLoc.isA(refLoc));
	}
	@Test
	public void testIsNotA_03() {
		FilterLocale inLoc = FilterLocale.parse("sv");
		FilterLocale refLoc = FilterLocale.parse("da");
		assertTrue("Assert sv is not a da", !inLoc.isA(refLoc));
	}
	
	@Test
	public void testEquals() {
		FilterLocale inLoc = FilterLocale.parse("sv-SE-test");
		FilterLocale refLoc = FilterLocale.parse("sv-se-test");
		assertTrue("Assert sv-SE-test is a sv-SE-test", inLoc.equals(refLoc));
	}

}
