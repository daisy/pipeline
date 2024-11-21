import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.csskit.antlr.CSSSource;
import cz.vutbr.web.csskit.antlr.DefaultCSSSourceReader;

import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSRuleFactory;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.VendorAtRule;

import org.junit.Assert;
import org.junit.Test;

public class VendorExtensionsTest {
	
	private static final RuleFactory rf = new BrailleCSSRuleFactory();
	
	@Test
	public void testVendorPseudoElement() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("ol.toc::-obfl-on-toc-start { content: '...' }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		Assert.assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		List<CombinedSelector> cslist = new ArrayList<CombinedSelector>();
		CombinedSelector cs = (CombinedSelector)rf.createCombinedSelector().unlock();
		Selector s = (Selector)rf.createSelector().unlock();
		s.add(rf.createElement("ol"));
		s.add(rf.createClass("toc"));
		s.add(rf.createPseudoElement("-obfl-on-toc-start"));
		cs.add(s);
		cslist.add(cs);
		Assert.assertEquals(cslist, rule.getSelectors());
	}
	
	@Test
	public void testVendorFunction() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("p::after { content: -obfl-evaluate('(...)') }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		Assert.assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		Assert.assertEquals(1, rule.size());
		Declaration decl = (Declaration)rule.get(0);
		Assert.assertEquals("content", decl.getProperty());
		Assert.assertEquals(1, decl.size());
		TermFunction fn = (TermFunction)decl.get(0);
		Assert.assertEquals("-obfl-evaluate", fn.getFunctionName());
	}
	
	@Test
	public void testVendorAtRule() throws IOException, CSSException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("@-obfl-volume-transition { @any-interrupted { content: flow(volume-end); } }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		Assert.assertEquals(1, sheet.size());
		VendorAtRule rule = (VendorAtRule)sheet.get(0);
		Assert.assertEquals("-obfl-volume-transition", rule.getName());
		Assert.assertEquals(1, rule.size());
		rule = (VendorAtRule)rule.get(0);
		Assert.assertEquals("any-interrupted", rule.getName());
		Assert.assertEquals(1, rule.size());
		Declaration decl = (Declaration)rule.get(0);
		Assert.assertEquals("content", decl.getProperty());
	}
	
	@Test
	public void testVendorProperty() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("span { -dotify-counter-style: symbols(numeric '⠴' '⠂' '⠆' '⠒' '⠲' '⠢' '⠖' '⠶' '⠦' '⠔'); }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		Assert.assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		Assert.assertEquals(1, rule.size());
		Declaration decl = (Declaration)rule.get(0);
		Assert.assertEquals("-dotify-counter-style", decl.getProperty());
		Assert.assertEquals(1, decl.size());
		TermFunction fn = (TermFunction)decl.get(0);
		Assert.assertEquals("symbols", fn.getFunctionName());
		SimpleInlineStyle style = new SimpleInlineStyle(rule);
		fn = (TermFunction)style.getValue("-dotify-counter-style");
		Assert.assertEquals("symbols", fn.getFunctionName());
		style = new SimpleInlineStyle("", style);
		Assert.assertTrue(!style.isEmpty());
	}
	
	@Test
	public void testVendorIdentValue() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("span { transform: -dotify-counter; }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		Assert.assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		Assert.assertEquals(1, rule.size());
		Declaration decl = (Declaration)rule.get(0);
		Assert.assertEquals("transform", decl.getProperty());
		Assert.assertEquals(1, decl.size());
		TermIdent ident = (TermIdent)decl.get(0);
		Assert.assertEquals("-dotify-counter", ident.toString());
	}
}
