package org.daisy.braille.utils.impl.tools.table;

import org.daisy.dotify.api.embosser.EightDotFallbackMethod;
import org.daisy.dotify.api.table.BrailleConstants;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class EmbosserBrailleConverterTest {

    @Test(expected = IllegalArgumentException.class)
    public void testWrongTableLength() {
        new EmbosserBrailleConverter(
            "abc",
            Charset.forName("utf-8"),
            EightDotFallbackMethod.MASK,
            '\u2800',
            true
        );
    }

    @Test
    public void testCorrectTableLengths() {
        new EmbosserBrailleConverter(
            BrailleConstants.BRAILLE_PATTERNS_64,
            Charset.forName("utf-8"),
            EightDotFallbackMethod.MASK,
            '\u2800',
            true
        );
        new EmbosserBrailleConverter(
            BrailleConstants.BRAILLE_PATTERNS_256,
            Charset.forName("utf-8"),
            EightDotFallbackMethod.MASK,
            '\u2800',
            true
        );
    }

}
