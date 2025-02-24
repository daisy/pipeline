package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
				if (event.getEventType() == XMLStreamConstants.COMMENT)
					; // ignore comments
				else if (beginsMixedContent(event)) {
					buffer.addAll(modifyWhitespace(events));
					events.clear();
					parseBlock(event);
				} else if (isEndElement(event, name)) {
					events.add(event);
					buffer.addAll(modifyWhitespace(events));
					break;
				} else
					events.add(event);
			}
		}

		/* Merge text nodes on both sides of a comment
		 */
		private List<XMLEvent> coalesceTextNodes(List<XMLEvent> events) throws XMLStreamException {
			List<XMLEvent> coalesced = new ArrayList<>();
			Characters chars = null; // if previous event (that was not a comment) were characters
			for (int i = 0; i < events.size(); i++) {
				XMLEvent event = events.get(i);
				if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
					if (chars != null)
						chars = createCharacters(chars.getData() + event.asCharacters().getData(),
						                         chars.getLocation());
					else
						chars = event.asCharacters();
				} else {
					if (chars != null) {
						coalesced.add(chars);
						chars = null;
					}
					coalesced.add(event);
				}
			}
			if (chars != null)
				coalesced.add(chars);
			return coalesced;
		}

		// Note that there could in principle be some leading or trailing white space inside a toc-entry
		// that is moved outside of the toc-entry, and because toc-entry are conditional, this could
		// lead to unwanted white space. Because at the moment it is forbidden by the implementation
		// (not by OBFL) that two toc-entry's are contained in the same block, this is not an actual
		// issue.
		private List<XMLEvent> modifyWhitespace(List<XMLEvent> events) throws XMLStreamException {
			// code below assumes no two text nodes follow each other immediately
			events = coalesceTextNodes(events);
			List<XMLEvent> buffer = new ArrayList<>();
			// for changing order between white space and markers, anchors and begin and end tags
			String pendingSpace = null;
			Deque<XMLEvent> pendingTags = new ArrayDeque<>();
			boolean firstContent = true;
			for (int i = 0; i < events.size(); i++) {
				XMLEvent event = events.get(i);
				if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
					String data = event.asCharacters().getData();
					// move any trailing space at the end of an element outside the element, or strip it when
					// at the end of the block
					if (isSpace(data)) {
						// strip space if it does not follow at least one text node that is not white space,
						// or an element that is not a span or marker
						if (!firstContent)
							pendingSpace = " ";
					} else {
						String pre = !firstContent && (pendingSpace != null || startsWithSpace(data))
							? " "
							: "";
						pendingSpace = null;
						if (!pendingTags.isEmpty()) {
							if (!"".equals(pre)) {
								// move any leading space at the start of an element outside the element, and
								// move any leading space in front of marker elements
								buffer.add(createCharacters(pre, event.getLocation()));
								pre = "";
							}
							buffer.addAll(pendingTags);
							pendingTags.clear();
						}
						buffer.add(createCharacters(pre + normalizeSpace(data), event.getLocation()));
						if (endsWithSpace(data))
							pendingSpace = " ";
						firstContent = false;
					}
				} else if (isStartElement(event, ObflQName.SPAN,
				                                 ObflQName.STYLE,
				                                 ObflQName.TOC_ENTRY,
				                                 ObflQName.MARKER,
				                                 ObflQName.ANCHOR)
				           || isEndElement(event, ObflQName.MARKER,
				                                  ObflQName.ANCHOR))
					pendingTags.add(event);
				else {
					if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
						if (pendingSpace != null) {
							buffer.add(createCharacters(pendingSpace, event.getLocation()));
							pendingSpace = null;
						}
					} else if (firstContent
					           && event.getEventType() == XMLStreamConstants.END_ELEMENT
					           && !isEndElement(event, ObflQName.SPAN,
					                                   ObflQName.STYLE,
					                                   ObflQName.TOC_ENTRY))
						firstContent = false;
					if (!pendingTags.isEmpty()) {
						buffer.addAll(pendingTags);
						pendingTags.clear();
					}
					buffer.add(event);
				}
			}
			if (!pendingTags.isEmpty()) {
				buffer.addAll(pendingTags);
				pendingTags.clear();
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
		static final QName ITEM = new QName(OBFL_NS, "item");
		static final QName MARKER = new QName(OBFL_NS, "marker");
		static final QName SPAN = new QName(OBFL_NS, "span");
		static final QName STYLE = new QName(OBFL_NS, "style");
		static final QName TD = new QName(OBFL_NS, "td");
		static final QName TOC_BLOCK = new QName(OBFL_NS, "toc-block");
		static final QName TOC_ENTRY = new QName(OBFL_NS, "toc-entry");
	}
}
