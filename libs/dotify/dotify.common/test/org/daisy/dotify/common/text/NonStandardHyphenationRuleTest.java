package org.daisy.dotify.common.text;
import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;


import org.daisy.dotify.common.text.NonStandardHyphenationInfo;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class NonStandardHyphenationRuleTest {

	@Test
	public void testApply_01() {
		NonStandardHyphenationInfo nshi = new NonStandardHyphenationInfo("c\u00adk", "k\u00adk");
		String input = "Zuc\u00adker";
		assertEquals("Zuk\u00adker", nshi.apply(input, 2)); 
		assertEquals(2, NonStandardHyphenationInfo.getHeadLength(input, 2));
	}
	
	@Test
	public void testApply_02() {
		NonStandardHyphenationInfo nshi = new NonStandardHyphenationInfo("-", "-\u200b-");
		String input = "kong-fu";
		assertEquals("kong-\u200b-fu", nshi.apply(input, 4)); 
	}
	
	@Test
	public void testHeadLength_01() {
		String input = "c\u200bk";
		assertEquals(1, NonStandardHyphenationInfo.getHeadLength(input, 0));
	}
}
