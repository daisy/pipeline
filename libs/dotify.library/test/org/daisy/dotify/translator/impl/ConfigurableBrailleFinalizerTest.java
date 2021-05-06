package org.daisy.dotify.translator.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class ConfigurableBrailleFinalizerTest {

    @Test
    public void testBrailleFinalizer_01() {
        ConfigurableBrailleFinalizer c = new ConfigurableBrailleFinalizer.Builder().build();
        assertEquals("⠤⠀", c.finalizeBraille("- "));
    }

    @Test
    public void testBrailleFinalizer_02() {
        ConfigurableBrailleFinalizer c = new ConfigurableBrailleFinalizer.Builder().space("⠀⠀").hyphen("⠤⠤").build();
        assertEquals("⠤⠤⠀⠀", c.finalizeBraille("- "));
    }

}
