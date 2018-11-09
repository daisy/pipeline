package org.daisy.dotify.translator.impl;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.translator.impl.DefaultBypassMarkerProcessorFactory;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class DefaultBypassMarkerProcessorFactoryTest {
	private final MarkerProcessor tp;

	public DefaultBypassMarkerProcessorFactoryTest() throws MarkerProcessorConfigurationException {
		tp = new DefaultBypassMarkerProcessorFactory().newMarkerProcessor("sv-se", BrailleTranslatorFactory.MODE_BYPASS);
	}

	@Test
	public void testDD_Text() {
		String text = "3rd";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(new DefaultTextAttribute.Builder("dd").build(3));
		String actual = tp.processAttributes(atts.build(3), text);
		assertEquals("", "* 3rd", actual);
	}

	@Test
	public void testEm_Text() {
		String text = "3rd";
		DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
		atts.add(new DefaultTextAttribute.Builder("em").build(3));
		String actual = tp.processAttributes(atts.build(3), text);
		assertEquals("", "3rd", actual);
	}

}
