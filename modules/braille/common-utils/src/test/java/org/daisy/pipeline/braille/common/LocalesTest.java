package org.daisy.pipeline.braille.common;

import java.util.Locale;

import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LocalesTest {
	
	@Test
	public void testParseLocale() {
		assertEquals(new Locale("en", "US"), parseLocale("en-us"));
		assertEquals(new Locale("en", "US"), parseLocale("en_us"));
		assertEquals(new Locale("en", "US"), parseLocale("en_US"));
	}
}
