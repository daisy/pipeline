package org.daisy.dotify.translator.impl.sv;

import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TranslatorType;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.impl.sv.SwedishMarkerProcessorFactory.SwedishMarkerProcessorConfigurationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class SwedishMarkerProcessorFactoryTest {
    private final DefaultMarkerProcessor processor;

    public SwedishMarkerProcessorFactoryTest() throws SwedishMarkerProcessorConfigurationException {
        processor = new SwedishMarkerProcessorFactory().newMarkerProcessor(
            "sv-se",
            TranslatorType.UNCONTRACTED.toString()
        );
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
        atts.add(new DefaultTextAttribute.Builder("sub").add(
            new DefaultTextAttribute.Builder().add(1).build(1)
        ).build(1));
        atts.add(1);
        String actual = processor.processAttributes(atts.build(3), text);
        assertEquals("", "H\u28232O", actual);
    }

    @Test
    public void testSubWithRedundantTextAttributeFail() {
        String text = "H2O";
        DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder();
        atts.add(1);
        atts.add(new DefaultTextAttribute.Builder("sub").add(
            new DefaultTextAttribute.Builder().add(
                    new DefaultTextAttribute.Builder("em").build(1)
            ).build(1)
        ).build(1));
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

    @Test
    public void testNested() {
        String text = "page 1";
        DefaultTextAttribute.Builder atts = new DefaultTextAttribute.Builder("strong");
        atts.add(5);
        atts.add(new DefaultTextAttribute.Builder("em").build(1));
        String actual = processor.processAttributes(
            new DefaultTextAttribute.Builder().add(atts.build(6)).build(6),
            text
        );
        assertEquals("", "⠨⠨page ⠠⠄1⠱", actual);
    }

}
