package org.daisy.pipeline.css.speech;

import org.junit.Assert;
import org.junit.Test;

public class VoiceFamilyListTest {

	@Test
	public void testFamilyName() {
		Assert.assertEquals("'espeak'",  VoiceFamilyList.of("espeak").toString());
		Assert.assertEquals("'alain16'", VoiceFamilyList.of("alain16").toString());
	}

	@Test
	public void testNonStandardCSS() {
		Assert.assertEquals("child male 'att'", VoiceFamilyList.of("child male att").toString());
	}

	@Test
	public void testBackwardCompatibility() {
		Assert.assertEquals("child male, child female",             VoiceFamilyList.of("child").toString());
		Assert.assertEquals("young male, young female",             VoiceFamilyList.of("17").toString());
		Assert.assertEquals("child male 'att', child female 'att'", VoiceFamilyList.of("att,16").toString());
		Assert.assertEquals("child male 'att'",                     VoiceFamilyList.of("16,male,att").toString());
		Assert.assertEquals("child female",                         VoiceFamilyList.of("female,15").toString());
	}
}
