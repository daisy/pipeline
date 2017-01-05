package org.daisy.dotify.impl.input;

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

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Creates an XML filter using StAX.
 * For each XMLEvent, an appropriate function is called. The functions
 * return an XMLEvent that is written to the output. Subclasses of the
 * StaxFilter override the functions for the events they are interested
 * in. By default, all events are copied as-is to the output.
 * 
 * JH090901: Modified StaxFilter to support sub tree addition.
 * @author Linus Ericson, Joel Håkansson
 */
public abstract class StaxFilter2 {

    private XMLEventReader reader = null;
    private XMLEventFactory eventFactory = null;
    private XMLOutputFactory outputFactory = null;
    private OutputStream outputStream = null;
    private XMLEventWriter writer;

	@SuppressWarnings("javadoc")
	public StaxFilter2(XMLEventReader xer, XMLEventFactory xef, XMLOutputFactory xof, OutputStream outStream) {
        reader = xer;
        eventFactory = xef;
        outputFactory = xof;
        outputStream = outStream;
        writer = null;
    }
    
	@SuppressWarnings("javadoc")
    public StaxFilter2(XMLEventReader xer, XMLEventFactory xef, OutputStream outStream) {
        this(xer, xef, XMLOutputFactory.newInstance(), outStream);
    }
    
	@SuppressWarnings("javadoc")
    public StaxFilter2(XMLEventReader xer, XMLOutputFactory xof, OutputStream outStream) {
        this(xer, XMLEventFactory.newInstance(), xof, outStream);
    }
    
	@SuppressWarnings("javadoc")
    public StaxFilter2(XMLEventReader xer, OutputStream outStream) {
        this(xer, XMLEventFactory.newInstance(), XMLOutputFactory.newInstance(), outStream);
    }
    
	@SuppressWarnings("javadoc")
    public void close() throws IOException {
    	outputStream.close();
    }
    
	@SuppressWarnings("javadoc")
    public final void filter() throws XMLStreamException {
        boolean rootElementSeen = false;
        boolean textSeen = false;
        int level = 0;
        int skip = 0;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            XMLEvent writeEvent = null;
            switch (event.getEventType()) {
            case XMLStreamConstants.ATTRIBUTE:
                writeEvent = this.attribute((Attribute)event);
                break;
            case XMLStreamConstants.CDATA:
                writeEvent = this.cdata((Characters)event);
            	textSeen = true;
                break;
            case XMLStreamConstants.CHARACTERS:
                writeEvent = this.characters((Characters)event);
            	textSeen = true;
            	break;
            case XMLStreamConstants.COMMENT:
                writeEvent = this.comment((Comment)event);
            	break;
            case XMLStreamConstants.DTD:
                writeEvent = this.dtd((DTD)event);
            	break;
            case XMLStreamConstants.END_DOCUMENT:
                writeEvent = this.endDocument((EndDocument)event);
            	break;
            case XMLStreamConstants.END_ELEMENT:
                level--;
                writeEvent = this.endElement((EndElement)event);
            	break;
            case XMLStreamConstants.ENTITY_DECLARATION:
                writeEvent = this.entityDeclaration((EntityDeclaration)event);
            	break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                writeEvent = this.entityReference((EntityReference)event);
            	textSeen = true;
            	break;
            case XMLStreamConstants.NAMESPACE:
                writeEvent = this.namespace((Namespace)event);
            	break;
            case XMLStreamConstants.NOTATION_DECLARATION:
                writeEvent = this.notationDeclaration((NotationDeclaration)event);
            	break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                writeEvent = this.processingInstruction((ProcessingInstruction)event);
            	break;
            case XMLStreamConstants.SPACE:
                writeEvent = this.space((Characters)event);
            	textSeen = true;
            	break;
            case XMLStreamConstants.START_DOCUMENT:
                StartDocument sd = (StartDocument)event;
            	if (sd.encodingSet()) {
            	    writer = outputFactory.createXMLEventWriter(outputStream, sd.getCharacterEncodingScheme());
            	    writeEvent = event;
            	} else {
            	    writer = outputFactory.createXMLEventWriter(outputStream, "utf-8");
            	    writeEvent = eventFactory.createStartDocument("utf-8", "1.0");
            	}
                this.startDocument((StartDocument)event);
            	break;
            case XMLStreamConstants.START_ELEMENT:                
                if (!rootElementSeen && !textSeen) {
                    writer.add(this.getEventFactory().createCharacters("\n"));
                    rootElementSeen = true;
                }
                writeEvent = this.startElement((StartElement)event);
                level++;
                if (writeEvent == null) {
                    skip = level;
                }                
            	break;
            }
            if (skip > 0) {
                while (reader.hasNext() && skip != 0) {
                    event = reader.nextEvent();
                    if (event.isStartElement()) {
                        level++;
                    } else if (event.isEndElement()) {
                        level--;
                    }
                    if (level < skip) {
                        skip = 0;
                    }
                }
            } else if (writeEvent != null) {
                writer.add(writeEvent);
            }
        }
        writer.close();
        reader.close();
    }
    
    protected XMLEventFactory getEventFactory() {
        return eventFactory;
    }
    
    protected XMLEventWriter getEventWriter() {
    	return writer;
    }
    
    protected Attribute attribute(Attribute event) {
        return event;
    }
    
    protected Characters cdata(Characters event) {
        return event;
    }
    
    protected Characters characters(Characters event) {
        return event;
    }
    
    protected Comment comment(Comment event) {
        return event;
    }
    
    protected DTD dtd(DTD event) {
        return event;
    }
    
    protected EndDocument endDocument(EndDocument event) {
        return event;
    }
    
    protected EndElement endElement(EndElement event) {
        return event;
    }
    
    protected EntityDeclaration entityDeclaration(EntityDeclaration event) {
        return event;
    }
    
    protected EntityReference entityReference(EntityReference event) {
        return event;
    }
    
    protected Namespace namespace(Namespace event) {
        return event;
    }
    
    protected NotationDeclaration notationDeclaration(NotationDeclaration event) {
        return event;
    }
    
    protected ProcessingInstruction processingInstruction(ProcessingInstruction event) {
        return event;
    }
    
    protected Characters space(Characters event) {
        return event;
    }
    
    protected void startDocument(StartDocument event) {
        // Nothing to see here. Move along.
    }
    
    /**
     * Callback function for start elements.
     * Returning <code>null</code> from this subroutine will cause the entire
     * subtree to be skipped.
     * @param event an input start element
     * @return an output start element
     */
    protected StartElement startElement(StartElement event) {
        return event;
    }    
    
}
