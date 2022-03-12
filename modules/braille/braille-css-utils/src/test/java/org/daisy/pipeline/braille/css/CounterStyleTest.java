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
		Assert.assertEquals("", CounterStyle.counterRepresentationAlphabetic(0, symbols));
		Assert.assertEquals("a", CounterStyle.counterRepresentationAlphabetic(1, symbols));
		Assert.assertEquals("b", CounterStyle.counterRepresentationAlphabetic(2, symbols));
		Assert.assertEquals("add", CounterStyle.counterRepresentationAlphabetic(100, symbols));
		Assert.assertEquals("", CounterStyle.counterRepresentationAlphabetic(-1, symbols));
		Assert.assertEquals("h", CounterStyle.counterRepresentationCyclic(0 , symbols));
		Assert.assertEquals("a", CounterStyle.counterRepresentationCyclic(1 , symbols));
		Assert.assertEquals("b", CounterStyle.counterRepresentationCyclic(2 , symbols));
		Assert.assertEquals("d", CounterStyle.counterRepresentationCyclic(100 , symbols));
		Assert.assertEquals("g", CounterStyle.counterRepresentationCyclic(-1 , symbols));
		Assert.assertEquals("", CounterStyle.counterRepresentationFixed(0 , symbols));
		Assert.assertEquals("a", CounterStyle.counterRepresentationFixed(1 , symbols));
		Assert.assertEquals("b", CounterStyle.counterRepresentationFixed(2 , symbols));
		Assert.assertEquals("", CounterStyle.counterRepresentationFixed(100 , symbols));
		Assert.assertEquals("", CounterStyle.counterRepresentationFixed(-1 , symbols));
		Assert.assertEquals("a", CounterStyle.counterRepresentationNumeric(0 , symbols));
		Assert.assertEquals("b", CounterStyle.counterRepresentationNumeric(1 , symbols));
		Assert.assertEquals("c", CounterStyle.counterRepresentationNumeric(2 , symbols));
		Assert.assertEquals("bee", CounterStyle.counterRepresentationNumeric(100 , symbols));
		Assert.assertEquals("-b", CounterStyle.counterRepresentationNumeric(-1 , symbols));
		Assert.assertEquals("", CounterStyle.counterRepresentationSymbolic(0 , symbols));
		Assert.assertEquals("a", CounterStyle.counterRepresentationSymbolic(1 , symbols));
		Assert.assertEquals("b", CounterStyle.counterRepresentationSymbolic(2 , symbols));
		Assert.assertEquals("ddddddddddddd", CounterStyle.counterRepresentationSymbolic(100 , symbols));
		Assert.assertEquals("", CounterStyle.counterRepresentationSymbolic(-1 , symbols));
	}
}
