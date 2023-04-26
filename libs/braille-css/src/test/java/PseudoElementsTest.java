import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.csskit.antlr.CSSSource;
import cz.vutbr.web.csskit.antlr.DefaultCSSSourceReader;

import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSRuleFactory;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.SupportedBrailleCSS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PseudoElementsTest {
	
	private static final RuleFactory rf = new BrailleCSSRuleFactory();
	
	public PseudoElementsTest() {
		SupportedBrailleCSS css = new SupportedBrailleCSS();
		CSSFactory.registerSupportedCSS(css);
		CSSFactory.registerDeclarationTransformer(css);
	}
	
	@Test
	public void testPseudoElement() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("body p::after { content: 'foo' }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
	}
	
	@Test
	public void testStackedPseudoElements() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("p::after::before { content: 'foo' }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		List<CombinedSelector> cslist = new ArrayList<CombinedSelector>();
		CombinedSelector cs = (CombinedSelector)rf.createCombinedSelector().unlock();
		Selector s = (Selector)rf.createSelector().unlock();
		s.add(rf.createElement("p"));
		s.add(rf.createPseudoElement("after"));
		s.add(rf.createPseudoElement("before"));
		cs.add(s);
		cslist.add(cs);
		assertEquals(cslist, rule.getSelectors());
		PseudoElement p = s.getPseudoElement();
		assertEquals("::after::before", p.toString());
		assertTrue(p instanceof PseudoElementImpl);
		PseudoElementImpl i = (PseudoElementImpl)p;
		assertTrue(i.hasStackedPseudoElement());
		i = i.getStackedPseudoElement();
		assertEquals("::before", i.toString());
	}
	
	@Test
	public void testPseudoElementWithPseudoClass() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource("table::table-by(row)::list-item:last-child::after { display: none }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		List<CombinedSelector> cslist = new ArrayList<CombinedSelector>();
		CombinedSelector cs = (CombinedSelector)rf.createCombinedSelector().unlock();
		Selector s = (Selector)rf.createSelector().unlock();
		s.add(rf.createElement("table"));
		s.add(rf.createPseudoElementFunction("table-by", "row"));
		s.add(rf.createPseudoElement("list-item"));
		s.add(rf.createPseudoClass("last-child"));
		s.add(rf.createPseudoElement("after"));
		cs.add(s);
		cslist.add(cs);
		assertEquals(cslist, rule.getSelectors());
		PseudoElement p = s.getPseudoElement();
		assertEquals("::table-by(row)::list-item:last-child::after", p.toString());
	}
}
