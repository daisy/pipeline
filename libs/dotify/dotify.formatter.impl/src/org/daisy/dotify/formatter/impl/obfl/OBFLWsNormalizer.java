package org.daisy.dotify.formatter.impl.obfl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.XMLEvent;

/**
 * Provides a whitespace normalizer for OBFL-files.
 *
 * @author Joel Håkansson
 */
public class OBFLWsNormalizer extends XMLParserBase implements XMLEventIterator {
    private static final Logger logger = Logger.getLogger(OBFLWsNormalizer.class.getCanonicalName());
    private final XMLEventReader input;
    private final XMLEventFactory eventFactory;
    private boolean writingOften;

    /**
     * Creates a new OBFLWsNormalizer.
     *
     * @param input        the input XMLEventReader. Note that the underlying stream might not be closed after parsing,
     *                     due to limitations in the StaX implementation.
     * @param eventFactory the xml event factory
     */
    public OBFLWsNormalizer(XMLEventReader input, XMLEventFactory eventFactory) {
        this.input = input;
        this.eventFactory = eventFactory;
        this.writingOften = false;
    }

    Deque<XMLEvent> buffer = new ArrayDeque<>();

    @Override
    public boolean hasNext() {
        return !buffer.isEmpty() || input.hasNext();
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        while (buffer.isEmpty() && input.hasNext()) {
            readNextChunk();
        }
        if (!buffer.isEmpty()) {
            return buffer.removeFirst();
        } else {
            throw new XMLStreamException();
        }
    }

    private void readNextChunk() {
        XMLEvent event;
        try {
            event = input.nextEvent();
        } catch (XMLStreamException e) {
            // if something goes wrong when reading the next input, it is possible
            // to get stuck in an endless loop if we keep calling nextEvent.
            throw new RuntimeException(e);
        }
        try {
            if (event.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                buffer.add(event);
            } else if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
                String chars = normalizeSpace(event.asCharacters().getData());
                if (!"".equals(chars)) {
                    eventFactory.setLocation(event.getLocation());
                    Characters c = eventFactory.createCharacters(chars);
                    eventFactory.setLocation(null);
                    buffer.add(c);
                }
            } else if (beginsMixedContent(event)) {
                parseBlock(event);
            } else {
                buffer.add(event);
            }
        } catch (XMLStreamException e) {
            logger.log(Level.WARNING, "Parsing failed.", e);
        }
    }

    @Override
    public void close() throws XMLStreamException {
        buffer.clear();
    }

    /**
     * Parses for whitespace.
     *
     * @param outputFactory an xml output factory
     * @param out           the output stream
     */
    //TODO: this argument doesn't make sense from a user's perspective
    public void parse(XMLOutputFactory outputFactory, OutputStream out) {
        XMLEventWriter writer = null;
        try {
            while (hasNext()) {
                // write result
                XMLEvent event = nextEvent();
                if (event.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                    StartDocument sd = (StartDocument) event;
                    if (sd.encodingSet()) {
                        writer = outputFactory.createXMLEventWriter(out, sd.getCharacterEncodingScheme());
                        writer.add(event);
                    } else {
                        writer = outputFactory.createXMLEventWriter(out, "utf-8");
                        writer.add(eventFactory.createStartDocument("utf-8", "1.0"));
                    }
                } else {
                    writer.add(event);
                }
                if (writingOften) {
                    writer.flush();
                }
            }
            input.close();
        } catch (XMLStreamException e) {
            logger.log(Level.WARNING, "Exception while parsing.", e);
        }

        //close outer one first
        try {
            writer.close();
        } catch (XMLStreamException e) {
            logger.log(Level.WARNING, "Failed to close writer.", e);
        }

        //should be closed automatically, but it isn't
        try {
            out.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to close output stream.", e);
        }
    }

    /**
     * Returns true if the output stream is updated often.
     *
     * @return true if the output stream is updated often, false otherwise
     */
    public boolean isWritingOften() {
        return writingOften;
    }

    /**
     * Sets an intention to write to the output stream often, if true.
     * Since this can affect performance, it is configurable.
     *
     * @param writingOften pass true to write often, false otherwise
     */
    public void setWritingOften(boolean writingOften) {
        this.writingOften = writingOften;
    }

    private void parseBlock(XMLEvent event) throws XMLStreamException {
        QName end = event.asStartElement().getName();
        List<XMLEvent> events = new ArrayList<>();
        events.add(event);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (beginsMixedContent(event)) {
                buffer.addAll(modifyWhitespace(events));
                events.clear();
                parseBlock(event);
            } else if (equalsEnd(event, end)) {
                events.add(event);
                buffer.addAll(modifyWhitespace(events));
                break;
            } else {
                events.add(event);
            }
        }
    }

    // Note that there could in principle be some leading or trailing white space inside a toc-entry
    // that is moved outside of the toc-entry, and because toc-entry are conditional, this could
    // lead to unwanted white space. Because at the moment it is forbidden by the implementation
    // (not by OBFL) that two toc-entry's are contained in the same block, this is not an actual
    // issue.
    private List<XMLEvent> modifyWhitespace(List<XMLEvent> events) {
        List<XMLEvent> modified = new ArrayList<>();
        // process
        for (int i = 0; i < events.size(); i++) {
            XMLEvent event = events.get(i);
            eventFactory.setLocation(event.getLocation());

            if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
                final String data = event.asCharacters().getData();

                String pre = "";
                String post = "";
                boolean beginWSMatch = beginWS(data);

                if (
                    isSpace(data) &&
                    ((i == events.size() - 2 &&
                    endsMixedContent(events.get(i + 1))) || i == events.size() - 1)
                ) {
                    // this is the last element in the block, ignore
                } else if (i > 0) {
                    XMLEvent preceedingEvent = events.get(i - 1);
                    if (
                        preceedingEvent.isEndElement() &&
                        beginWSMatch &&
                        isPreserveElement(preceedingEvent.asEndElement().getName())
                    ) {
                        pre = " ";
                    } else if (
                        equalsEnd(
                            preceedingEvent,
                            ObflQName.SPAN,
                            ObflQName.STYLE,
                            ObflQName.TOC_ENTRY
                        ) &&
                        beginWSMatch
                    ) {
                        pre = " ";
                    } else if (equalsEnd(preceedingEvent, ObflQName.MARKER, ObflQName.ANCHOR)) {
                        if (beginWSMatch) {
                            pre = " ";
                        } else {
                            int j = untilEventIsNotBackward(events, i - 1, ObflQName.MARKER, ObflQName.ANCHOR);
                            if (j > -1) {
                                XMLEvent upstream = events.get(j);
                                if (upstream.isCharacters() && endWS(upstream.asCharacters().getData())) {
                                    pre = " ";
                                }
                            }
                        }
                    } else if (preceedingEvent.isEndElement()) {
                        int j = untilEventIsNotBackward(events, i - 1, XMLStreamConstants.END_ELEMENT);
                        if (j > -1) {
                            XMLEvent upstream = events.get(j);
                            if (upstream.isCharacters() && endWS(upstream.asCharacters().getData())) {
                                pre = " ";
                            }
                        }
                    }

                }
                if (i < events.size() - 1) {
                    XMLEvent followingEvent = events.get(i + 1);
                    boolean endWSMatch = endWS(data);
                    if (isSpace(data)) {
                        // don't output post
                        if (equalsStart(followingEvent, ObflQName.MARKER)) {
                            pre = "";
                        }
                    } else if (
                        followingEvent.isStartElement() &&
                        endWSMatch &&
                        isPreserveElement(followingEvent.asStartElement().getName())
                    ) {
                        post = " ";
                    } else if (
                        equalsStart(
                            followingEvent,
                            ObflQName.SPAN,
                            ObflQName.STYLE,
                            ObflQName.TOC_ENTRY
                        ) &&
                        endWSMatch
                    ) {
                        post = " ";
                    } else if (followingEvent.isStartElement()) {
                        int j = untilEventIsNotForward(events, i + 1, XMLStreamConstants.START_ELEMENT);
                        if (j > -1) {
                            XMLEvent downstream = events.get(j);
                            if (downstream.isCharacters() && beginWS(downstream.asCharacters().getData())) {
                                post = " ";
                            }
                        }
                    }
                }

                String chars = pre + normalizeSpace(data) + post;
                if (!"".equals(chars)) {
                    modified.add(eventFactory.createCharacters(chars));
                }
            } else if (equalsStart(event, ObflQName.SPAN, ObflQName.STYLE, ObflQName.TOC_ENTRY)) {

                if (i > 0) {
                    int j = untilEventIsNotBackward(events, i - 1, ObflQName.MARKER, ObflQName.ANCHOR);
                    if (!(j > -1 && j < i - 1)) {
                        j = untilEventIsNotBackward(events, i - 1, XMLStreamConstants.END_ELEMENT);
                    }
                    if (j > -1 && j < i - 1) {
                        XMLEvent upstream = events.get(j);
                        if (upstream.isCharacters() && endWS(upstream.asCharacters().getData())) {
                            modified.add(eventFactory.createCharacters(" "));
                        }
                    }
                }

                modified.add(event);

            } else if (equalsEnd(event, ObflQName.SPAN, ObflQName.STYLE, ObflQName.TOC_ENTRY)) {
                modified.add(event);
                if (i < events.size() - 1) {
                    int j = untilEventIsNotForward(events, i + 1, XMLStreamConstants.START_ELEMENT);
                    if (j > -1 && j > i + 1) {
                        XMLEvent downstream = events.get(j);
                        if (downstream.isCharacters() && beginWS(downstream.asCharacters().getData())) {
                            modified.add(eventFactory.createCharacters(" "));
                        }
                    }
                }
            } else {
                modified.add(event);
            }
        }
        eventFactory.setLocation(null);
        return modified;
    }

    private boolean isPreserveElement(QName name) {
        return name.equals(ObflQName.PAGE_NUMBER) || name.equals(ObflQName.LEADER) || name.equals(ObflQName.EVALUATE);
    }

    private static int untilEventIsNotForward(List<XMLEvent> events, final int i, final int eventType) {
        for (int j = i; j < events.size(); j++) {
            if (events.get(j).getEventType() != eventType) {
                return j;
            }
        }
        return -1;
    }

    private static int untilEventIsNotBackward(List<XMLEvent> events, final int i, final int eventType) {
        for (int j = 0; j < i; j++) {
            if (events.get(i - j).getEventType() != eventType) {
                return i - j;
            }
        }
        return -1;
    }

    private static int untilEventIsNotBackward(List<XMLEvent> events, final int i, QName... name) {
        for (int j = 0; j < i; j++) {
            XMLEvent event = events.get(i - j);
            if (XMLParserBase.equalsElement(event, name)) {
                // continue
            } else {
                return i - j;
            }
        }
        return -1;
    }

    private boolean beginsMixedContent(XMLEvent event) {
        return equalsStart(
            event,
            ObflQName.BLOCK,
            ObflQName.TOC_BLOCK,
            ObflQName.ITEM,
            ObflQName.BEFORE,
            ObflQName.AFTER,
            ObflQName.TD
        );

    }

    private boolean endsMixedContent(XMLEvent event) {
        return equalsEnd(
            event,
            ObflQName.BLOCK,
            ObflQName.TOC_BLOCK,
            ObflQName.ITEM,
            ObflQName.BEFORE,
            ObflQName.AFTER,
            ObflQName.TD
        );
    }

    /**
     * @param args the arguments
     */
    public static void main(String[] args) {
        try {
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            OBFLWsNormalizer p = new OBFLWsNormalizer(
                inFactory.createXMLEventReader(
                    new FileInputStream("ws-test-input.xml")
                ),
                XMLEventFactory.newInstance()
            );
            p.parse(XMLOutputFactory.newInstance(), new FileOutputStream("out.xml"));
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Failed to open stream.", e);
        } catch (XMLStreamException e) {
            logger.log(Level.WARNING, "Failed to create event reader.", e);
        }
    }


}
