package org.daisy.braille.utils.impl.tools.embosser;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class DotMapperConfigurationTest {

    @Test
    public void testCheckBitmap() {
        DotMapperConfiguration.checkBitMap(DotMapper.UNICODE_BIT_MAP);
        // if we're here, the test was successful
        assertTrue(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckBitmapNonUnique() {
        DotMapperConfiguration.checkBitMap(new int[]{2, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckBitmapPowerOfTwo() {
        DotMapperConfiguration.checkBitMap(new int[]{3});
    }

}
