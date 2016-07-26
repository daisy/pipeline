package org.daisy.dotify.obfl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class XMLParserBaseTest {

	public XMLParserBaseTest() {
	}
	
	@Test
	public void testNormalizeSpace_01() {
		assertEquals("a", XMLParserBase.normalizeSpace(" a "));
	}
	
	@Test
	public void testNormalizeSpace_02() {
		assertEquals("a b", XMLParserBase.normalizeSpace(" a \n\t  b  "));
	}
	
	@Test
	public void testNormalizeSpace_03() {
		assertEquals("", XMLParserBase.normalizeSpace("  "));
	}

	@Test
	public void testNormalizeSpace_04() {
		assertEquals("", XMLParserBase.normalizeSpace(""));
	}

	@Test
	public void testNormalizeSpace_05() {
		assertEquals("a", XMLParserBase.normalizeSpace("a"));
	}

}
