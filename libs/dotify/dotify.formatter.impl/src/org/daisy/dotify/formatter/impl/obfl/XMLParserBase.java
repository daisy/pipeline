package org.daisy.dotify.formatter.impl.obfl;

import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

public abstract class XMLParserBase {
	private static final Pattern ws = Pattern.compile("\\s+");

	protected XMLParserBase() {}
	
	public static String normalizeSpace(String input) {
		return ws.matcher(input).replaceAll(" ").trim();
	}
	
	//performance optimization
	public static boolean isSpace(String input) {
		char[] ca = input.toCharArray();
		for (char c : ca) {
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean beginWS(String input) {
		return Character.isWhitespace(input.charAt(0));
	}
	
	public static boolean endWS(String input) {
		return Character.isWhitespace(input.charAt(input.length()-1));
	}
	
	public static boolean equalsElement(XMLEvent event, QName... element) {
		if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
			QName name = event.asStartElement().getName();
			for (QName n : element) {
				if (name.equals(n)) {
					return true;
				}
			}
		} else if (event.getEventType()==XMLStreamConstants.END_ELEMENT) {
			QName name = event.asEndElement().getName();
			for (QName n : element) {
				if (name.equals(n)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean equalsStart(XMLEvent event, QName... element) {
		if (event.getEventType() != XMLStreamConstants.START_ELEMENT) {
			return false;
		} else {
			QName name = event.asStartElement().getName();
			for (QName n : element) {
				if (name.equals(n)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/*
	public static boolean equalsStart(XMLEvent event, QName element) {
		return event.getEventType() == XMLStreamConstants.START_ELEMENT
				&& event.asStartElement().getName().equals(element);
	}*/

	public static boolean equalsEnd(XMLEvent event, QName... element) {
		if (event.getEventType() != XMLStreamConstants.END_ELEMENT) {
			return false;
		} else {
			QName name = event.asEndElement().getName();
			for (QName n : element) {
				if (name.equals(n)) {
					return true;
				}
			}
			return false;
		}
	}
	/*
	public static boolean equalsEnd(XMLEvent event, QName element) {
		return event.getEventType() == XMLStreamConstants.END_ELEMENT
				&& event.asEndElement().getName().equals(element);
	}*/

}
