/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.pef;

import java.util.Map;

/**
 * Provides an XML element data object.
 * @author Joel Håkansson
 *
 */
public class Element {
	private String uri;
	private String localName;
	private Map<String, String> atts;
	
	/**
	 * Creates a new Element
	 * @param uri the namespace uri for the element
	 * @param localName the local name for the element
	 * @param attributes the attributes for the element
	 */
	public Element(String uri, String localName, Map<String, String> attributes) {
		this.uri = uri;
		this.localName = localName;
		this.atts = attributes;
	}
	
	/**
	 * Gets this element's namespace uri
	 * @return returns this element's namespace uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Gets this elements local name
	 * @return returns this element's local name
	 */
	public String getLocalName() {
		return localName;
	}
	
	/**
	 * Gets this element's attributes
	 * @return returns this element's attributes
	 */
	public Map<String, String> getAttributes() {
		return atts;
	}
}
