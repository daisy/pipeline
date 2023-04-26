import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.CombinedSelector.Specificity;
import cz.vutbr.web.css.CombinedSelector.Specificity.Level;
import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.Combinator;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.csskit.antlr.CSSSource;
import cz.vutbr.web.csskit.antlr.DefaultCSSSourceReader;
import cz.vutbr.web.csskit.CombinedSelectorImpl.SpecificityImpl;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;

import org.apache.xerces.parsers.DOMParser;

import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSRuleFactory;
import org.daisy.braille.css.SelectorImpl.NegationPseudoClassImpl;
import org.daisy.braille.css.SelectorImpl.RelationalPseudoClassImpl;
import org.daisy.braille.css.SupportedBrailleCSS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public class PseudoClassTest {
	
	private static final RuleFactory rf = new BrailleCSSRuleFactory();
	
	public PseudoClassTest() {
		SupportedBrailleCSS css = new SupportedBrailleCSS();
		CSSFactory.registerSupportedCSS(css);
		CSSFactory.registerDeclarationTransformer(css);
	}
	
	@Test
	public void testNegationPseudoClass() throws CSSException, IOException, SAXException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource(":not(.foo,.bar) { display: none }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		List<CombinedSelector> cslist = new ArrayList<CombinedSelector>();
		CombinedSelector cs = (CombinedSelector)rf.createCombinedSelector().unlock();
		List<Selector> negated = new ArrayList<Selector>();
		Selector s = (Selector)rf.createSelector().unlock();
		s.add(rf.createClass("foo"));
		negated.add(s);
		s = (Selector)rf.createSelector().unlock();
		s.add(rf.createClass("bar"));
		negated.add(s);
		s = (Selector)rf.createSelector().unlock();
		s.add(new NegationPseudoClassImpl(negated));
		cs.add(s);
		cslist.add(cs);
		assertEquals(cslist, rule.getSelectors());
		Specificity spec = new SpecificityImpl();
		spec.add(Level.C);
		assertEquals(cs.computeSpecificity(), spec);
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new ByteArrayInputStream(
			"<html><div class='foo'/><div/><div class='bar'/><div class='baz'></div></html>"
			.getBytes(StandardCharsets.UTF_8))));
		Document doc = parser.getDocument();
		NodeList divs = doc.getElementsByTagName("div");
		assertFalse(s.matches((Element)divs.item(0)));
		assertTrue(s.matches((Element)divs.item(1)));
		assertFalse(s.matches((Element)divs.item(2)));
		assertTrue(s.matches((Element)divs.item(3)));
	}
	
	@Test
	public void testRelationalPseudoClass() throws CSSException, IOException, SAXException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource(":has(.foo,.bar) { display: none }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		List<CombinedSelector> cslist = new ArrayList<CombinedSelector>();
		List<CombinedSelector> relative = new ArrayList<CombinedSelector>();
		CombinedSelector cs = (CombinedSelector)rf.createCombinedSelector().unlock();
		Selector s = (Selector)rf.createSelector().unlock();
		s.add(rf.createClass("foo"));
		s.setCombinator(Combinator.DESCENDANT);
		cs.add(s);
		relative.add(cs);
		s = (Selector)rf.createSelector().unlock();
		s.add(rf.createClass("bar"));
		s.setCombinator(Combinator.DESCENDANT);
		cs = (CombinedSelector)rf.createCombinedSelector().unlock();
		cs.add(s);
		relative.add(cs);
		s = (Selector)rf.createSelector().unlock();
		s.add(new RelationalPseudoClassImpl(relative));
		cs = (CombinedSelector)rf.createCombinedSelector().unlock();
		cs.add(s);
		cslist.add(cs);
		assertEquals(cslist, rule.getSelectors());
		Specificity spec = new SpecificityImpl();
		spec.add(Level.C);
		assertEquals(cs.computeSpecificity(), spec);
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new ByteArrayInputStream(
			"<html><div><span class='bar'/></div><div/><div/></html>"
			.getBytes(StandardCharsets.UTF_8))));
		Document doc = parser.getDocument();
		NodeList divs = doc.getElementsByTagName("div");
		assertTrue(s.matches((Element)divs.item(0)));
		assertFalse(s.matches((Element)divs.item(1)));
		assertFalse(s.matches((Element)divs.item(2)));
	}
	
	@Test
	public void testNegationCombinedWithRelationalPseudoClass() throws CSSException, IOException, SAXException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			new CSSSource(":not(:has(:not(.foo, .bar))) { display: none }",
			              (String)null,
			              new URL("file:///base/url/is/not/specified"),
			              0, 0),
			new DefaultCSSSourceReader());
		assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		List<CombinedSelector> cslist = rule.getSelectors();
		assertEquals(1, cslist.size());
		CombinedSelector cs = cslist.get(0);
		assertEquals(1, cslist.size());
		Specificity spec = new SpecificityImpl();
		spec.add(Level.C);
		assertEquals(cs.computeSpecificity(), spec);
		Selector s = cs.get(0);
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new ByteArrayInputStream(
			"<html><div><span class='bar'/></div><div><span/></div><div/></html>"
			.getBytes(StandardCharsets.UTF_8))));
		Document doc = parser.getDocument();
		NodeList divs = doc.getElementsByTagName("div");
		assertTrue(s.matches((Element)divs.item(0)));
		assertFalse(s.matches((Element)divs.item(1)));
		assertTrue(s.matches((Element)divs.item(2)));
	}
}
