package org.daisy.braille.utils.impl.provider;

import org.daisy.dotify.api.table.BrailleConstants;
import org.daisy.dotify.api.table.BrailleConverter;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class EmbosserTableProviderTest {

    //TODO: more tests

    @Test
    public void testTableUnicode() {
        EmbosserTableProvider bt = new EmbosserTableProvider();
        String input = BrailleConstants.BRAILLE_PATTERNS_256;
        EmbosserTableProvider.TableType t = EmbosserTableProvider.TableType.UNICODE_BRAILLE;
        BrailleConverter ta = bt.newTable(t);
        String text = ta.toText(input);
        String braille = ta.toBraille(text);
        assertEquals(
            "Assert that conversion is reversible",
            input,
            braille
        );
        assertEquals(
            "Assert that text has been transformed",
            BrailleConstants.BRAILLE_PATTERNS_256,
            text
        );
        assertTrue(
            "Assert that table supports 8-dot",
            ta.supportsEightDot()
        );
        assertEquals(
            "Assert that UTF-8 is the preferred charset",
            Charset.forName("UTF-8"),
            ta.getPreferredCharset()
        );
    }

    @Test
    public void testNABCC() {
        EmbosserTableProvider bt = new EmbosserTableProvider();
        String input = BrailleConstants.BRAILLE_PATTERNS_64;
        BrailleConverter nabcc = bt.newTable(EmbosserTableProvider.TableType.NABCC);
        BrailleConverter nabcc8dot = bt.newTable(EmbosserTableProvider.TableType.NABCC_8DOT);
        assertEquals(
            "Assert that first 64 characters of NABCC and NABCC_8DOT are equal",
            nabcc.toText(input),
            nabcc8dot.toText(input)
        );
    }
}
