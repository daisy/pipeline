import java.util.Iterator;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.Combinator;
import cz.vutbr.web.css.Selector.ElementName;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Term;

import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeBlock;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;

import org.junit.Assert;
import org.junit.Test;

public class InlineStyleTest {
	
	@Test
	public void testInlineStyle() {
		InlineStyle style;
		Iterator<RuleBlock<?>> blocks;
		RuleBlock<?> block;
		Iterator<Declaration> declarations;
		Iterator<Rule<?>> declarationsAndPages;
		Declaration declaration;
		Selector selector;
		Iterator<Term<?>> terms;
		PseudoElementImpl pseudo;
		RuleRelativeBlock relativeRule;
		style = new InlineStyle(
			"text-transform: none"
		);
		blocks = style.iterator();
		Assert.assertTrue(blocks.hasNext());
		block = blocks.next();
		Assert.assertTrue(block instanceof RuleMainBlock);
		Assert.assertTrue(block == style.getMainStyle());
		declarations = ((RuleMainBlock)block).iterator();
		Assert.assertTrue(declarations.hasNext());
		declaration = declarations.next();
		Assert.assertEquals("text-transform", declaration.getProperty());
		terms = declaration.iterator();
		Assert.assertTrue(terms.hasNext());
		Assert.assertEquals("none", terms.next().toString());
		Assert.assertFalse(terms.hasNext());
		Assert.assertFalse(declarations.hasNext());
		Assert.assertFalse(blocks.hasNext());
		
		style = new InlineStyle(
			// FIXME: @page { size: 25 10; } not supported
			"text-transform: none; " +
			"&::table-by(row)::list-item { margin-left:2; } " +
			"& > span { display: block } " +
			"& span:first-child { display: inline }"
		);
		blocks = style.iterator();
		Assert.assertTrue(blocks.hasNext());
		block = blocks.next();
		Assert.assertTrue(block instanceof RuleMainBlock);
		Assert.assertTrue(block == style.getMainStyle());
		declarations = ((RuleMainBlock)block).iterator();
		Assert.assertTrue(declarations.hasNext());
		declaration = declarations.next();
		Assert.assertEquals("text-transform", declaration.getProperty());
		Assert.assertTrue(blocks.hasNext());
		block = blocks.next();
		Assert.assertTrue(block instanceof RuleRelativeBlock);
		relativeRule = (RuleRelativeBlock)block;
		Assert.assertEquals(1, relativeRule.getSelector().size());
		selector = relativeRule.getSelector().get(0);
		Assert.assertEquals(null, selector.getCombinator());
		Assert.assertEquals(1, selector.size());
		Assert.assertTrue(selector.get(0) instanceof PseudoElementImpl);
		pseudo = (PseudoElementImpl)selector.get(0);
		Assert.assertEquals("table-by", pseudo.getName());
		Assert.assertEquals(1, pseudo.getArguments().length);
		Assert.assertEquals("row", pseudo.getArguments()[0]);
		Assert.assertTrue(pseudo.getPseudoClasses().isEmpty());
		Assert.assertTrue(pseudo.hasStackedPseudoElement());
		pseudo = pseudo.getStackedPseudoElement();
		Assert.assertEquals("list-item", pseudo.getName());
		Assert.assertTrue(pseudo.getPseudoClasses().isEmpty());
		Assert.assertFalse(pseudo.hasStackedPseudoElement());
		declarationsAndPages = relativeRule.iterator();
		Assert.assertTrue(declarationsAndPages.hasNext());
		{
				Rule<?> r = declarationsAndPages.next();
				Assert.assertTrue(r instanceof Declaration);
				declaration = (Declaration)r;
		}
		Assert.assertEquals("margin-left", declaration.getProperty());
		terms = declaration.iterator();
		Assert.assertTrue(terms.hasNext());
		Assert.assertEquals("2", terms.next().toString());
		Assert.assertFalse(terms.hasNext());
		Assert.assertFalse(declarationsAndPages.hasNext());
		Assert.assertTrue(blocks.hasNext());
		block = blocks.next();
		Assert.assertTrue(block instanceof RuleRelativeBlock);
		relativeRule = (RuleRelativeBlock)block;
		Assert.assertEquals(1, relativeRule.getSelector().size());
		selector = relativeRule.getSelector().get(0);
		Assert.assertEquals(Combinator.CHILD, selector.getCombinator());
		Assert.assertEquals(1, selector.size());
		Assert.assertTrue(selector.get(0) instanceof ElementName);
		Assert.assertTrue(blocks.hasNext());
		block = blocks.next();
		Assert.assertTrue(block instanceof RuleRelativeBlock);
		relativeRule = (RuleRelativeBlock)block;
		Assert.assertEquals(1, relativeRule.getSelector().size());
		selector = relativeRule.getSelector().get(0);
		Assert.assertEquals(Combinator.DESCENDANT, selector.getCombinator());
		Assert.assertEquals(2, selector.size());
		Assert.assertTrue(selector.get(0) instanceof ElementName);
		Assert.assertTrue(selector.get(1) instanceof PseudoClass);
		Assert.assertFalse(blocks.hasNext());
		
		style = new InlineStyle(
			"span:first-child { display: inline }"
		);
		blocks = style.iterator();
		Assert.assertFalse(blocks.hasNext());
		
		style = new InlineStyle(
			"& span:first-child { display: inline }"
		);
		blocks = style.iterator();
		Assert.assertTrue(blocks.hasNext());
		block = blocks.next();
		Assert.assertTrue(block instanceof RuleRelativeBlock);
		relativeRule = (RuleRelativeBlock)block;
		Assert.assertEquals(1, relativeRule.getSelector().size());
		selector = relativeRule.getSelector().get(0);
		Assert.assertEquals(Combinator.DESCENDANT, selector.getCombinator());
		Assert.assertEquals(2, selector.size());
		Assert.assertTrue(selector.get(0) instanceof ElementName);
		Assert.assertTrue(selector.get(1) instanceof PseudoClass);
		Assert.assertFalse(blocks.hasNext());
	}
}
