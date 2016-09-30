package org.daisy.braille.impl.table;
import static org.junit.Assert.assertEquals;

import org.daisy.braille.impl.table.StringTranslator.MatchMode;
import org.junit.Test;

public class StringTranslatorTest {
	
	public StringTranslator getTranslator() {
		StringTranslator t = new StringTranslator();
		t.addToken("a", "x");
		t.addToken("ab", "y");
		t.addToken("aba", "z");
		return t;
	}

	@Test (expected=IllegalArgumentException.class)
	public void testDuplicateTokens() {
		StringTranslator t = getTranslator();
		t.addToken("ab", "å");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testUnknownTokens() {
		StringTranslator t = getTranslator();
		t.translate("aaabaabc");
	}

	@Test (expected=NullPointerException.class)
	public void testNullInput() {
		StringTranslator t = getTranslator();
		t.translate(null);
	}

	@Test
	public void testGreedyTranslation() {
		StringTranslator t = getTranslator();
		assertEquals("Assert that greedy translation is correct", "x", t.translate("a"));
		assertEquals("Assert that greedy translation is correct", "yy", t.translate("abab"));
		assertEquals("Assert that greedy translation is correct", "yz", t.translate("ababa"));
		assertEquals("Assert that greedy translation is correct", "xxzy", t.translate("aaabaab"));
	}
	
	@Test
	public void testReluctantTranslation() {
		StringTranslator t = getTranslator();
		t.setMatchMode(MatchMode.RELUCTANT);
		assertEquals("Assert that greedy translation is correct", "x", t.translate("a"));
		assertEquals("Assert that greedy translation is correct", "yy", t.translate("abab"));
		assertEquals("Assert that greedy translation is correct", "yyx", t.translate("ababa"));
		assertEquals("Assert that greedy translation is correct", "xxyxy", t.translate("aaabaab"));
	}

	@Test
	public void testEmptyInput() {
		StringTranslator t = getTranslator();
		t.translate("");
	}

}
