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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An XMLResolver that uses a SAX EntityResolver for entity resolution.
 * @author Linus Ericson
 */
public class XMLResolverAdapter implements XMLResolver {

    private final EntityResolver resolver;
    
    public XMLResolverAdapter(EntityResolver entityResolver) {
        resolver = entityResolver;
    }
    
    @Override
	@SuppressWarnings("unused")
    public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException {
        try {
            InputSource is = resolver.resolveEntity(publicID, systemID);
            if (is == null) {
                return null;
            }
            InputStream istr = is.getByteStream();
            if (istr != null) {
                return istr;
            }
            Reader rdr = is.getCharacterStream();
            if (rdr != null) {
                StringBuffer buffer = new StringBuffer();
                int ch = 0;
                while ((ch = rdr.read())> -1) {
                    buffer.append((char)ch);
                }
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer.toString().getBytes());
                return bais;
            }
        } catch (SAXException e) {
            throw new XMLStreamException(e);
            //e.printStackTrace();
        } catch (IOException e) {
            throw new XMLStreamException(e);
            //e.printStackTrace();
        }
        return null;
    }

}
