package org.daisy.dotify.formatter.impl.obfl;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.formatter.impl.obfl.XMLParserBase;
import org.junit.Test;

@SuppressWarnings("javadoc")
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
