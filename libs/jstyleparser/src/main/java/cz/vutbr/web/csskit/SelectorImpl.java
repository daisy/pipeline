package cz.vutbr.web.csskit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.MatchCondition;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.CombinedSelector.Specificity;
import cz.vutbr.web.css.CombinedSelector.Specificity.Level;

/**
 * Encapsulates one selector for CSS declaration.
 * CombinedSelector can contain classes, attributes, ids, pseudo attributes,
 * and element name, together with combinator according to next placed selectors
 * 
 * @author kapy
 * @author Jan Svercl, VUT Brno, 2008   	    
 */
public class SelectorImpl extends AbstractRule<Selector.SelectorPart> implements Selector {

	protected Combinator combinator;
    
	/**
	 * @return the combinator
	 */
	public Combinator getCombinator() {
		return combinator;
	}

	/**
	 * @param combinator the combinator to set
	 */
	public Selector setCombinator(Combinator combinator) {
		this.combinator = combinator;
		return this;
	}

	@Override
    public String toString() {
    	
    	StringBuilder sb = new StringBuilder();
    	
    	if(combinator!=null) sb.append(combinator.value());    	
    	sb = OutputUtil.appendList(sb, list, OutputUtil.EMPTY_DELIM);
    	
    	return sb.toString();
    }

	
    public String getClassName() {
        String className = null;
        for(SelectorPart item : list) {
            if(item instanceof ElementClass) {
                className = ((ElementClass)item).getClassName();
            }
        }
        return className;
    }
    
    
    public String getIDName() {
        String idName = null;
        for(SelectorPart item : list) {
            if(item instanceof ElementID)
            	idName = ((ElementID)item).getID();
        }
        return idName;
    }
    
    public ElementName getElementName() {
    	ElementName elementName = null;
    	for(SelectorPart item : list) {
    		if(item instanceof ElementName)
    			elementName = (ElementName)item;
    	}
    	return elementName;
    }
	
	public PseudoElement getPseudoElement() {
		// FIXME
		for (SelectorPart item : list)
			if (item instanceof PseudoElement)
				return (PseudoElement)item; // pseudo-elements may only be appended after the last simple selector of the selector
		return null;
	}
	
    public boolean matches(Element e) {
    	
		// check other items of simple selector
		for(SelectorPart item : list) {
			if(item == null || !item.matches(e, CSSFactory.getDefaultMatchCondition())) //null in case of syntax error (missing term)
				return false;
		}
		
		// we passed checking
		return true;
    }
    
    public boolean matches(Element e, MatchCondition cond) {
        
        // check other items of simple selector
        for(SelectorPart item : list) {
            if(item == null || !item.matches(e, cond)) //null in case of syntax error (missing term)
                return false;
        }
        // we passed checking
        return true;
    }
    
    /**
     * Computes specificity of this selector
     */
    public void computeSpecificity(CombinedSelector.Specificity spec) {   	
		for(SelectorPart item: list) {
			item.computeSpecificity(spec);
		}
    }   
       
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((combinator == null) ? 0 : combinator.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof SelectorImpl))
			return false;
		SelectorImpl other = (SelectorImpl) obj;
		if (combinator == null) {
			if (other.combinator != null)
				return false;
		} else if (!combinator.equals(other.combinator))
			return false;
		return true;
	}

	 
    // ============================================================
    // implementation of intern classes	

	/**
	 * Element name
	 * @author kapy, bfrees
	 */
	public static class ElementNameImpl implements ElementName {
		
		private String localName;
		private String namespaceURI;
		private String prefix;
		private boolean locked = false;
		
		protected ElementNameImpl(String namespaceURI, String localName, String prefix) {
			setName(namespaceURI, localName, prefix);
		}
		
		public void computeSpecificity(CombinedSelector.Specificity spec) {
			if(!WILDCARD.equals(localName))
				spec.add(Level.D);
		}
		
		private boolean matchesLocalName(Element e) {
			if (localName.equals(WILDCARD))
				return true;
			else
				return localName.equalsIgnoreCase(e.getLocalName());
		}
		
		private boolean matchesNamespaceURI(Element e) {
			if (namespaceURI == null)
				return true;
			else {
				String elementNS = e.getNamespaceURI();
				if (elementNS == null) elementNS = "";
				return namespaceURI.equals(elementNS);
			}
		}
		
		public boolean matches(Element e, MatchCondition cond) {
			return matchesLocalName(e) && matchesNamespaceURI(e);
		}
		
		public String getLocalName() {
			return localName;
		}
		
		public String getNamespaceURI() {
			return namespaceURI;
		}
		
		public String getPrefix() {
			return prefix;
		}
		
		public ElementName setName(String namespaceURI, String localName, String prefix) {
			if (locked)
				throw new UnsupportedOperationException("Immutable object");
			if (localName == null || localName.equals(""))
				throw new IllegalArgumentException("Invalid localName (" + localName + ")");
			if ("".equals(prefix) && !"".equals(namespaceURI)
			    || WILDCARD.equals(prefix) && namespaceURI != null
			    || namespaceURI == null && prefix != null && !prefix.equals(WILDCARD))
				throw new IllegalArgumentException("Invalid combination of prefix (" + prefix + ")"
				                                   + " and namespaceURI (" + namespaceURI + ")");
			this.localName = localName;
			this.namespaceURI = namespaceURI;
			this.prefix = prefix;
			return this;
		}
		
		public ElementName lock() {
			locked = true;
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (prefix != null)
				sb.append(prefix + "|");
			sb.append(localName);
			return sb.toString();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((localName == null) ? 0 : localName.hashCode());
			result = prime * result + ((namespaceURI == null) ? 0 : namespaceURI.hashCode());
			result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ElementNameImpl))
				return false;
			ElementNameImpl other = (ElementNameImpl) obj;
			if (localName == null) {
				if (other.localName != null)
					return false;
			} else if (!localName.equals(other.localName))
				return false;
			if (namespaceURI == null) {
				if (other.namespaceURI != null)
					return false;
			} else if (!namespaceURI.equals(other.namespaceURI))
				return false;
			if (prefix == null) {
				if (other.prefix != null)
					return false;
			} else if (!prefix.equals(other.prefix))
				return false;
			return true;
		}
    	
    }
    
    /**
     * Element class
     * @author kapy
     *
     */
    public static class ElementClassImpl implements ElementClass {

    	private String className;
    	
    	protected ElementClassImpl(String className) {
    		setClassName(className);
    	}
    	
    	public void computeSpecificity(Specificity spec) {
    		spec.add(Level.C);
    	}
    	
    	public boolean matches(Element e, MatchCondition cond) {
    		return ElementUtil.matchesClass(e, className);
    	}
    	
		public String getClassName() {
			return className;
		}
    	
		public ElementClass setClassName(String className) {
			if(className == null)
				throw new IllegalArgumentException("Invalid element class (null)");
			
			this.className = className;
			return this;
		}
    	
    	@Override
    	public String toString() {
    		return "." + className;
    	}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((className == null) ? 0 : className.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ElementClassImpl))
				return false;
			ElementClassImpl other = (ElementClassImpl) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			return true;
		}    	
	}
	
	/**
	 * Pseudo class
	 * @author bertfrees
	 *
	 */
	public static class PseudoClassImpl implements PseudoClass {
		
		private static enum PseudoClassDef {
			ACTIVE("active"),
			FOCUS("focus"),
			HOVER("hover"),
			LINK("link"),
			VISITED("visited"),
			FIRST_CHILD("first-child"),
			LAST_CHILD("last-child"),
			ONLY_CHILD("only-child"),
			ONLY_OF_TYPE("only-of-type"),
			NTH_CHILD("nth-child", 1),
			NTH_LAST_CHILD("nth-last-child", 1),
			NTH_OF_TYPE("nth-of-type", 1),
			NTH_LAST_OF_TYPE("nth-last-of-type", 1),
			FIRST_OF_TYPE("first-of-type"),
			LAST_OF_TYPE("last-of-type"),
			ROOT("root"),
			EMPTY("empty"),
			BLANK("blank"),
			LANG("lang", 1),
			ENABLED("enabled"),
			DISABLED("disabled"),
			CHECKED("checked"),
			TARGET("target");
			
			private final String name;
			private final int minArgs;
			private final int maxArgs;
			
			private PseudoClassDef(String name) {
				this.name = name;
				this.minArgs = 0;
				this.maxArgs = 0;
			}
			
			private PseudoClassDef(String name, int args) {
				this(name, args, args);
			}
			
			private PseudoClassDef(String name, int minArgs, int maxArgs) {
				this.name = name;
				this.minArgs = minArgs;
				this.maxArgs = maxArgs;
			}
		}
		
		private static final HashMap<String,PseudoClassDef> PSEUDO_CLASS_DEFS;
		static {
			PSEUDO_CLASS_DEFS = new HashMap<String,PseudoClassDef>();
			for (PseudoClassDef d : PseudoClassDef.values())
				PSEUDO_CLASS_DEFS.put(d.name, d);
		}
	
		private final PseudoClassDef def;
		private final List<String> args;
		
		//decoded element index for nth-XXXX properties -- values a and b in the an+b specification
		private int[] elementIndex;
		
		static final Pattern WHITESPACE_RE = Pattern.compile("\\s*");
		
		protected PseudoClassImpl(String name, String... args) {
			name = name.toLowerCase(); // Pseudo-class names are case-insensitive
			if (PSEUDO_CLASS_DEFS.containsKey(name))
				def = PSEUDO_CLASS_DEFS.get(name);
			else
				throw new IllegalArgumentException(name + " is not a valid pseudo-class name");
			if (args.length > 0 && def.maxArgs == 0)
				throw new IllegalArgumentException(name + " must not be a function");
			if (args.length == 0 && def.minArgs > 0)
				throw new IllegalArgumentException(name + " must be a function");
			if (def.minArgs > 0) {
				if (args.length < def.minArgs || args.length > def.maxArgs)
					throw new IllegalArgumentException(name + " requires " + def.minArgs
					                                   + (def.maxArgs > def.minArgs ? ".." + def.maxArgs : "") + " "
					                                   + (def.minArgs == 1 && def.maxArgs == 1 ? "argument" : "arguments"));
				this.args = new ArrayList<String>();
				for (String a : args)
					this.args.add(a);
			} else
				this.args = null;
			
			//decode the element index for nth-X properties
			elementIndex = null;
			if (def == PseudoClassDef.NTH_CHILD
				|| def == PseudoClassDef.NTH_LAST_CHILD
				|| def == PseudoClassDef.NTH_OF_TYPE
				|| def == PseudoClassDef.NTH_LAST_OF_TYPE) {
				try {
					elementIndex = decodeIndex(args[0]);
				} catch (NumberFormatException e) {
				}
			}
		}
		
		public void computeSpecificity(Specificity spec) {
			spec.add(Level.C);
		}
		
		public boolean matches(Element e, MatchCondition cond) {
			
				switch (def) {
					case FIRST_CHILD:
					case LAST_CHILD:
					case ONLY_CHILD:
					    if (e.getParentNode().getNodeType() == Node.ELEMENT_NODE)
					    {
    						boolean first = false;
    						boolean last = false;
    						if (def != PseudoClassDef.LAST_CHILD) {
    							Node prev = e;
    							do {
    								prev = prev.getPreviousSibling();
    								if (prev == null) {
    								    first = true;
    								    break;
    								}
    							} while(prev.getNodeType() != Node.ELEMENT_NODE);
    						}
    						if (def != PseudoClassDef.FIRST_CHILD) {
    							Node next = e;
    							do {
    								next = next.getNextSibling();
    								if (next == null) {
    								    last = true;
    								    break; 
    								}
    							} while(next.getNodeType() != Node.ELEMENT_NODE);
    						}
    						switch (def) {
    							case FIRST_CHILD: return first;
    							case LAST_CHILD: return last;
    							default: return first && last; //ONLY_CHILD
    						}
					    }
					    else
					        return false;
                    case FIRST_OF_TYPE:
                    case LAST_OF_TYPE:
                    case ONLY_OF_TYPE:
                        if (e.getParentNode().getNodeType() == Node.ELEMENT_NODE)
                        {
                            boolean firstt = false;
                            boolean lastt = false;
                            if (def != PseudoClassDef.LAST_OF_TYPE) {
                                Node prev = e;
                                firstt = true;
                                do {
                                    prev = prev.getPreviousSibling();
                                    if (prev != null && prev.getNodeType() == Node.ELEMENT_NODE
                                            && isSameElementType(e, (Element) prev))
                                        firstt = false;
                                } while (prev != null && firstt);
                            }
                            if (def != PseudoClassDef.FIRST_OF_TYPE) {
                                Node next = e;
                                lastt = true;
                                do {
                                    next = next.getNextSibling();
                                    if (next != null && next.getNodeType() == Node.ELEMENT_NODE
                                            && isSameElementType(e, (Element) next))
                                        lastt = false;
                                } while(next != null && lastt);
                            }
                            switch (def) {
                                case FIRST_OF_TYPE: return firstt;
                                case LAST_OF_TYPE: return lastt;
                                default: return firstt && lastt; //ONLY_OF_TYPE
                            }
                        }
                        else
                            return false;
                    case NTH_CHILD:
                        return positionMatches(countSiblingsBefore(e, false) + 1, elementIndex);
                    case NTH_LAST_CHILD:
                        return positionMatches(countSiblingsAfter(e, false) + 1, elementIndex);
                    case NTH_OF_TYPE:
                        return positionMatches(countSiblingsBefore(e, true) + 1, elementIndex);
                    case NTH_LAST_OF_TYPE:
                        return positionMatches(countSiblingsAfter(e, true) + 1, elementIndex);
                    case ROOT:
                        return e.getParentNode().getNodeType() == Node.DOCUMENT_NODE;
                    case BLANK:
                    case EMPTY:
                        NodeList elist = e.getChildNodes();
                        for (int i = 0; i < elist.getLength(); i++)
                        {
                            Node n = elist.item(i);
                            short t = n.getNodeType();
                            if (t == Node.ELEMENT_NODE || t == Node.CDATA_SECTION_NODE || t == Node.ENTITY_REFERENCE_NODE)
                                return false;
                            else if (t == Node.TEXT_NODE)
                                if (def == PseudoClassDef.EMPTY || !WHITESPACE_RE.matcher(n.getNodeValue()).matches())
                                    return false;
                        }
                        return true;
					default:
					    //match all pseudo classes specified by an additional condition (usually used for using LINK pseudo class for links)
						if (cond.isSatisfied(e, this)) 
						{
							return true;
						}
				}
			
			return false;
		}
		
		/**
		 * Checks whether the element position matches a <code>an+b</code> index specification.
		 * @param pos The element position according to some counting criteria.
		 * @param n The index specifiaction <code>an+b</code> - <code>a</code> and <code>b</code> values in array int[2].
		 * @return <code>true</code> when the position matches the index.
		 */
		protected boolean positionMatches(int pos, int[] n)
		{
		    if (n != null)
		    {
                try {
                    int an = pos - n[1];
                    if (n[0] == 0)
                        return an == 0;
                    else
                        return an * n[0] >= 0 && an % n[0] == 0;
                } catch (NumberFormatException ex) {
                    return false;
                }
		    }
		    else //no indices specified (syntax error or missing values)
		        return false;
		}
		
		/**
		 * Decodes the element index in the <code>an+b</code> form.
		 * @param index the element index string
		 * @return an array of two integers <code>a</code> and <code>b</code>
		 * @throws NumberFormatException
		 */
		protected static int[] decodeIndex(String index) throws NumberFormatException
		{
		    String s = index.toLowerCase().trim();
		    if (s.equals("odd")){
                int[] ret = {2, 1};
                return ret;
		    }
		    else if (s.equals("even")){
                int[] ret = {2, 0};
                return ret;
            }
		    else {
                int[] ret = {0, 0};
    		    int n = s.indexOf('n');
    		    if (n != -1)
    		    {
    		        String sa = s.substring(0, n).trim();
                    if (sa.length() == 0)
                        ret[0] = 1;
                    else if (sa.equals("-"))
                        ret[0] = -1;
                    else
                        ret[0] = Integer.parseInt(sa);
                    
    		        n++;
    		        StringBuilder sb = new StringBuilder();
    		        while (n < s.length())
    		        {
    		            char ch = s.charAt(n);
    		            if (ch != '+' && !Character.isWhitespace(ch))
    		                sb.append(ch);
    		            n++;
    		        }
    		        if (sb.length() > 0)
    	                ret[1] = Integer.parseInt(sb.toString());
    		    }
    		    else
    		        ret[1] = Integer.parseInt(s);
    		    
    		    return ret;
		    }
		}
		
		/**
		 * Computes the count of element siblings before the given element in the DOM tree.
		 * @param e The element to be examined
		 * @param sameType when set to <code>true</code> only the element with the same type are considered.
		 *                 Otherwise, all elements are considered.
		 * @return the number of preceding siblings
		 */
		protected int countSiblingsBefore(Element e, boolean sameType)
		{
		    int cnt = 0;
		    Node prev = e;
		    do {
		        prev = prev.getPreviousSibling();
		        if (prev != null && prev.getNodeType() == Node.ELEMENT_NODE)
		        {
		            if (!sameType || isSameElementType(e, (Element) prev))
		                cnt++;
		        }
		    } while (prev != null);
		    
		    return cnt;
		}
		
        /**
         * Computes the count of element siblings after the given element in the DOM tree.
         * @param e The element to be examined
         * @param sameType when set to <code>true</code> only the element with the same type are considered.
         *                 Otherwise, all elements are considered.
         * @return the number of following siblings
         */
        protected int countSiblingsAfter(Element e, boolean sameType)
        {
            int cnt = 0;
            Node next = e;
            do {
                next = next.getNextSibling();
                if (next != null && next.getNodeType() == Node.ELEMENT_NODE)
                {
                    if (!sameType || isSameElementType(e, (Element) next))
                        cnt++;
                }
            } while (next != null);
            
            return cnt;
        }
        
		/**
		 * Checks whether two elements have the same name.
		 * @param e1 the first element
		 * @param e2 the second element
		 * @return <code>true</code> when the elements have the same names
		 */
		protected boolean isSameElementType(Element e1, Element e2)
		{
		    return e1.getNodeName().equalsIgnoreCase(e2.getNodeName());
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(OutputUtil.PAGE_OPENING).append(def.name);
			if (args != null) {
				sb.append(OutputUtil.FUNCTION_OPENING);
				OutputUtil.appendList(sb, args, ", ");
				sb.append(OutputUtil.FUNCTION_CLOSING);
			}
			sb.append(OutputUtil.PAGE_CLOSING);
			return sb.toString();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + def.hashCode();
			result = prime * result + (args != null ? args.hashCode() : 0);
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof PseudoClassImpl))
				return false;
			PseudoClassImpl other = (PseudoClassImpl) obj;
			if (def != other.def)
				return false;
			if (args != null && !args.equals(other.args))
				return false;
			return true;
		}
	}
	
	/**
	 * Pseudo element
	 * @author bertfrees
	 */
	public static class PseudoElementImpl implements PseudoElement {
		
		final static HashSet<String> PSEUDO_CLASS_DEFS = new HashSet<String>();
		static {
			PSEUDO_CLASS_DEFS.add(PseudoElement.FIRST_LINE);
			PSEUDO_CLASS_DEFS.add(PseudoElement.FIRST_LETTER);
			PSEUDO_CLASS_DEFS.add(PseudoElement.BEFORE);
			PSEUDO_CLASS_DEFS.add(PseudoElement.AFTER);
		}
		
		private final String name;
		
		protected PseudoElementImpl(String name) {
			if (PSEUDO_CLASS_DEFS.contains(name))
				this.name = name;
			else
			throw new IllegalArgumentException(name + " is not a valid pseudo-element name");
		}
		
		public String getName() {
			return name;
		}
		
		public String[] getArguments() {
			return new String[]{};
		}
		
		public void computeSpecificity(Specificity spec) {
			spec.add(Level.D);
		}
		
		public boolean matches(Element e, MatchCondition cond) {
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb
				.append(OutputUtil.PAGE_OPENING)
				.append(OutputUtil.PAGE_OPENING)
				.append(name)
				.append(OutputUtil.PAGE_CLOSING);
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof PseudoElementImpl))
				return false;
			PseudoElementImpl other = (PseudoElementImpl) obj;
			if (!name.equals(other.name))
				return false;
			return true;
		}
	}
	
    /**
     * Element ID
     * @author kapy
     *
     */
    public static class ElementIDImpl implements ElementID {
    	
    	private String id;
    	
    	protected ElementIDImpl(String value) {
    		setID(value);
    	}
    	
    	public void computeSpecificity(Specificity spec) {
    		spec.add(Level.B);
		}    	
    	
    	public boolean matches(Element e, MatchCondition cond) {
    		return ElementUtil.matchesID(e, id);
    	}
    	
    	public ElementID setID(String id) {
    		if(id==null)
    			throw new IllegalArgumentException("Invalid element ID (null)");
    		
    		this.id = id;
    		return this;
    	}
    	
    	public String getID() {
    		return id;
    	}
    	    	
    	@Override
    	public String toString() {
    		return "#" + id;
    	}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ElementIDImpl))
				return false;
			ElementIDImpl other = (ElementIDImpl) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
    	
    	
    }
    
    /**
     * Attribute holder
     * @author kapy
     *
     */
    public static class ElementAttributeImpl implements ElementAttribute {
    	
    	/** Operator between attribute and value */
    	private Operator operator;
    	
    	private String localName;
    	private String namespaceURI;
    	private String prefix;
    	private String value;
    	private boolean isStringValue;
    	
    	protected ElementAttributeImpl(String value, boolean isStringValue, Operator operator,
    			String namespaceURI, String localName, String prefix) {
    		this.isStringValue = isStringValue;
    		this.operator = operator;
    		setAttribute(namespaceURI, localName, prefix);
    		setValue(value);
    	}
    	
    	/**
		 * @return the operator
		 */
		public Operator getOperator() {
			return operator;
		}

		/**
		 * @param operator the operator to set
		 */
		public void setOperator(Operator operator) {
			this.operator = operator;
		}



		public String getLocalName() {
			return localName;
		}

		public String getNamespaceURI() {
			return namespaceURI;
		}

		public String getPrefix() {
			return prefix;
		}


		public ElementAttribute setAttribute(String namespaceURI, String localName, String prefix) {
			if (localName == null || localName.equals(""))
				throw new IllegalArgumentException("Invalid localName (" + localName + ")");
			if ((prefix == null || prefix.equals("")) && !"".equals(namespaceURI)
			    || WILDCARD.equals(prefix) && namespaceURI != null
			    || namespaceURI == null && !WILDCARD.equals(prefix))
				throw new IllegalArgumentException("Invalid combination of prefix (" + prefix + ")"
				                                   + " and namespaceURI (" + namespaceURI + ")");
			this.localName = localName;
			this.namespaceURI = namespaceURI;
			this.prefix = prefix;
			return this;
		}
		
		public void computeSpecificity(Specificity spec) {
			spec.add(Level.C);
		}
		
		public boolean matches(Element e, MatchCondition cond) {
			return ElementUtil.matchesAttribute(e, namespaceURI, localName, value, operator);
		}
    	
		public String getValue() {
			return value;
		}
		
    	public ElementAttribute setValue(String value) {
    		this.value = value;
    		return this;
    	}
		
		@Override
    	public String toString() {
    		StringBuilder sb = new StringBuilder();
    		
    		sb.append(OutputUtil.ATTRIBUTE_OPENING);
    		if (prefix != null)
    			sb.append(prefix + "|");
    		sb.append(localName);
    		sb.append(operator.value());

    		if(isStringValue && value!=null)
    			sb.append(OutputUtil.STRING_OPENING);
    		
    		if(value != null) sb.append(value);
    		
    		if(isStringValue && value!=null)
    			sb.append(OutputUtil.STRING_CLOSING);

    		sb.append(OutputUtil.ATTRIBUTE_CLOSING);
    		
    		return sb.toString();
    	}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((localName == null) ? 0 : localName.hashCode());
			result = prime * result
					+ ((namespaceURI == null) ? 0 : namespaceURI.hashCode());
			result = prime * result
					+ ((prefix == null) ? 0 : prefix.hashCode());
			result = prime * result + (isStringValue ? 1231 : 1237);
			result = prime * result
					+ ((operator == null) ? 0 : operator.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ElementAttributeImpl))
				return false;
			ElementAttributeImpl other = (ElementAttributeImpl) obj;
			if (localName == null) {
				if (other.localName != null)
					return false;
			} else if (!localName.equals(other.localName))
				return false;
			if (namespaceURI == null) {
				if (other.namespaceURI != null)
					return false;
			} else if (!namespaceURI.equals(other.namespaceURI))
				return false;
			if (prefix == null) {
				if (other.prefix != null)
					return false;
			} else if (!prefix.equals(other.prefix))
				return false;
			if (isStringValue != other.isStringValue)
				return false;
			if (operator == null) {
				if (other.operator != null)
					return false;
			} else if (!operator.equals(other.operator))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
		
    }
    
    public static class ElementDOMImpl implements ElementDOM {
 
        /** The element used as the selector */
    	private Element elem;
    	/** When set to true, the selector has a maximal specificity (inline). Otherwise, it has a minimal specificity. */
    	private boolean inlinePriority;
    	
    	protected ElementDOMImpl(Element e, boolean inlinePriority) {
    		this.elem = e;
    		this.inlinePriority = inlinePriority;
    	}

		public Element getElement() {
			return elem;
		}

		public ElementDOM setElement(Element e) {
			this.elem = e;
			return this;
		}

		public void computeSpecificity(Specificity spec) {
		    if (inlinePriority)
		        spec.add(Level.A);
		}

		public boolean matches(Element e, MatchCondition cond) {
			return elem.equals(e);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((elem == null) ? 0 : elem.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ElementDOMImpl))
				return false;
			ElementDOMImpl other = (ElementDOMImpl) obj;
			if (elem == null) {
				if (other.elem != null)
					return false;
			} else if (!elem.equals(other.elem))
				return false;
			return true;
		}
		
		
    	
    }
    
}
