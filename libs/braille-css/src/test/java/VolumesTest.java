import java.io.IOException;

import java.net.URL;

import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.csskit.antlr.CSSSource;
import cz.vutbr.web.csskit.antlr.DefaultCSSSourceReader;

import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.RuleVolume;
import org.daisy.braille.css.RuleVolumeArea;
import org.daisy.braille.css.RuleVolumeArea.VolumeArea;
import org.daisy.braille.css.SupportedBrailleCSS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class VolumesTest {
	
	public VolumesTest() {
		SupportedBrailleCSS css = new SupportedBrailleCSS();
		CSSFactory.registerSupportedCSS(css);
		CSSFactory.registerDeclarationTransformer(css);
	}
	
	@Test
	public void testSimpleVolumeRule() throws IOException, CSSException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("@volume { max-length: 100; }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
		Rule<?> rule = sheet.get(0);
		assertTrue(rule instanceof RuleVolume);
		RuleVolume volumeRule = (RuleVolume)rule;
		assertEquals(1, volumeRule.size());
		rule = volumeRule.get(0);
		assertTrue(rule instanceof Declaration);
		Declaration decl = (Declaration)rule;
		assertEquals("max-length", decl.getProperty());
	}
	
	@Test
	public void testFirstVolumeRule() throws IOException, CSSException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("@volume:first { max-length: 100; }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
		Rule<?> rule = sheet.get(0);
		assertTrue(rule instanceof RuleVolume);
		RuleVolume volumeRule = (RuleVolume)rule;
		assertEquals("first", volumeRule.getPseudo());
		assertEquals(1, volumeRule.size());
		rule = volumeRule.get(0);
		assertTrue(rule instanceof Declaration);
		Declaration decl = (Declaration)rule;
		assertEquals("max-length", decl.getProperty());
	}
	
	@Test
	public void testNthVolumeRule() throws IOException, CSSException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("@volume:nth(2) { max-length: 100; }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
		Rule<?> rule = sheet.get(0);
		assertTrue(rule instanceof RuleVolume);
		RuleVolume volumeRule = (RuleVolume)rule;
		assertEquals("nth(2)", volumeRule.getPseudo());
		assertEquals(1, volumeRule.size());
		rule = volumeRule.get(0);
		assertTrue(rule instanceof Declaration);
		Declaration decl = (Declaration)rule;
		assertEquals("max-length", decl.getProperty());
	}
	
	@Test
	public void testVolumeRuleWithBeginArea() throws IOException, CSSException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("@volume { max-length: 100; @begin { content: 'foo' } }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
		Rule<?> rule = sheet.get(0);
		assertTrue(rule instanceof RuleVolume);
		RuleVolume volumeRule = (RuleVolume)rule;
		assertEquals(2, volumeRule.size());
		rule = volumeRule.get(0);
		assertTrue(rule instanceof Declaration);
		Declaration decl = (Declaration)rule;
		assertEquals("max-length", decl.getProperty());
		rule = volumeRule.get(1);
		assertTrue(rule instanceof RuleVolumeArea);
		RuleVolumeArea area = (RuleVolumeArea)rule;
		assertEquals(VolumeArea.BEGIN, area.getVolumeArea());
	}
}
