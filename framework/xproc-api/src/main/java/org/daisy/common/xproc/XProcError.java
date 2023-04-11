package org.daisy.common.xproc;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.SourceLocator;

import static org.daisy.common.stax.XMLStreamWriterHelper.skipElement;

public abstract class XProcError {
	
	public abstract QName getCode();
	
	public abstract String getMessage();
	
	public abstract XProcError getCause();
	
	public abstract SourceLocator[] getLocation();
	
	// adapted from java.lang.Throwable
	private String printEnclosedLocation(SourceLocator[] enclosingLocation) {
		StringBuilder s = new StringBuilder();
		if (getCode() != null) {
			s.append("[").append(getCode()).append("]");
			if (getMessage() != null)
				s.append(" ").append(getMessage());
		} else
			s.append(getMessage());
		SourceLocator[] loc = getLocation();
		int m = loc.length - 1;
		int n = enclosingLocation.length - 1;
		while (m >= 0 && n >=0 && loc[m].equals(enclosingLocation[n])) {
			m--;
			n--;
		}
		int inCommon = loc.length - 1 - m;
		for (int i = 0; i <= m; i++)
			s.append("\n\tat " + loc[i]);
		if (inCommon != 0)
			s.append("\n\t... " + inCommon + " more"); // in Logback: "... X common frames omitted"
		XProcError cause = getCause();
		if (cause != null) {
			s.append("\nCaused by: ");
			s.append(cause.printEnclosedLocation(loc));
		}
		return s.toString();
	}
	
	@Override
	public String toString() {
		return printEnclosedLocation(new SourceLocator[]{});
	}

	public static XProcError parse(XMLStreamReader reader) throws XMLStreamException {
		if (reader.getEventType() == START_DOCUMENT)
			reader.next();
		if (reader.getEventType() != START_ELEMENT)
			throw new IllegalArgumentException();
		if (!C_ERROR.equals(reader.getName()))
			throw new IllegalArgumentException();
		QName code = null;
		String message = null;
		List<SourceLocator> location = null;
		XProcError cause = null;
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			QName name = reader.getAttributeName(i);
			String value = reader.getAttributeValue(i);
			if (_CODE.equals(name)) {
				if (value.contains(":")) {
					String prefix = value.substring(0, value.indexOf(":"));
					String namespace = reader.getNamespaceContext().getNamespaceURI(prefix);
					String localPart = value.substring(prefix.length() + 1, value.length());
					code = new QName(namespace, localPart, prefix);
				} else
					code = new QName(value);
				break;
			}
		}
		Stack<QName> parents = new Stack<>();
		int event = reader.next();
		while (true)
			try {
				switch (event) {
				case START_ELEMENT:
					if (C_ERROR.equals(reader.getName())) {
						if (parents.size() == 1 && PX_CAUSE.equals(parents.peek()))
							cause = parse(reader);
						else
							skipElement(reader);
					} else if (PX_FILE.equals(reader.getName())) {
						if (parents.size() == 1 && PX_LOCATION.equals(parents.peek())) {
							if (location == null) location = new ArrayList<>();
							location.add(new PxFileLocation(reader));
						} else
							skipElement(reader);
					} else
						parents.add(reader.getName());
					break;
				case END_ELEMENT:
					if (C_ERROR.equals(reader.getName())) {
						QName _code = code;
						String _message = message;
						SourceLocator[] _location = location == null
							? new SourceLocator[]{}
							: location.toArray(new SourceLocator[location.size()]);
						XProcError _cause = cause;
						return new XProcError() {
							public QName getCode() { return _code; }
							public String getMessage() { return _message; }
							public XProcError getCause() { return _cause; }
							public SourceLocator[] getLocation() { return _location; }
						};
					}
					parents.pop();
					break;
				case CHARACTERS:
					message = message == null ? reader.getText() : (message + reader.getText());
					break;
				default:
				}
				event = reader.next();
			} catch (NoSuchElementException e) {
				break;
			}
		throw new RuntimeException("coding error");
	}

	private static final QName C_ERROR = new QName("http://www.w3.org/ns/xproc-step", "error");
	private static final QName PX_LOCATION = new QName("http://www.daisy.org/ns/pipeline/xproc", "location");
	private static final QName PX_FILE = new QName("http://www.daisy.org/ns/pipeline/xproc", "file");
	private static final QName PX_CAUSE = new QName("http://www.daisy.org/ns/pipeline/xproc", "cause");
	private static final QName _CODE = new QName("code");
	private static final QName _HREF = new QName("href");
	private static final QName _LINE = new QName("line");

	private static class PxFileLocation implements SourceLocator {
		private String href = "";
		private int line = -1;
		PxFileLocation(XMLStreamReader reader) throws XMLStreamException {
			if (!PX_FILE.equals(reader.getName()))
				throw new IllegalArgumentException();
			for (int i = 0; i < reader.getAttributeCount(); i++) {
				QName name = reader.getAttributeName(i);
				String value = reader.getAttributeValue(i);
				if (_HREF.equals(name))
					href = value;
				else if (_LINE.equals(name))
					line = Integer.parseInt(value);
			}
			skipElement(reader);
		}
		public String getPublicId() { return null; }
		public String getSystemId() { return href; }
		public int getLineNumber() { return line; }
		public int getColumnNumber() { return -1; }
		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			String fileName = getSystemId();
			if (fileName != null && !"".equals(fileName)) {
				if (fileName.lastIndexOf('/') >= 0)
					fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
				s.append(fileName);
				int line = getLineNumber();
				if (line > 0)
					s.append(":" + getLineNumber());
			}
			return s.toString();
		}
	}
}
