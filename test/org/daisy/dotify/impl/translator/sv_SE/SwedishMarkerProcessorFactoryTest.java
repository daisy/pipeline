package org.daisy.dotify.impl.translator.sv_SE;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.junit.Test;

public class SwedishMarkerProcessorFactoryTest {
	private final MarkerProcessor processor;

	public SwedishMarkerProcessorFactoryTest() throws MarkerProcessorConfigurationException {
		processor = new SwedishMarkerProcessorFactory().newMarkerProcessor("sv-se", BrailleTranslatorFactory.MODE_UNCONTRACTED);
	}

	@Test
	public void testSub() {
		String text = "H2O";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(1);
		atts.add(new DefaultTextAttribute.Builder("sub").build(1));
		atts.add(1);
		String actual = processor.processAttributes(atts.build(3), text);
		assertEquals("", "H\u28232O", actual);
	}

	@Test
	public void testSubWithRedundantTextAttributeSuccess() {
		String text = "H2O";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(1);
		atts.add(new DefaultTextAttribute.Builder("sub").add(new DefaultTextAttribute.Builder().add(1).build(1)).build(1));
		atts.add(1);
		String actual = processor.processAttributes(atts.build(3), text);
		assertEquals("", "H\u28232O", actual);
	}

	@Test
	public void testSubWithRedundantTextAttributeFail() {
		String text = "H2O";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(1);
		atts.add(new DefaultTextAttribute.Builder("sub").add(new DefaultTextAttribute.Builder().add(new DefaultTextAttribute.Builder("em").build(1)).build(1)).build(1));
		atts.add(1);
		String actual = processor.processAttributes(atts.build(3), text);
		// assert that sub is not added, since the structure is invalid.
		assertEquals("", "H⠠⠄2O", actual);
	}

	@Test
	public void testSup() {
		String text = "3rd";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(1);
		atts.add(new DefaultTextAttribute.Builder("sup").build(2));
		String actual = processor.processAttributes(atts.build(3), text);
		assertEquals("", "3\u282crd", actual);
	}

	@Test
	public void testDD() {
		String text = "3rd";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(new DefaultTextAttribute.Builder("dd").build(3));
		String actual = processor.processAttributes(atts.build(3), text);
		assertEquals("", "\u2820\u2804\u28003rd", actual);
	}

}
