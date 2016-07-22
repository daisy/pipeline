/**
 * MatchConditionOnElements.java
 *
 * Created on 1.7.2013, 11:26:35 by burgetr
 */
package cz.vutbr.web.csskit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import cz.vutbr.web.css.MatchCondition;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Selector.SelectorPart;

/**
 * A match condition for matching the pseudo classes to particular elements. It allows to assign
 * pseudoclasses to the individual elements in the DOM tree and to the element names. Multiple pseudo classes
 * may be assigned to a single element or element name. When testing the condition, the exact element is
 * tested first. If no pseudo class is defined for that element, the element name is tested.
 * 
 * @author burgetr
 */
public class MatchConditionOnElements implements MatchCondition
{
    private Map<Element, Set<PseudoClass>> elements;
    private Map<String, Set<PseudoClass>> names;
    
    /**
     * Creates the condition with an empty set of assigned elements and element names.
     */
    public MatchConditionOnElements()
    {
        elements = null;
        names = null;
    }
    
    /**
     * Creates the condition and assigns a pseudo class to a given element.
     * @param e the element
     * @param pseudoClass the pseudo class to be assigned
     */
    public MatchConditionOnElements(Element e, PseudoClass pseudoClass)
    {
        addMatch(e, pseudoClass);
    }
    
    /**
     * Creates the condition and assigns a pseudo class to a given element name. Element names are case-insensitive.
     * @param name the element name
     * @param pseudoClass the pseudo class to be assigned
     */
    public MatchConditionOnElements(String name, PseudoClass pseudoClass)
    {
        addMatch(name, pseudoClass);
    }
    
    /**
     * Assigns a pseudo class to the given element. Multiple pseudo classes may be assigned to a single element.
     * @param e the DOM element
     * @param pseudoClass the pseudo class to be assigned
     */
    public void addMatch(Element e, PseudoClass pseudoClass)
    {
        if (elements == null)
            elements = new HashMap<Element, Set<PseudoClass>>();
        
        Set<PseudoClass> classes = elements.get(e);
        if (classes == null)
        {
            classes = new HashSet<PseudoClass>(2);
            elements.put(e, classes);
        }
        classes.add(pseudoClass);
    }
    
    /**
     * Removes the pseudo class from the given element.
     * @param e the DOM element
     * @param pseudoClass the pseudo class to be removed
     */
    public void removeMatch(Element e, PseudoClass pseudoClass)
    {
        if (elements != null)
        {
            Set<PseudoClass> classes = elements.get(e);
            if (classes != null)
                classes.remove(pseudoClass);
        }   
    }
    
    /**
     * Assigns a pseudo class to the given element name. Element names are case-insensitive.
     * Multiple pseudo classes may be assigned to a single element name.
     * @param name the element name
     * @param pseudoClass the pseudo class to be assigned
     */
    public void addMatch(String name, PseudoClass pseudoClass)
    {
        if (names == null)
            names = new HashMap<String, Set<PseudoClass>>();
        
        Set<PseudoClass> classes = names.get(name);
        if (classes == null)
        {
            classes = new HashSet<PseudoClass>(2);
            names.put(name, classes);
        }
        classes.add(pseudoClass);
    }
    
    /**
     * Removes the pseudo class from the given element name. Element names are case-insensitive.
     * @param name the element name
     * @param pseudoClass the pseudo class to be removed
     */
    public void removeMatch(String name, PseudoClass pseudoClass)
    {
        if (names != null)
        {
            Set<PseudoClass> classes = names.get(name);
            if (classes != null)
                classes.remove(pseudoClass);
        }   
    }
    
    public boolean isSatisfied(Element e, SelectorPart selpart)
    {
        if (selpart instanceof PseudoClass)
        {
            PseudoClass required = ((PseudoClass) selpart);
            
            if (elements != null)
            {
                Set<PseudoClass> pseudos = elements.get(e);
                if (pseudos != null)
                    return pseudos.contains(required);
            }
            
            if (names != null)
            {
                Set<PseudoClass> pseudos = names.get(e.getTagName().toLowerCase());
                if (pseudos != null)
                    return pseudos.contains(required);
            }
            
            return false;
        }
        else
            return false;
    }
}
