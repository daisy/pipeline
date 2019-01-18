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
import cz.vutbr.web.csskit.antlr.CSSParserFactory.SourceType;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;

import org.daisy.braille.css.AnyAtRule;
import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSRuleFactory;

import org.junit.Assert;
import org.junit.Test;

public class VendorExtensionsTest {
	
	private static final RuleFactory rf = new BrailleCSSRuleFactory();
	
	//@Test
	public void testVendorPseudoElement() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			"ol.toc::-obfl-on-toc-start { content: '...' }",
			new DefaultNetworkProcessor(), null, SourceType.EMBEDDED, new URL("file:///base/url/is/not/specified"));
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
	
	//@Test
	public void testVendorFunction() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			"p::after { content: -obfl-evaluate('(...)') }",
			new DefaultNetworkProcessor(), null, SourceType.EMBEDDED, new URL("file:///base/url/is/not/specified"));
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
			"@-obfl-volume-transition { @any-interrupted { content: flow(volume-end); } }",
			new DefaultNetworkProcessor(), null, SourceType.EMBEDDED, new URL("file:///base/url/is/not/specified"));
		Assert.assertEquals(1, sheet.size());
		AnyAtRule rule = (AnyAtRule)sheet.get(0);
		Assert.assertEquals("-obfl-volume-transition", rule.getName());
		Assert.assertEquals(1, rule.size());
		rule = (AnyAtRule)rule.get(0);
		Assert.assertEquals("any-interrupted", rule.getName());
		Assert.assertEquals(1, rule.size());
		Declaration decl = (Declaration)rule.get(0);
		Assert.assertEquals("content", decl.getProperty());
	}
}
