package org.daisy.braille.css;

import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.csskit.RuleFactoryImpl;

public class BrailleCSSRuleFactory extends RuleFactoryImpl {
	
	public BrailleCSSRuleFactory() {}
	
	@Override
	public RuleMargin createMargin(String area) {
		return new RuleMarginImpl(area);
	}
	
	@Override
	public Selector createSelector() {
		return new SelectorImpl();
	}
	
	@Override
	public PseudoClass createPseudoClass(String name) {
		return new SelectorImpl.PseudoClassImpl(name);
	}
	
	@Override
	public PseudoClass createPseudoClassFunction(String name, String... args) {
		return new SelectorImpl.PseudoClassImpl(name, args);
	}
	
	@Override
	public PseudoElement createPseudoElement(String name) {
		return new SelectorImpl.PseudoElementImpl(name);
	}
	
	@Override
	public PseudoElement createPseudoElementFunction(String name, String... args) {
		return new SelectorImpl.PseudoElementImpl(name, args);
	}
}
