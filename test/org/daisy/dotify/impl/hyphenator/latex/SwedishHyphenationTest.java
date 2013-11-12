package org.daisy.dotify.impl.hyphenator.latex;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.junit.Ignore;
import org.junit.Test;

public class SwedishHyphenationTest {
	private final HyphenatorInterface hyph_sv_SE;
	public SwedishHyphenationTest() throws HyphenatorConfigurationException {
		HyphenatorInterface h2;
		try {
			String locale = "sv-SE";
			HyphenatorFactory hf = new LatexHyphenatorFactory(LatexHyphenatorCore.getInstance());
			//if this is set to 3 (using only patterns) several tests will fail 
			//hf.setFeature(HyphenationFeatures.HYPHENATION_ACCURACY, 3);
			h2 = hf.newHyphenator(locale);
		} catch (HyphenatorConfigurationException e) {
			h2 = null;
		}
		hyph_sv_SE = h2;
	}

	@Test
	public void testHyphenation_Sv_ZeroWidthSpace() throws HyphenatorConfigurationException {
		assertEquals("CD-\u200bver­sio­nen", hyph_sv_SE.hyphenate("CD-versionen"));
	}
	@Test
	public void testHyphenation_Sv_001() throws HyphenatorConfigurationException {
		assertEquals("re­tå", hyph_sv_SE.hyphenate("retå"));
	}
	@Test
	public void testHyphenation_Sv_002() throws HyphenatorConfigurationException {
		assertEquals("att", hyph_sv_SE.hyphenate("att"));
	}
	@Test
	public void testCompoundWord_Sv_001() throws HyphenatorConfigurationException {
		assertEquals("val­nötska­ka", hyph_sv_SE.hyphenate("valnötskaka"));
	}
	@Test
	public void testWord_Sv_002() throws HyphenatorConfigurationException {
		assertEquals("vart­åt", hyph_sv_SE.hyphenate("vartåt"));
	}
	@Test
	public void testCompoundWord_Sv_003() throws HyphenatorConfigurationException {
		assertEquals("fisklå­da", hyph_sv_SE.hyphenate("fisklåda"));
	}
	@Test
	public void testCompoundWord_Sv_004() throws HyphenatorConfigurationException {
		assertEquals("blå­rött", hyph_sv_SE.hyphenate("blårött"));
	}
	@Test
	public void testCompoundWord_Sv_005() throws HyphenatorConfigurationException {
		assertEquals("ro­sen­rött", hyph_sv_SE.hyphenate("rosenrött"));
	}
	@Test
	public void testCompoundWord_Sv_006() throws HyphenatorConfigurationException {
		assertEquals("him­mels­blått", hyph_sv_SE.hyphenate("himmelsblått"));
	}
	@Test
	public void testWord_Sv_007() throws HyphenatorConfigurationException {
		assertEquals("him­mels", hyph_sv_SE.hyphenate("himmels"));
	}
	@Test
	public void testWord_Sv_008() throws HyphenatorConfigurationException {
		assertEquals("blått", hyph_sv_SE.hyphenate("blått"));
	}
	@Test
	public void testWord_Sv_009() throws HyphenatorConfigurationException {
		assertEquals("sjut­ton­åring­ar", hyph_sv_SE.hyphenate("sjuttonåringar"));
	}
	@Test
	public void testWord_Sv_010() throws HyphenatorConfigurationException {
		assertEquals("ar­ton­åring­ar", hyph_sv_SE.hyphenate("artonåringar"));
	}
	@Test
	public void testWord_Sv_011() throws HyphenatorConfigurationException {
		assertEquals("tret­ton­åring", hyph_sv_SE.hyphenate("trettonåring"));
	}
	@Test
	public void testWord_Sv_012() throws HyphenatorConfigurationException {
		assertEquals("schysst", hyph_sv_SE.hyphenate("schysst"));
	}
	@Test
	public void testWord_Sv_013() throws HyphenatorConfigurationException {
		assertEquals("sel­le­ri", hyph_sv_SE.hyphenate("selleri"));
	}
	@Test
	public void testCompoundWord_Sv_014() throws HyphenatorConfigurationException {
		assertEquals("el­stöts­pen­na", hyph_sv_SE.hyphenate("elstötspenna"));
	}
	@Test
	public void testCompoundWord_Sv_015() throws HyphenatorConfigurationException {
		assertEquals("test­fil", hyph_sv_SE.hyphenate("testfil"));
	}
	@Test
	public void testWord_Sv_016() throws HyphenatorConfigurationException {
		assertEquals("här­om­da­gen", hyph_sv_SE.hyphenate("häromdagen"));
	}
	@Test
	public void testCompoundWord_Sv_017() throws HyphenatorConfigurationException {
		assertEquals("skri­var­driv­ru­ti­nen", hyph_sv_SE.hyphenate("skrivardrivrutinen"));
	}
	@Test
	public void testCompoundWord_Sv_018() throws HyphenatorConfigurationException {
		assertEquals("ut­skrifts­fil", hyph_sv_SE.hyphenate("utskriftsfil"));
	}
	@Test
	public void testCompoundWord_Sv_019() throws HyphenatorConfigurationException {
		assertEquals("tal­boks­lä­sa­re", hyph_sv_SE.hyphenate("talboksläsare"));
	}	
	@Test
	public void testWord_Sv_020() throws HyphenatorConfigurationException {
		assertEquals("hämt­mat", hyph_sv_SE.hyphenate("hämtmat"));
	}
	@Test
	public void testCompoundWord_Sv_021() throws HyphenatorConfigurationException {
		assertEquals("över­armsöver­an­sträng­ning", hyph_sv_SE.hyphenate("överarmsöveransträngning"));
	}
	@Test
	@Ignore
	public void testCompoundWord_Sv_023() throws HyphenatorConfigurationException {
		assertEquals("möns­ter­djups­mä­tar­ap­pa­rat", hyph_sv_SE.hyphenate("mönsterdjupsmätarapparat"));
	}
	@Test
	public void testWord_Sv_024() throws HyphenatorConfigurationException {
		assertEquals("hög­sko­le­stu­den­ter", hyph_sv_SE.hyphenate("högskolestudenter"));
	}
	@Test
	public void testWord_Sv_025() throws HyphenatorConfigurationException {
		assertEquals("ton­års­flicka", hyph_sv_SE.hyphenate("tonårsflicka"));
	}
	@Test
	public void testWord_Sv_026() throws HyphenatorConfigurationException {
		assertEquals("son­son", hyph_sv_SE.hyphenate("sonson"));
	}
}