/*
 * org.daisy.util (C) 2005-2008 Daisy Consortium
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
package org.daisy.dotify.common.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Linus Ericson
 */
public class CachingURIResolver implements URIResolver {

    private final XMLReader reader;
    
    public CachingURIResolver() throws XMLToolsException {
    	this(SAXParserFactory.newInstance());
    }

    public CachingURIResolver(SAXParserFactory parserFactory) throws XMLToolsException {
        parserFactory.setNamespaceAware(true);
        SAXParser parser;
        try {
            parser = parserFactory.newSAXParser();
            reader = parser.getXMLReader();
        } catch (ParserConfigurationException e) {
            throw new XMLToolsException(e);
        } catch (SAXException e) {
            throw new XMLToolsException(e);
        }        
        reader.setEntityResolver(new EntityResolverCache());
    }    
    
    public Source resolve(String href, String base) throws TransformerException {
        //System.err.println("resolving href:" + href + ", base: " + base);
        
        // Self reference, let the internal URIResolver handle it.
        if (href == null || "".equals(href)) {
            return null;
        }
        
        try {
            URI hrefUri = new URI(href);
            URI baseUri = new URI(base);
            
            URI resolved = URIUtils.resolve(baseUri, hrefUri);
            
            if ("file".equals(resolved.getScheme())) {
                Source xmlSource = new SAXSource(reader, new InputSource(new FileInputStream(new File(resolved))));
                return xmlSource;
            }
        } catch (URISyntaxException e) {
            throw new TransformerException(e);
        } catch (FileNotFoundException e) {
            throw new TransformerException(e);
        }        
        return null;
    }

}
