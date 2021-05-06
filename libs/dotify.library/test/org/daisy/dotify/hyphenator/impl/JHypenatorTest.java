package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class JHypenatorTest {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Test
    public void testBeginEndHandling() throws HyphenatorConfigurationException {
        JHyphenator jHyphenator = new JHyphenator("sv");

        assertEquals("i", jHyphenator.handleWord("i", new byte[]{}));
        assertEquals("in", jHyphenator.handleWord("in", new byte[]{1}));
        assertEquals("test", jHyphenator.handleWord("test", new byte[]{1, 0, 0}));
        assertEquals("test", jHyphenator.handleWord("test", new byte[]{0, 0, 1}));
        assertEquals("test\u00ADar",
                jHyphenator.handleWord("testar", new byte[]{1, 0, 0, 1, 0, 1})
        );
    }

    @Test
    public void testHyphenate() throws HyphenatorConfigurationException {
        JHyphenator jHyphenator = new JHyphenator("sv");

        assertEquals("in", jHyphenator.hyphenate("in"));
        assertEquals("test", jHyphenator.hyphenate("test"));
        assertEquals("tes\u00ADtar", jHyphenator.hyphenate("testar"));
        assertEquals(
                "tes\u00ADtar i do\u00ADti\u00ADfy",
                jHyphenator.hyphenate("testar i dotify")
        );
    }
}
