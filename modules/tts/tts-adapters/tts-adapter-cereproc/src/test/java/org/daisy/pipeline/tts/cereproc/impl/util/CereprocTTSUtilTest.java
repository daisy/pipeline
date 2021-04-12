package org.daisy.pipeline.tts.cereproc.impl.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Optional;

public class CereprocTTSUtilTest {

    @Test
    public void testApplyRegexForSwedish() throws MalformedURLException {
        Locale locale = new Locale("sv");
        CereprocTTSUtil ttsutil = new CereprocTTSUtil(Optional.of(locale));

        Assert.assertEquals("lorem ipsum Tjugosjunde kapitlet. lorem ipsum", ttsutil.applyRegex("lorem ipsum 27 kap. lorem ipsum"));
        Assert.assertEquals("test roman letter  tre", ttsutil.applyRegex("test roman letter III"));

    }

    @Test
    public void testApplyRegexForEnglish() throws MalformedURLException {
        Locale locale = new Locale("en");

        CereprocTTSUtil ttsutil = new CereprocTTSUtil(Optional.of(locale));

        Assert.assertEquals("test roman letter  three", ttsutil.applyRegex("test roman letter III"));
        Assert.assertEquals("This apartment is 25  square centimeters big", ttsutil.applyRegex("This apartment is 25 cm2 big"));
    }

    @Test
    public void testApplyCharacterSubstitutionForSwedish() throws MalformedURLException {
        Locale locale = new Locale("sv");
        CereprocTTSUtil ttsutil = new CereprocTTSUtil(Optional.of(locale));

        // Swedish rules
        Assert.assertEquals("Greek letter  beta  beta", ttsutil.applyCharacterSubstitution("Greek letter β beta"));
        // Common rules
        Assert.assertEquals(" \" -  (", ttsutil.applyCharacterSubstitution(" ” —  ₍"));
    }

    @Test
    public void testApplyCharacterSubstitutionForEnglish() throws MalformedURLException {
        Locale locale = new Locale("en");
        CereprocTTSUtil ttsutil = new CereprocTTSUtil(Optional.of(locale));

        // English rules
        Assert.assertEquals("capital gamma  capital gamma ", ttsutil.applyCharacterSubstitution("capital gamma Γ"));
        // Common rules
        Assert.assertEquals(" \" -  (", ttsutil.applyCharacterSubstitution(" ” —  ₍"));
    }
}