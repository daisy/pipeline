package org.daisy.dotify.text.impl;

import org.daisy.dotify.api.text.IntegerOutOfRange;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class SvInteger2TextTest {
    private BasicInteger2Text int2text = new SvInt2TextLocalization();

    @Test
    public void testNumber_01() throws IntegerOutOfRange {
        assertEquals("nittionio", int2text.intToText(99));
    }

    @Test
    public void testNumber_02() throws IntegerOutOfRange {
        assertEquals("etthundratrettiotvå", int2text.intToText(132));
    }

    @Test
    public void testNumber_03() throws IntegerOutOfRange {
        assertEquals("ettusensjuhundrafemtioåtta", int2text.intToText(1758));
    }

    @Test
    public void testNumber_04() throws IntegerOutOfRange {
        assertEquals("minus tolv", int2text.intToText(-12));
    }

    @Test
    public void testNumber_05() throws IntegerOutOfRange {
        assertEquals("femton", int2text.intToText(15));
    }

    @Test
    public void testNumber_06() throws IntegerOutOfRange {
        assertEquals("tvåhundratrettiotvå", int2text.intToText(232));
    }

    @Test
    public void testNumber_07() throws IntegerOutOfRange {
        assertEquals("fem", int2text.intToText(5));
    }

    @Test
    public void testNumber_08() throws IntegerOutOfRange {
        assertEquals("tjugoåtta", int2text.intToText(28));
    }

    @Test
    public void testNumber_09() throws IntegerOutOfRange {
        assertEquals("etthundra", int2text.intToText(100));
    }

    @Test
    public void testNumber_10() throws IntegerOutOfRange {
        assertEquals("ettusen", int2text.intToText(1000));
    }

}
