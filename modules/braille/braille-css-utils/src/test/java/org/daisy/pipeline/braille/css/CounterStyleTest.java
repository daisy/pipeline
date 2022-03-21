package org.daisy.pipeline.braille.css;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class CounterStyleTest {

	@Test
	public void testCounterRepresentation() {
		ArrayList<String> symbols = new ArrayList<String>();
		symbols.add("a");
		symbols.add("b");
		symbols.add("c");
		symbols.add("d");
		symbols.add("e");
		symbols.add("f");
		symbols.add("g");
		symbols.add("h");
		Assert.assertEquals("", CounterStyle.counterRepresentationAlphabetic(0, symbols).orElse(""));
		Assert.assertEquals("a", CounterStyle.counterRepresentationAlphabetic(1, symbols).orElse(""));
		Assert.assertEquals("b", CounterStyle.counterRepresentationAlphabetic(2, symbols).orElse(""));
		Assert.assertEquals("add", CounterStyle.counterRepresentationAlphabetic(100, symbols).orElse(""));
		Assert.assertEquals("", CounterStyle.counterRepresentationAlphabetic(-1, symbols).orElse(""));
		Assert.assertEquals("h", CounterStyle.counterRepresentationCyclic(0 , symbols));
		Assert.assertEquals("a", CounterStyle.counterRepresentationCyclic(1 , symbols));
		Assert.assertEquals("b", CounterStyle.counterRepresentationCyclic(2 , symbols));
		Assert.assertEquals("d", CounterStyle.counterRepresentationCyclic(100 , symbols));
		Assert.assertEquals("g", CounterStyle.counterRepresentationCyclic(-1 , symbols));
		Assert.assertEquals("", CounterStyle.counterRepresentationFixed(0 , symbols).orElse(""));
		Assert.assertEquals("a", CounterStyle.counterRepresentationFixed(1 , symbols).orElse(""));
		Assert.assertEquals("b", CounterStyle.counterRepresentationFixed(2 , symbols).orElse(""));
		Assert.assertEquals("", CounterStyle.counterRepresentationFixed(100 , symbols).orElse(""));
		Assert.assertEquals("", CounterStyle.counterRepresentationFixed(-1 , symbols).orElse(""));
		Assert.assertEquals("a", CounterStyle.counterRepresentationNumeric(0 , symbols, "-"));
		Assert.assertEquals("b", CounterStyle.counterRepresentationNumeric(1 , symbols, "-"));
		Assert.assertEquals("c", CounterStyle.counterRepresentationNumeric(2 , symbols, "-"));
		Assert.assertEquals("bee", CounterStyle.counterRepresentationNumeric(100 , symbols, "-"));
		Assert.assertEquals("-b", CounterStyle.counterRepresentationNumeric(-1 , symbols, "-"));
		Assert.assertEquals("", CounterStyle.counterRepresentationSymbolic(0 , symbols).orElse(""));
		Assert.assertEquals("a", CounterStyle.counterRepresentationSymbolic(1 , symbols).orElse(""));
		Assert.assertEquals("b", CounterStyle.counterRepresentationSymbolic(2 , symbols).orElse(""));
		Assert.assertEquals("ddddddddddddd", CounterStyle.counterRepresentationSymbolic(100 , symbols).orElse(""));
		Assert.assertEquals("", CounterStyle.counterRepresentationSymbolic(-1 , symbols).orElse(""));
	}

	@Test
	public void testPredefinedCounterStyles() {
		Assert.assertEquals("a", CounterStyle.LOWER_ALPHA.format(1));
		Assert.assertEquals("j", CounterStyle.LOWER_ALPHA.format(10));
		Assert.assertEquals("aa", CounterStyle.LOWER_ALPHA.format(27));
		Assert.assertEquals("0", CounterStyle.LOWER_ALPHA.format(0));
		Assert.assertEquals("-1", CounterStyle.LOWER_ALPHA.format(-1));
		Assert.assertEquals("i", CounterStyle.LOWER_ROMAN.format(1));
		Assert.assertEquals("xcix", CounterStyle.LOWER_ROMAN.format(99));
		Assert.assertEquals("0", CounterStyle.LOWER_ROMAN.format(0));
		Assert.assertEquals("-1", CounterStyle.LOWER_ROMAN.format(-1));
	}
}
