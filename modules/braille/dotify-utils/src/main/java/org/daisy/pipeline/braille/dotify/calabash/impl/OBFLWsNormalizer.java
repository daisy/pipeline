package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.Location;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.daisy.common.transform.TransformerException;

/*
 * This code was adapted from org.daisy.dotify.formatter.impl.obfl.OBFLWsNormalizer.
 */
public class OBFLWsNormalizer {

	public OBFLWsNormalizer(XMLEventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	private final XMLEventFactory eventFactory;

	public Runnable transform(XMLEventReader input, XMLEventConsumer output) {
		Deque<XMLEvent> buffer = new ArrayDeque<>();
		return () -> {
			try (NormalizedOBFL i = new NormalizedOBFL(input)) {
				while (i.hasNext())
					output.add(i.next());
			} catch (XMLStreamException e) {
				throw new TransformerException(e);
			}
		};
	}

	private class NormalizedOBFL implements Iterator<XMLEvent>, AutoCloseable {

		private final XMLEventReader input;
		private final Deque<XMLEvent> buffer;

		NormalizedOBFL(XMLEventReader input) {
			this.input = input;
			buffer = new ArrayDeque<>();
		}

		@Override
		public boolean hasNext() {
			return !buffer.isEmpty() || input.hasNext();
		}

		@Override
		public XMLEvent next() throws NoSuchElementException, TransformerException {
			while (buffer.isEmpty() && input.hasNext())
				try {
					readNextChunk();
				} catch (XMLStreamException e) {
					throw new TransformerException(e);
				}
			if (!buffer.isEmpty())
				return buffer.removeFirst();
			else
				throw new NoSuchElementException();
		}

		@Override
		public void close() {
			buffer.clear();
		}

		private void readNextChunk() throws XMLStreamException {
			XMLEvent event = input.nextEvent();
			if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
				String chars = normalizeSpace(event.asCharacters().getData());
				if (!"".equals(chars))
					buffer.add(createCharacters(chars, event.getLocation()));
			} else if (event.getEventType() == XMLStreamConstants.START_DOCUMENT)
				buffer.add(event);
			else if (beginsMixedContent(event))
				parseBlock(event);
			else
				buffer.add(event);
		}

		/* Group content within block and before and after nested blocks, and call modifyWhiteSpace() on
		 * each group.
		 */
		private void parseBlock(XMLEvent event) throws XMLStreamException {
			QName name = event.asStartElement().getName();
			List<XMLEvent> events = new ArrayList<>();
			events.add(event);
			while (input.hasNext()) {
				event = input.nextEvent();
				if (beginsMixedContent(event)) {
					buffer.addAll(modifyWhitespace(events));
					events.clear();
					parseBlock(event);
				} else if (isEndElement(event, name)) {
					buffer.addAll(modifyWhitespace(events));
					buffer.add(event); // not passing end element to modifyWhitespace() in order to simplify code
					break;
				} else
					events.add(event);
			}
		}

		// Note that there could in principle be some leading or trailing white space inside a toc-entry
		// that is moved outside of the toc-entry, and because toc-entry are conditional, this could
		// lead to unwanted white space. Because at the moment it is forbidden by the implementation
		// (not by OBFL) that two toc-entry's are contained in the same block, this is not an actual
		// issue.
		private List<XMLEvent> modifyWhitespace(List<XMLEvent> events) throws XMLStreamException {
			List<XMLEvent> buffer = new ArrayList<>();
			for (int i = 0; i < events.size(); i++) {
				XMLEvent event = events.get(i);
				if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
					String data = event.asCharacters().getData();
					// strip leading space unless:
					String pre = ""; {
						// ... it is not a white space only text node that is the last node in the block, or
						// that is immediately followed by a marker ...
						if (isSpace(data)
						    && (i == events.size() - 1
						        || isStartElement(events.get(i + 1), ObflQName.MARKER)))
							;
						else if (i > 0) {
							XMLEvent precedingEvent = events.get(i - 1);
							// ... and it immediately follows a page-number, leader, evaluate, span, style,
							// toc-entry, marker or anchor
							if (startsWithSpace(data)
							    && isEndElement(precedingEvent, ObflQName.PAGE_NUMBER,
							                                    ObflQName.LEADER,
							                                    ObflQName.EVALUATE,
							                                    ObflQName.SPAN,
							                                    ObflQName.STYLE,
							                                    ObflQName.TOC_ENTRY,
							                                    ObflQName.MARKER,
							                                    ObflQName.ANCHOR))
								pre = " ";
							// in addition, move over any trailing space that precedes this node, with only
							// markers and anchors in between ...
							else if (i > 1
							         && isEndElement(precedingEvent, ObflQName.MARKER,
							                                         ObflQName.ANCHOR)) {
								XMLEvent previousNotMarkerOrAnchor = tryPrevious(
									backwardSkipWhile(
										events.listIterator(i - 1), ObflQName.MARKER,
										                            ObflQName.ANCHOR)
								).orElse(null);
								if (previousNotMarkerOrAnchor != null
								    && previousNotMarkerOrAnchor.isCharacters()
								    && endsWithSpace(previousNotMarkerOrAnchor.asCharacters().getData()))
									pre = " ";
							// ... as well as any trailing space within the immediately preceding element
							} else if (i > 1
							           && precedingEvent.isEndElement()) {
								XMLEvent previousNotEndElement = tryPrevious(
									backwardSkipWhile(
										events.listIterator(i - 1), XMLStreamConstants.END_ELEMENT)
								).orElse(null);
								if (previousNotEndElement != null
								    && previousNotEndElement.isCharacters()
								    && endsWithSpace(previousNotEndElement.asCharacters().getData()))
									pre = " ";
							}
						}
					}
					// strip trailing space unless:
					String post = ""; {
						// ... it does not occur at the end of the block ...
						if (i < events.size() - 1) {
							XMLEvent followingEvent = events.get(i + 1);
							// ... and it is not a white space only node (because it would potentially also be
							// preserved as leading space) ...
							if (isSpace(data))
								;
							// ... and a page-number, leader, evaluate, span, style or toc-entry follows
							// immediately
							else if (endsWithSpace(data)
							         && isStartElement(followingEvent, ObflQName.PAGE_NUMBER,
							                                           ObflQName.LEADER,
							                                           ObflQName.EVALUATE,
							                                           ObflQName.SPAN,
							                                           ObflQName.STYLE,
							                                           ObflQName.TOC_ENTRY))
								post = " ";
							// in addition, move over any leading space within the immediately following
							// element
							else if (i < events.size() - 2
							         && followingEvent.isStartElement()) {
								XMLEvent nextNotStartElement = tryNext(
									skipWhile(
										events.listIterator(i + 2), XMLStreamConstants.START_ELEMENT)
								).orElse(null);
								if (nextNotStartElement != null
								    && nextNotStartElement.isCharacters()
								    && startsWithSpace(nextNotStartElement.asCharacters().getData()))
									post = " ";
							}
						}
					}
					String chars = pre + normalizeSpace(data) + post;
					if (!"".equals(chars))
						buffer.add(createCharacters(chars, event.getLocation()));
				// if this is a span, style or toc-entry
				} else if (isStartElement(event, ObflQName.SPAN,
				                                 ObflQName.STYLE,
				                                 ObflQName.TOC_ENTRY)) {
					if (i > 1) {
						XMLEvent precedingEvent = events.get(i - 1);
						// insert any trailing space that precedes this element, with only markers and anchors
						// in between, before this element ...
						if (isEndElement(precedingEvent, ObflQName.MARKER,
							                             ObflQName.ANCHOR)) {
							XMLEvent previousNotMarkerOrAnchor = tryPrevious(
								backwardSkipWhile(
									events.listIterator(i - 1), ObflQName.MARKER,
									                            ObflQName.ANCHOR)
							).orElse(null);
							if (previousNotMarkerOrAnchor != null
							    && previousNotMarkerOrAnchor.isCharacters()
							    && endsWithSpace(previousNotMarkerOrAnchor.asCharacters().getData()))
								buffer.add(createCharacters(" ", event.getLocation()));
						// ... as well as any trailing space within the immediately preceding element
						} else if (precedingEvent.isEndElement()) {
							XMLEvent previousNotEndElement = tryPrevious(
								backwardSkipWhile(
									events.listIterator(i - 1), XMLStreamConstants.END_ELEMENT)
							).orElse(null);
							if (previousNotEndElement != null
							    && previousNotEndElement.isCharacters()
							    && endsWithSpace(previousNotEndElement.asCharacters().getData()))
								buffer.add(createCharacters(" ", event.getLocation()));
						}
					}
					buffer.add(event);
				} else if (isEndElement(event, ObflQName.SPAN,
				                               ObflQName.STYLE,
				                               ObflQName.TOC_ENTRY)) {
					buffer.add(event);
					if (i < events.size() - 2) {
						XMLEvent followingEvent = events.get(i + 1);
						// move over any leading space within the immediately following element after this
						// span, style or toc-entry
						if (followingEvent.isStartElement()) {
							XMLEvent nextNotStartElement = tryNext(
								skipWhile(
									events.listIterator(i + 2), XMLStreamConstants.START_ELEMENT)
							).orElse(null);
							if (nextNotStartElement != null
							    && nextNotStartElement.isCharacters()
							    && startsWithSpace(nextNotStartElement.asCharacters().getData()))
								buffer.add(createCharacters(" ", event.getLocation()));
						}
					}
				} else
					buffer.add(event);
			}
			return buffer;
		}
	}

	private Characters createCharacters(String chars, Location location) throws XMLStreamException {
		eventFactory.setLocation(location);
		try {
			return eventFactory.createCharacters(chars);
		} finally {
			eventFactory.setLocation(null);
		}
	}

	private static boolean isSpace(String input) {
		for (char c : input.toCharArray())
			if (!Character.isWhitespace(c))
				return false;
		return true;
	}

	private static boolean startsWithSpace(String input) {
		return Character.isWhitespace(input.charAt(0));
	}

	private static boolean endsWithSpace(String input) {
		return Character.isWhitespace(input.charAt(input.length() - 1));
	}

	private static final Pattern WS = Pattern.compile("\\s+");

	private static String normalizeSpace(String input) {
		return WS.matcher(input).replaceAll(" ").trim();
	}

	private static boolean isStartElement(XMLEvent event, QName... element) {
		if (event.getEventType() != XMLStreamConstants.START_ELEMENT)
			return false;
		else {
			QName name = event.asStartElement().getName();
			for (QName n : element) {
				if (name.equals(n)) {
					return true;
				}
			}
			return false;
		}
	}

	private static boolean isEndElement(XMLEvent event, QName... element) {
		if (event.getEventType() != XMLStreamConstants.END_ELEMENT)
			return false;
		else {
			QName name = event.asEndElement().getName();
			for (QName n : element) {
				if (name.equals(n)) {
					return true;
				}
			}
			return false;
		}
	}

	private static ListIterator<XMLEvent> skipWhile(ListIterator<XMLEvent> events, int eventType) {
		while (events.hasNext())
			if (events.next().getEventType() != eventType) {
				events.previous();
				break;
			}
		return events;
	}

	private static ListIterator<XMLEvent> backwardSkipWhile(ListIterator<XMLEvent> events, int eventType) {
		while (events.hasPrevious())
			if (events.previous().getEventType() != eventType) {
				events.next();
				break;
			}
		return events;
	}

	private static ListIterator<XMLEvent> backwardSkipWhile(ListIterator<XMLEvent> events, QName... name) {
		while (events.hasPrevious()) {
			XMLEvent e = events.previous();
			if (isStartElement(e, name) || isEndElement(e, name))
				continue;
			else {
				events.next();
				break;
			}
		}
		return events;
	}

	private static <T> Optional<T> tryNext(Iterator<T> list) {
		if (list.hasNext())
			return Optional.of(list.next());
		return Optional.empty();
	}

	private static <T> Optional<T> tryPrevious(ListIterator<T> list) {
		if (list.hasPrevious())
			return Optional.of(list.previous());
		return Optional.empty();
	}

	private static boolean beginsMixedContent(XMLEvent event) {
		return isStartElement(event, ObflQName.BLOCK,
		                             ObflQName.TOC_BLOCK,
		                             ObflQName.ITEM,
		                             ObflQName.BEFORE,
		                             ObflQName.AFTER,
		                             ObflQName.TD
		);
	}

	private static class ObflQName {
		private ObflQName() {}

		static final String OBFL_NS = "http://www.daisy.org/ns/2011/obfl";

		static final QName AFTER = new QName(OBFL_NS, "after");
		static final QName ANCHOR = new QName(OBFL_NS, "anchor");
		static final QName BEFORE = new QName(OBFL_NS, "before");
		static final QName BLOCK = new QName(OBFL_NS, "block");
		static final QName EVALUATE = new QName(OBFL_NS, "evaluate");
		static final QName ITEM = new QName(OBFL_NS, "item");
		static final QName LEADER = new QName(OBFL_NS, "leader");
		static final QName MARKER = new QName(OBFL_NS, "marker");
		static final QName PAGE_NUMBER = new QName(OBFL_NS, "page-number");
		static final QName SPAN = new QName(OBFL_NS, "span");
		static final QName STYLE = new QName(OBFL_NS, "style");
		static final QName TD = new QName(OBFL_NS, "td");
		static final QName TOC_BLOCK = new QName(OBFL_NS, "toc-block");
		static final QName TOC_ENTRY = new QName(OBFL_NS, "toc-entry");
	}
}
