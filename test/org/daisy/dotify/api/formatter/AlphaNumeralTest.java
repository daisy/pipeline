package org.daisy.dotify.api.formatter;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.formatter.AlphaNumeral;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class AlphaNumeralTest {

	@Test
	public void test_01() {
		assertEquals("Test 1", "A", AlphaNumeral.int2alpha(1));
	}

	@Test
	public void test_02() {
		assertEquals("Test 2", "C", AlphaNumeral.int2alpha(3));
	}

	@Test
	public void test_03() {
		assertEquals("Test 3", "Z", AlphaNumeral.int2alpha(26));
	}

	@Test
	public void test_04() {
		assertEquals("Test 4", "AA", AlphaNumeral.int2alpha(26 + 1));
	}

	@Test
	public void test_05() {
		assertEquals("Test 5", "AC", AlphaNumeral.int2alpha(26 + 3));
	}

	@Test
	public void test_06() {
		assertEquals("Test 6", "AZ", AlphaNumeral.int2alpha(26 + 26));
	}

	@Test
	public void test_07() {
		assertEquals("Test 7", "BA", AlphaNumeral.int2alpha(26 * 2 + 1));
	}

	@Test
	public void test_08() {
		assertEquals("Test 8", "AAA", AlphaNumeral.int2alpha(26 * 26 + 26 + 1));
	}

	@Test
	public void test_09() {
		assertEquals("Test 9", "ABBA", AlphaNumeral.int2alpha(26 * 26 * 26 + 26 * 26 * 2 + 26 * 2 + 1));
	}
}
