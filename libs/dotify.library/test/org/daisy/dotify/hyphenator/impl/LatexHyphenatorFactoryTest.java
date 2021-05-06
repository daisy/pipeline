package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class LatexHyphenatorFactoryTest {

    @Test
    public void testEnglishHyphenator() throws HyphenatorConfigurationException {
        HyphenatorInterface h = new LatexHyphenatorFactory(LatexHyphenatorCore.getInstance()).newHyphenator("en");

        //Test
        assertEquals("test­ing", h.hyphenate("testing"));
    }

}
