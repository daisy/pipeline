package org.daisy.common.stax;

import java.util.function.Predicate;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * The Class StaxEventHelper offers some stax utilities.
 */
public final class StaxEventHelper {

	/**
	 * EventPredicates related helper functions.
	 */
	public static class EventPredicates {

		/**
		 * Checks if is element
		 *
		 * @param name the name
		 * @return the predicate
		 */
		public static Predicate<XMLEvent> isElement(final QName name) {
			return new Predicate<XMLEvent>() {
				@Override
				public boolean test(XMLEvent event) {
					return event.isStartElement()
							&& event.asStartElement().getName().equals(name);
				}
			};
		}

		/**
		 * Checks if is start or stop element.
		 *
		 * @param name the name
		 * @return the predicate
		 */
		public static Predicate<XMLEvent> isStartOrStopElement(final QName name) {
			return new Predicate<XMLEvent>() {
				@Override
				public boolean test(XMLEvent event) {
					return (event.isStartElement() && event.asStartElement()
							.getName().equals(name))
							|| (event.isEndElement() && event.asEndElement()
									.getName().equals(name));
				}
			};
		}

		/** The I s_ star t_ element. */
		public static Predicate<XMLEvent> IS_START_ELEMENT = new Predicate<XMLEvent>() {
			@Override
			public boolean test(XMLEvent event) {
				return event.isStartElement();
			}
		};

		/** The I s_ en d_ element. */
		public static Predicate<XMLEvent> IS_END_ELEMENT = new Predicate<XMLEvent>() {
			@Override
			public boolean test(XMLEvent event) {
				return event.isEndElement();
			}
		};

		/**
		 * Checks if the event is child or siblings ChildOrSiblingPredicate.
		 */
		public static class ChildOrSiblingPredicate implements
				Predicate<XMLEvent> {

			/** The opened. */
			private int opened = 1;

			@Override
			public boolean test(XMLEvent event) {
				switch (event.getEventType()) {
				case XMLEvent.START_ELEMENT:
					opened++;
					break;
				case XMLEvent.END_ELEMENT:
					opened--;
					break;
				default:
					break;
				}
				return opened > 0;
			}
		};

		/**
		 * Gets the child or sibling predicate.
		 *
		 * @return the child or sibling predicate
		 */
		public static Predicate<XMLEvent> getChildOrSiblingPredicate() {
			return new ChildOrSiblingPredicate();
		}

		/**
		 * Checks if the event is offspring.
		 */
		public static class ChildPredicate implements Predicate<XMLEvent> {

			/** The opened. */
			private int opened = 0;

			@Override
			public boolean test(XMLEvent event) {

				switch (event.getEventType()) {
				case XMLEvent.START_ELEMENT:
					opened++;
					break;
				case XMLEvent.END_ELEMENT:
					opened--;
					break;
				default:
					break;
				}

				return opened > 0;
			}

		}

		/**
		 * Checks if is child predicate.
		 *
		 * @return the predicate
		 */
		public static Predicate<XMLEvent> isChildPredicate() {
			return new ChildPredicate();
		}

	}

	/**
	 * Peek next element matching the QName
	 *
	 * @param reader the reader
	 * @param name the name
	 * @return the start element
	 * @throws XMLStreamException the xML stream exception
	 */
	public static StartElement peekNextElement(XMLEventReader reader, QName name)
			throws XMLStreamException {
		while (reader.hasNext()) {
			XMLEvent event = reader.peek();
			if (event.isStartElement()
					&& event.asStartElement().getName().equals(name)) {
				return event.asStartElement();
			}
			reader.next();
		}
		throw new IllegalStateException("Element " + name + " not found");
	}

	/**
	 * Peek next element which mathces any QNames from the set provided
	 *
	 * @param reader the reader
	 * @param names the names
	 * @return the start element
	 * @throws XMLStreamException the xML stream exception
	 */
	public static StartElement peekNextElement(XMLEventReader reader,
			Set<QName> names) throws XMLStreamException {

		while (reader.hasNext()) {
			XMLEvent event = reader.peek();
			if (event.isStartElement()
					&& names.contains(event.asStartElement().getName())) {
				return event.asStartElement();
			}
			reader.next();
		}
		throw new IllegalStateException("Element  not found");
	}

	/**
	 * Loops through the elements until the checker returns null or the element stream stops
	 *
	 * @param reader the reader
	 * @param filter the filter says whether the element has to be processed or not.
	 * @param checker the checker
	 * @param processor the processor
	 * @throws XMLStreamException the xML stream exception
	 */
	public static synchronized void loop(XMLEventReader reader,
			Predicate<XMLEvent> filter, Predicate<XMLEvent> checker,
			EventProcessor processor) throws XMLStreamException {
		while (reader.hasNext()) {
			XMLEvent event = reader.peek();
			if (filter.test(event)) {
				if (!checker.test(event)) {
					break;
				}
				processor.process(event);
			}
			if (reader.hasNext()) {
				reader.next();
			}
		}
	}

	/**
	 * Instantiates a new stax event helper.
	 */
	private StaxEventHelper() {
		// no instantiation
	}

}
