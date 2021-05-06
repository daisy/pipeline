package org.daisy.braille.utils.impl.provider.indexbraille;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class IndexTransparentEmbosserWriterTest {

    @Test
    public void testTransparent() {
        assertEquals((byte) 0b1000_0000, IndexTransparentEmbosserWriter.mapUnicode2Transparent((char) 0b1000_0000));
        assertEquals(0b0000_1000, IndexTransparentEmbosserWriter.mapUnicode2Transparent((char) 0b0100_0000));
        assertEquals(0b0100_0000, IndexTransparentEmbosserWriter.mapUnicode2Transparent((char) 0b0010_0000));
        assertEquals(0b0010_0000, IndexTransparentEmbosserWriter.mapUnicode2Transparent((char) 0b0001_0000));
        assertEquals(0b0001_0000, IndexTransparentEmbosserWriter.mapUnicode2Transparent((char) 0b0000_1000));
        assertEquals(0b0000_0100, IndexTransparentEmbosserWriter.mapUnicode2Transparent((char) 0b0000_0100));
        assertEquals(0b0000_0010, IndexTransparentEmbosserWriter.mapUnicode2Transparent((char) 0b0000_0010));
        assertEquals(0b0000_0001, IndexTransparentEmbosserWriter.mapUnicode2Transparent((char) 0b0000_0001));
        assertEquals(0b0000_0000, IndexTransparentEmbosserWriter.mapUnicode2Transparent((char) 0b0000_0000));
    }

}
