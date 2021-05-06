package org.daisy.braille.utils.impl.tools.embosser;

import org.daisy.braille.utils.impl.tools.embosser.InternalContract.BrailleRange;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class BufferedEmbosserWriterTest {

    @Test
    public void testRangeSixDot() throws IOException {
        ContractEmbosserWriter ew = Mockito.mock(ContractEmbosserWriter.class);
        BufferedEmbosserWriter bew = new BufferedEmbosserWriter(ew);
        bew.open(true);
        bew.write("\u2800");
        bew.close();
        assertEquals(BrailleRange.SIX_DOT, bew.getContract().getBrailleRange());
    }

    @Test
    public void testRangeEightDot() throws IOException {
        ContractEmbosserWriter ew = Mockito.mock(ContractEmbosserWriter.class);
        BufferedEmbosserWriter bew = new BufferedEmbosserWriter(ew);
        bew.open(true);
        bew.write("\u2800\u2800\u2840\u2800\u2800");
        bew.close();
        assertEquals(BrailleRange.EIGHT_DOT, bew.getContract().getBrailleRange());
    }

    @Test
    public void testSixDotSimpleRowGap() throws IOException {
        ContractEmbosserWriter ew = Mockito.mock(ContractEmbosserWriter.class);
        BufferedEmbosserWriter bew = new BufferedEmbosserWriter(ew);
        bew.open(true);
        bew.write("\u2800");
        bew.setRowGap(0);
        bew.setRowGap(4);
        bew.close();
        assertEquals(BrailleRange.SIX_DOT, bew.getContract().getBrailleRange());
        assertTrue(bew.getContract().onlySimpleRowgaps());
    }

    @Test
    public void testSixDotComplexRowGap() throws IOException {
        ContractEmbosserWriter ew = Mockito.mock(ContractEmbosserWriter.class);
        BufferedEmbosserWriter bew = new BufferedEmbosserWriter(ew);
        bew.open(true);
        bew.write("\u2800");
        bew.setRowGap(1);
        bew.setRowGap(2);
        bew.close();
        assertEquals(BrailleRange.SIX_DOT, bew.getContract().getBrailleRange());
        assertFalse(bew.getContract().onlySimpleRowgaps());
    }

    @Test
    public void testEightDotSimpleRowGap() throws IOException {
        ContractEmbosserWriter ew = Mockito.mock(ContractEmbosserWriter.class);
        BufferedEmbosserWriter bew = new BufferedEmbosserWriter(ew);
        bew.open(true);
        bew.write("\u28F0");
        bew.setRowGap(1);
        bew.setRowGap(6);
        bew.close();
        assertEquals(BrailleRange.EIGHT_DOT, bew.getContract().getBrailleRange());
        assertTrue(bew.getContract().onlySimpleRowgaps());
    }

    @Test
    public void testEightDotComplexRowGap() throws IOException {
        ContractEmbosserWriter ew = Mockito.mock(ContractEmbosserWriter.class);
        BufferedEmbosserWriter bew = new BufferedEmbosserWriter(ew);
        bew.open(true);
        bew.write("\u28F0");
        bew.setRowGap(0);
        bew.setRowGap(2);
        bew.close();
        assertEquals(BrailleRange.EIGHT_DOT, bew.getContract().getBrailleRange());
        assertFalse(bew.getContract().onlySimpleRowgaps());
    }
}
