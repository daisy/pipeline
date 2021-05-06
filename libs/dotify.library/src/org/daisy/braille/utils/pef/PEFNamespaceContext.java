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
package org.daisy.braille.utils.pef;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

/**
 * Provides a NamespaceContext implementation for PEF 1.0.
 * It includes namespace prefix mapping for namespaces in PEF 1.0, namely:
 * <ul>
 *  <li>http://www.daisy.org/ns/2008/pef</li>
 *  <li>http://purl.org/dc/elements/1.1/</li>
 * </ul>
 *
 * @author Joel Håkansson
 */
public class PEFNamespaceContext implements NamespaceContext {
    private Map<String, String> namespaces;
    private Map<String, String> prefixes;

    /**
     * Creates a new PEFNamespaceContext using the prefixes
     * <strong>pef</strong> for <code>http://www.daisy.org/ns/2008/pef</code> and <strong>dc</strong> for
     * <code>http://purl.org/dc/elements/1.1/</code>.
     */
    public PEFNamespaceContext() {
        this("pef", "dc");
    }

    /**
     * Creates a new PEFNamespaceContext using the supplied prefixes.
     *
     * @param pefPrefix the prefix to use for <code>http://www.daisy.org/ns/2008/pef</code>
     * @param dcPrefix  the prefix to use for <code>http://purl.org/dc/elements/1.1/</code>
     */
    public PEFNamespaceContext(String pefPrefix, String dcPrefix) {
        namespaces = new HashMap<>();
        prefixes = new HashMap<>();
        namespaces.put(pefPrefix, "http://www.daisy.org/ns/2008/pef");
        namespaces.put(dcPrefix, "http://purl.org/dc/elements/1.1/");
        for (String s : namespaces.keySet()) {
            prefixes.put(namespaces.get(s), s);
        }
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return namespaces.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return prefixes.get(namespaceURI);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return Collections.singleton(getPrefix(namespaceURI)).iterator();
    }

}
