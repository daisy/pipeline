package cz.vutbr.web.css;

import org.w3c.dom.Element;

/**
 * Acts as collection of parsed parts of Selector (Parts)
 * with extended functionality.
 * 
 * Items are defined within this interface.
 * 
 * 
 * @author kapy
 * @author Jan Svercl, VUT Brno, 2008
 */
public interface Selector extends Rule<Selector.SelectorPart> {

	/**
	 * Combinator for simple selectors 
	 * @author kapy
	 *
	 */
    public enum Combinator {
    	DESCENDANT(" "),
        ADJACENT("+"),
    	PRECEDING("~"),
    	CHILD(">");
    
    	private String value;
    	
    	private Combinator(String value) {
    		this.value = value;
    	}
    	
    	public String value() {return value;}
    }
    
    /**
     * Operator for SelectorPart attributes 
     * @author kapy
     *
     */
    public enum Operator {
    	EQUALS("="),
    	INCLUDES("~="),
    	DASHMATCH("|="),
    	CONTAINS("*="),
    	STARTSWITH("^="),
    	ENDSWITH("$="),
    	NO_OPERATOR("");
    	
    	private String value;
    	
    	private Operator(String value) {
    		this.value = value;
    	}
    	
    	public String value() {return value;}
    }
    
    /**
     * Returns combinator of this and other simple selector
     * @return Combinator
     */
    public Combinator getCombinator();
    
    /**
     * Sets combinator 
     * @param combinator Combinator between this and other selector
     * @return Modified instance
     */
    public Selector setCombinator(Combinator combinator);  
    
    /**
     * Name of CSS class which is affected by this selector  
     * @return Name of CSS class
     */
    public String getClassName();
    
    /**
     * ID of CSS item which is affected by this selector
     * @return ID of CSS item
     */
    public String getIDName();
    
    /**
     * Name of HTML element which is affected by this selector
     * @return Name of HTML element
     */
    public ElementName getElementName();
    
    /**
     * Reads the pseudoelement of the selector 
     * @return the used pseudo-element or <code>null</code> if no pseudo-element is specified
     */
    public PseudoElement getPseudoElement();
    
    /**
     * Modifies specificity according to CSS standard
     * @param spec Specificity to be modified
     */
    public void computeSpecificity(CombinedSelector.Specificity spec);
    
    /**
     * Matches simple selector against DOM element
     * @param e Element
     * @return <code>true</true> in case of match
     */
    public boolean matches(Element e);
    
    /**
     * Matches simple selector against DOM element with an additional condition
     * @param e Element
     * @param cond An additional condition to be applied
     * @return <code>true</true> in case of match
     */
    public boolean matches(Element e, MatchCondition cond);
    
    /**
     * Interface for handling items
     * @author kapy
     *
     */
    public interface SelectorPart { 	
    	public boolean matches(Element e, MatchCondition cond);
    	public void computeSpecificity(CombinedSelector.Specificity spec);
    }
    
    /**
     * Element name
     * @author kapy, bfrees
     *
     */
    public interface ElementName extends SelectorPart {
    	public static final String WILDCARD = "*";
    	/**
    	 * @return localName. WILDCARD means any name.
    	 */
    	public String getLocalName();
    	/**
    	 * @return namespaceURI. null means any namespace, "" means no namespace.
    	 */
    	public String getNamespaceURI();
    	/**
    	 * @return prefix. null means undefined prefix. "" means no namespace. WILDCARD means any namespace.
    	 */
    	public String getPrefix();
    	/**
    	 * Sets namespaceURI to namespaceURI, localName to localName, and prefix to prefix.
    	 * @param namespaceURI
    	 * @param localName
    	 * @param prefix
    	 * @throws IllegalArgumentException if localName is null or "",
    	 *                                  if namespaceURI is null and prefix is not WILDCARD or null,
    	 *                                  if prefix is "" and namespaceURI is not "", or
    	 *                                  if prefix is WILDCARD and namespaceURI is not null.
    	 */
    	public ElementName setName(String namespaceURI, String localName, String prefix);
    	/**
    	 * Make immutable.
    	 */
    	public ElementName lock();
    }
    
    /**
     * Element attribute
     * @author kapy
     *
     */
    public interface ElementAttribute extends SelectorPart {
    	public static final String WILDCARD = "*";
    	/**
    	 * @return localName.
    	 */
    	public String getLocalName();
    	/**
    	 * @return namespaceURI. null means any namespace, "" means no namespace.
    	 */
    	public String getNamespaceURI();
    	/**
    	 * @return prefix. null means undefined prefix. "" means no namespace. WILDCARD means any namespace.
    	 */
    	public String getPrefix();
    	/**
    	 * Sets namespaceURI to namespaceURI, localName to localName, and prefix to prefix.
    	 * @param namespaceURI
    	 * @param localName
    	 * @param prefix
    	 * @throws IllegalArgumentException if localName is null or "",
    	 *                                  if namespaceURI is null and prefix is not WILDCARD,
    	 *                                  if prefix is WILDCARD and namespaceURI is not null,
    	 *                                  if prefix is "" or null and namespaceURI is not "".
    	 */
    	public ElementAttribute setAttribute(String namespaceURI, String localName, String prefix);
    	
    	public String getValue();
    	public ElementAttribute setValue(String value);
    	
    	public Operator getOperator();
    	public void setOperator(Operator operator);
    }
    
    /**
     * Element class
     * @author kapy
     *
     */
    public interface ElementClass extends SelectorPart {
    	public String getClassName();
    	public ElementClass setClassName(String name);
    }
    
    /**
     * Element id
     * @author kapy
     *
     */
    public interface ElementID extends SelectorPart {
    	public String getID();
    	public ElementID setID(String id);
    }
    
    public interface ElementDOM extends SelectorPart {
    	public Element getElement();
    	public ElementDOM setElement(Element e);
    }
    
    public interface PseudoPage extends SelectorPart {}
    
    /**
     * Pseudo class
     * @author bertfrees
     *
     */
    public interface PseudoClass extends PseudoPage {}
    
    /**
     * Pseudo element
     * @author bertfrees
     *
     */
    public interface PseudoElement extends PseudoPage {
        
        public static final String FIRST_LINE = "first-line";
        public static final String FIRST_LETTER = "first-letter";
        public static final String BEFORE = "before";
        public static final String AFTER = "after";
        
        public String getName();
        public String[] getArguments();
        
    }
}
