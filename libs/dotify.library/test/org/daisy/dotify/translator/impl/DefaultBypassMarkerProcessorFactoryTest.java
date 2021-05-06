package org.daisy.dotify.translator.impl;

import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TranslatorType;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.impl.DefaultBypassMarkerProcessorFactory.DefaultBypassMarkerProcessorConfigurationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class DefaultBypassMarkerProcessorFactoryTest {
    private final DefaultMarkerProcessor tp;

    public DefaultBypassMarkerProcessorFactoryTest() throws DefaultBypassMarkerProcessorConfigurationException {
        tp = new DefaultBypassMarkerProcessorFactory().newMarkerProcessor("sv-se", TranslatorType.BYPASS.toString());
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
