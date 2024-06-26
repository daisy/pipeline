/**
 * 
 */
package cz.vutbr.web.csskit;

import org.w3c.dom.Element;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaExpression;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.MediaQueryList;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleFontFace;
import cz.vutbr.web.css.RuleImport;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RuleMedia;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.RuleViewport;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.Selector.ElementAttribute;
import cz.vutbr.web.css.Selector.ElementClass;
import cz.vutbr.web.css.Selector.ElementDOM;
import cz.vutbr.web.css.Selector.ElementID;
import cz.vutbr.web.css.Selector.ElementName;
import cz.vutbr.web.css.Selector.Operator;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Selector.PseudoElement;

/**
 * @author kapy
 *
 */
@SuppressWarnings("deprecation")
public class RuleFactoryImpl implements RuleFactory {

	private static RuleFactory instance;
	
	static {
		instance = new RuleFactoryImpl();
	}
	
	protected RuleFactoryImpl() {
	}
	
	public static final RuleFactory getInstance() {
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see cz.vutbr.web.css.RuleFactory#createDeclaration()
	 */
	public Declaration createDeclaration() {
		return new DeclarationImpl();
	}

	/* (non-Javadoc)
	 * @see cz.vutbr.web.css.RuleFactory#createDeclaration(cz.vutbr.web.css.Declaration)
	 */
	public Declaration createDeclaration(Declaration clone) {
		return new DeclarationImpl(clone);
	}

	/* (non-Javadoc)
	 * @see cz.vutbr.web.css.RuleFactory#createImport()
	 */
	@Deprecated
	public RuleImport createImport() {
		return new RuleImportImpl();
	}

	/* (non-Javadoc)
	 * @see cz.vutbr.web.css.RuleFactory#createMedia()
	 */
	public RuleMedia createMedia() {
		return new RuleMediaImpl();
	}

	public MediaQueryList createMediaQueryList() {
	    return new MediaQueryListImpl();
	}

	public MediaQuery createMediaQuery() {
	    return new MediaQueryImpl();
	}
	
    public MediaExpression createMediaExpression() {
        return new MediaExpressionImpl();
    }
    
	/* (non-Javadoc)
	 * @see cz.vutbr.web.css.RuleFactory#createPage()
	 */
	public RulePage createPage() {
		return new RulePageImpl();
	}
	
	/* (non-Javadoc)
	 * @see cz.vutbr.web.css.RuleFactory#createMargin()
	 */
	public RuleMargin createMargin(String area) {
		return new RuleMarginImpl(area);
	}

    public RuleViewport createViewport() {
        return new RuleViewportImpl();
    }
    
	public RuleFontFace createFontFace() {
	    return new RuleFontFaceImpl();
	}
	
	/* (non-Javadoc)
	 * @see cz.vutbr.web.css.RuleFactory#createCombinedSelector()
	 */
	public CombinedSelector createCombinedSelector() {
		return new CombinedSelectorImpl();
	}

	/* (non-Javadoc)
	 * @see cz.vutbr.web.css.RuleFactory#createSet()
	 */
	public RuleSet createSet() {
		return new RuleSetImpl();
	}

	/* (non-Javadoc)
	 * @see cz.vutbr.web.css.RuleFactory#createSelector()
	 */
	public Selector createSelector() {
		return new SelectorImpl();
	}
	
	public ElementAttribute createAttribute(String value,
			boolean isStringValue, Operator operator, String attribute) {
		return new SelectorImpl.ElementAttributeImpl(value, isStringValue, operator,
			"", attribute, null);
	}
	
	public ElementAttribute createAttribute(String value,
			boolean isStringValue, Operator operator,
			String namespaceURI, String localName, String prefix) {
		return new SelectorImpl.ElementAttributeImpl(value, isStringValue, operator,
			namespaceURI, localName, prefix);
	}
	
	public ElementClass createClass(String className) {
		return new SelectorImpl.ElementClassImpl(className);
	}

	public ElementName createElement(String elementName) {
		return new SelectorImpl.ElementNameImpl(null, elementName, null);
	}
	
	public ElementName createElement(String namespaceURI, String localName, String prefix) {
		return new SelectorImpl.ElementNameImpl(namespaceURI, localName, prefix);
	}
	
	public ElementDOM createElementDOM(Element e, boolean inlinePriority) {
		return new SelectorImpl.ElementDOMImpl(e, inlinePriority);
	}

	
	public ElementID createID(String id) {
		return new SelectorImpl.ElementIDImpl(id);
	}
	
	public PseudoClass createPseudoClass(String name) {
		return new SelectorImpl.PseudoClassImpl(name);
	}
	
	public PseudoClass createPseudoClassFunction(String name, String... args) {
		return new SelectorImpl.PseudoClassImpl(name, args);
	}
	
	public PseudoElement createPseudoElement(String name) {
		return new SelectorImpl.PseudoElementImpl(name);
	}
	
	public PseudoElement createPseudoElementFunction(String name, String... args) {
		throw new IllegalArgumentException("pseudo element " + name + " cannot be used as a function");
	}
	
	public StyleSheet createStyleSheet() {
		return new StyleSheetImpl();
	}
	
	public StyleSheet createStyleSheet(StyleSheet.Origin origin) {
		StyleSheet ret = new StyleSheetImpl();
		ret.setOrigin(origin);
		return ret;
	}
}
