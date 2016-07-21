package org.daisy.pipeline.braille.dotify.impl;

import java.util.ArrayList;

import static org.daisy.pipeline.braille.dotify.impl.BrailleFilterFactoryImpl.counterRepresentationAlphabetic;
import static org.daisy.pipeline.braille.dotify.impl.BrailleFilterFactoryImpl.counterRepresentationCyclic;
import static org.daisy.pipeline.braille.dotify.impl.BrailleFilterFactoryImpl.counterRepresentationFixed;
import static org.daisy.pipeline.braille.dotify.impl.BrailleFilterFactoryImpl.counterRepresentationNumeric;
import static org.daisy.pipeline.braille.dotify.impl.BrailleFilterFactoryImpl.counterRepresentationSymbolic;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BrailleFilterFactoryImplTest {
	
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
		assertEquals("", counterRepresentationAlphabetic(0, symbols));
		assertEquals("a", counterRepresentationAlphabetic(1, symbols));
		assertEquals("b", counterRepresentationAlphabetic(2, symbols));
		assertEquals("add", counterRepresentationAlphabetic(100, symbols));
		assertEquals("", counterRepresentationAlphabetic(-1, symbols));
		assertEquals("h", counterRepresentationCyclic(0 , symbols));
		assertEquals("a", counterRepresentationCyclic(1 , symbols));
		assertEquals("b", counterRepresentationCyclic(2 , symbols));
		assertEquals("d", counterRepresentationCyclic(100 , symbols));
		assertEquals("g", counterRepresentationCyclic(-1 , symbols));
		assertEquals("", counterRepresentationFixed(0 , symbols));
		assertEquals("a", counterRepresentationFixed(1 , symbols));
		assertEquals("b", counterRepresentationFixed(2 , symbols));
		assertEquals("", counterRepresentationFixed(100 , symbols));
		assertEquals("", counterRepresentationFixed(-1 , symbols));
		assertEquals("a", counterRepresentationNumeric(0 , symbols));
		assertEquals("b", counterRepresentationNumeric(1 , symbols));
		assertEquals("c", counterRepresentationNumeric(2 , symbols));
		assertEquals("bee", counterRepresentationNumeric(100 , symbols));
		assertEquals("-b", counterRepresentationNumeric(-1 , symbols));
		assertEquals("", counterRepresentationSymbolic(0 , symbols));
		assertEquals("a", counterRepresentationSymbolic(1 , symbols));
		assertEquals("b", counterRepresentationSymbolic(2 , symbols));
		assertEquals("ddddddddddddd", counterRepresentationSymbolic(100 , symbols));
		assertEquals("", counterRepresentationSymbolic(-1 , symbols));
	}
}
