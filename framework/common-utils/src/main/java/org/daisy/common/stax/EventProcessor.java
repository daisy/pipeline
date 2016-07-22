package org.daisy.common.stax;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;


/**
 * Stax event processor.
 */
public interface EventProcessor {

	/**
	 * Process the event
	 *
	 * @param event the event
	 * @throws XMLStreamException the xML stream exception
	 */
	void process(XMLEvent event) throws XMLStreamException;
}
