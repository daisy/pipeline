package org.daisy.pipeline.css;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.css.CounterStyleImpl.AdditiveTuple;

import org.junit.Assert;
import org.junit.Test;

public class CounterStyleTest {

	@Test
	public void testCounterRepresentation() {
		List<String> symbols = new ArrayList<String>();
		symbols.add("a");
		symbols.add("b");
		symbols.add("c");
		symbols.add("d");
		symbols.add("e");
		symbols.add("f");
		symbols.add("g");
		symbols.add("h");
		Assert.assertEquals("", CounterStyleImpl.counterRepresentationAlphabetic(0, symbols).orElse(""));
		Assert.assertEquals("a", CounterStyleImpl.counterRepresentationAlphabetic(1, symbols).orElse(""));
		Assert.assertEquals("b", CounterStyleImpl.counterRepresentationAlphabetic(2, symbols).orElse(""));
		Assert.assertEquals("add", CounterStyleImpl.counterRepresentationAlphabetic(100, symbols).orElse(""));
		Assert.assertEquals("", CounterStyleImpl.counterRepresentationAlphabetic(-1, symbols).orElse(""));
		Assert.assertEquals("h", CounterStyleImpl.counterRepresentationCyclic(0, symbols));
		Assert.assertEquals("a", CounterStyleImpl.counterRepresentationCyclic(1, symbols));
		Assert.assertEquals("b", CounterStyleImpl.counterRepresentationCyclic(2, symbols));
		Assert.assertEquals("d", CounterStyleImpl.counterRepresentationCyclic(100, symbols));
		Assert.assertEquals("g", CounterStyleImpl.counterRepresentationCyclic(-1, symbols));
		Assert.assertEquals("", CounterStyleImpl.counterRepresentationFixed(0, symbols).orElse(""));
		Assert.assertEquals("a", CounterStyleImpl.counterRepresentationFixed(1, symbols).orElse(""));
		Assert.assertEquals("b", CounterStyleImpl.counterRepresentationFixed(2, symbols).orElse(""));
		Assert.assertEquals("", CounterStyleImpl.counterRepresentationFixed(100, symbols).orElse(""));
		Assert.assertEquals("", CounterStyleImpl.counterRepresentationFixed(-1, symbols).orElse(""));
		Assert.assertEquals("a", CounterStyleImpl.counterRepresentationNumeric(0, symbols, "-"));
		Assert.assertEquals("b", CounterStyleImpl.counterRepresentationNumeric(1, symbols, "-"));
		Assert.assertEquals("c", CounterStyleImpl.counterRepresentationNumeric(2, symbols, "-"));
		Assert.assertEquals("bee", CounterStyleImpl.counterRepresentationNumeric(100, symbols, "-"));
		Assert.assertEquals("-b", CounterStyleImpl.counterRepresentationNumeric(-1, symbols, "-"));
		Assert.assertEquals("", CounterStyleImpl.counterRepresentationSymbolic(0, symbols).orElse(""));
		Assert.assertEquals("a", CounterStyleImpl.counterRepresentationSymbolic(1, symbols).orElse(""));
		Assert.assertEquals("b", CounterStyleImpl.counterRepresentationSymbolic(2, symbols).orElse(""));
		Assert.assertEquals("ddddddddddddd", CounterStyleImpl.counterRepresentationSymbolic(100, symbols).orElse(""));
		Assert.assertEquals("", CounterStyleImpl.counterRepresentationSymbolic(-1, symbols).orElse(""));
	}

	@Test
	public void testCounterRepresentationAdditive() {
		List<AdditiveTuple> symbols = new ArrayList<>();
		symbols.add(new AdditiveTuple(1000, "⠍"));
		symbols.add(new AdditiveTuple(900, "⠉⠍"));
		symbols.add(new AdditiveTuple(500, "⠙"));
		symbols.add(new AdditiveTuple(400, "⠉⠙"));
		symbols.add(new AdditiveTuple(100, "⠉"));
		symbols.add(new AdditiveTuple(90, "⠭⠉"));
		symbols.add(new AdditiveTuple(50, "⠇"));
		symbols.add(new AdditiveTuple(40, "⠭⠇"));
		symbols.add(new AdditiveTuple(10, "⠭"));
		symbols.add(new AdditiveTuple(9, "⠊⠭"));
		symbols.add(new AdditiveTuple(5, "⠧"));
		symbols.add(new AdditiveTuple(4, "⠊⠧"));
		symbols.add(new AdditiveTuple(1, "⠊"));
		Assert.assertEquals("⠍⠉⠍⠭⠉⠧⠊⠊⠊", CounterStyleImpl.counterRepresentationAdditive(1998, symbols).orElse(""));
	}

	@Test
	public void testPredefinedCounterStyles() {
		Assert.assertEquals("100", CounterStyle.DECIMAL.format(100));
		Assert.assertEquals("-25", CounterStyle.DECIMAL.format(-25));
		Assert.assertEquals("a", CounterStyle.LOWER_ALPHA.format(1));
		Assert.assertEquals("j", CounterStyle.LOWER_ALPHA.format(10));
		Assert.assertEquals("aa", CounterStyle.LOWER_ALPHA.format(27));
		Assert.assertEquals("cv", CounterStyle.LOWER_ALPHA.format(100));
		Assert.assertEquals("0", CounterStyle.LOWER_ALPHA.format(0));
		Assert.assertEquals("-1", CounterStyle.LOWER_ALPHA.format(-1));
		Assert.assertEquals("i", CounterStyle.LOWER_ROMAN.format(1));
		Assert.assertEquals("xcix", CounterStyle.LOWER_ROMAN.format(99));
		Assert.assertEquals("0", CounterStyle.LOWER_ROMAN.format(0));
		Assert.assertEquals("-1", CounterStyle.LOWER_ROMAN.format(-1));
		Assert.assertEquals("MCMXCVIII", CounterStyle.UPPER_ROMAN.format(1998));
	}
}
