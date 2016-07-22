package cz.vutbr.web.csskit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import cz.vutbr.web.css.Selector;

public class ElementUtil {

	public static final String CLASS_DELIM = " ";
	public static final String CLASS_ATTR = "class";
	public static final String ID_ATTR = "id";
	
	public static String getAttribute(Element e, String name)
	{
	    return e.hasAttribute(name) ? e.getAttribute(name) : "";
	}
	
	public static Collection<String> elementClasses(Element e) 
	{
	    if (e.hasAttribute(CLASS_ATTR))
	    {
    		String classNames = getAttribute(e, CLASS_ATTR);
    		
    		Collection<String> list = new ArrayList<String>();
    		for (String cname : classNames.toLowerCase().split(CLASS_DELIM)) 
    		{
    			cname = cname.trim();
    			if(cname.length() > 0)
    				list.add(cname);
    		}
    		return list;
	    }
	    else
	        return Collections.emptyList();
	}
	
	public static boolean matchesClassOld(Element e, String className) 
	{
        if (e.hasAttribute(CLASS_ATTR))
        {
            String classNames = getAttribute(e, CLASS_ATTR).toLowerCase();
            int len = className.length();
    	    int start = classNames.indexOf(className.toLowerCase());
    	    if (start == -1)
    	        return false;
    	    else
    	        return ((start == 0 || Character.isWhitespace(classNames.charAt(start - 1))) &&
    	                (start + len == classNames.length() || Character.isWhitespace(classNames.charAt(start + len))));
        }
        else
            return false;
	}
	
	public static boolean matchesClass(Element e, String className)
    {
        if (e.hasAttribute(CLASS_ATTR))
        {
            String classNames = getAttribute(e, CLASS_ATTR).toLowerCase();
            String search = className.toLowerCase();
            int len = className.length();
            int lastIndex = 0;
            
            while ((lastIndex = classNames.indexOf(search, lastIndex)) != -1) {
                if ((lastIndex == 0 || Character.isWhitespace(classNames.charAt(lastIndex - 1))) &&
                        (lastIndex + len == classNames.length() || Character.isWhitespace(classNames.charAt(lastIndex + len)))) {
                    return true;
                }
                lastIndex += len;
            }
            return false;
        }
        else
            return false;
    }

	
	public static String elementID(Element e) 
	{
		String id = getAttribute(e, ID_ATTR);
		return id;
	}
	
	public static boolean matchesID(Element e, String id) 
	{
		return id.equalsIgnoreCase(elementID(e));
	}
	
	public static String elementName(Element e) 
	{
		String name = e.getNodeName();
		return name;
	}
	
	public static boolean matchesName(Element e, String name)
	{
		if (name == null)
		    return false;
		else
		    return name.equalsIgnoreCase(elementName(e));
	}
	
	public static boolean matchesAttribute(Element e, String namespaceURI, String localName, String value, Selector.Operator o) 
	{
		if (namespaceURI == null) {
			NamedNodeMap attributes = e.getAttributes();
			Set<String> namespaces = new HashSet<String>();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attr = attributes.item(i);
				String attrNS = attr.getNamespaceURI();
				if (attrNS == null) attrNS = "";
				namespaces.add(attrNS);
			}
			for (String ns : namespaces)
				if (matchesAttribute(e, ns, localName, value, o))
					return true;
			return false;
		}
		String attributeValue = namespaceURI.equals("") ? e.getAttribute(localName) : e.getAttributeNS(namespaceURI, localName);
	    if (attributeValue.length() >  0 && o != null)
	    {
    		switch(o) {
        		case EQUALS:
        			return attributeValue.equals(value);
        		case INCLUDES:
        		    if (value.isEmpty() || containsWhitespace(value))
        		        return false;
        		    else
        		    {
            			attributeValue = " " + attributeValue + " ";
            			return attributeValue.matches(".* " + value + " .*");
        		    }
        		case DASHMATCH:
        			return attributeValue.matches("^" + value + "(-.*|$)");
        		case CONTAINS:
        			return !value.isEmpty() && attributeValue.matches(".*" + value + ".*");
        		case STARTSWITH:
        			return !value.isEmpty() && attributeValue.matches("^" + value + ".*");
                case ENDSWITH:
                    return !value.isEmpty() && attributeValue.matches(".*" + value + "$");
        		default:
        			return true;
    		}
	    }
	    else
	        return false;
	}
	
	private static boolean containsWhitespace(String s)
	{
	    for (int i = 0; i < s.length(); i++)
	    {
	        if (Character.isWhitespace(s.charAt(i)))
	                return true;
	    }
	    return false;
	}
	
}
