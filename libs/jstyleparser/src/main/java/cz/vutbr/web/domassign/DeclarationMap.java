/**
 * DeclarationMap.java
 *
 * Created on 22.1.2010, 16:23:07 by burgetr
 */
package cz.vutbr.web.domassign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Selector.PseudoElement;

/**
 * This is a map that assigns a sorted list of declarations to each element 
 * and an optional pseudo-element. 
 * 
 * @author burgetr
 */
public class DeclarationMap extends MultiMap<Element, PseudoElement, List<Declaration>>
{
    
    private static final Logger log = LoggerFactory.getLogger(DeclarationMap.class);
    
    /**
     * Adds a declaration for a specified list. If the list does not exist yet, it is created.
     * @param el the element that the declaration belongs to
     * @param pseudo an optional pseudo-element or null
     * @param decl the new declaration
     */
    public void addDeclaration(Element el, PseudoElement pseudo, Declaration decl)
    {
        List<Declaration> list = getOrCreate(el, pseudo);
        list.add(decl);
    }
    
    /**
     * Sorts the given list according to the rule specificity.
     * @param el the element to which the list is assigned
     * @param pseudo an optional pseudo-element or null
     */
    public void sortDeclarations(Element el, PseudoElement pseudo)
    {
        List<Declaration> list = get(el, pseudo);
        if (list != null) {
            Collections.sort(list);
            log.debug("Sorted {} declarations.", list.size());
            log.trace("With values: {}", list);
        }
    }

	@Override
	protected List<Declaration> createDataInstance()
	{
		return new ArrayList<Declaration>();
	}
    
    
}

