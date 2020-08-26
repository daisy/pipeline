package org.daisy.pipeline.common.calabash.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.SourceLocator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.OutputValue;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcError;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "px:log-error",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}log-error" }
)
public class LogError implements XProcStepProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogError.class);
	private static final QName C_ERROR = new QName("http://www.w3.org/ns/xproc-step", "error");
	private static final QName PX_LOCATION = new QName("http://www.daisy.org/ns/pipeline/xproc", "location");
	private static final QName PX_FILE = new QName("http://www.daisy.org/ns/pipeline/xproc", "file");
	private static final QName PX_CAUSE = new QName("http://www.daisy.org/ns/pipeline/xproc", "cause");
	private static final QName _CODE = new QName("code");
	private static final QName _HREF = new QName("href");
	private static final QName _LINE = new QName("line");

	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new LogErrorStep(runtime, step);
	}

	public static class LogErrorStep extends Identity implements XProcStep {

		private static final net.sf.saxon.s9api.QName _severity = new net.sf.saxon.s9api.QName("severity");
		private ReadablePipe errorPipe = null;

		private LogErrorStep(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}

		@Override
		public void setInput(String port, ReadablePipe pipe) {
			if ("source".equals(port))
				super.setInput(port, pipe);
			else
				errorPipe = pipe;
		}

		@Override
		public void run() throws SaxonApiException {
			super.run();
			try {
				new ErrorReporter(getOption(_severity, "INFO"))
					.transform(
						ImmutableMap.of(new QName("source"), new XMLCalabashInputValue(errorPipe, runtime)),
						ImmutableMap.of())
					.run();
			} catch (Exception e) {
				logger.error("px:report-error", e);
				throw new XProcException(step.getNode(), e);
			}
		}

		private static class ErrorReporter implements XMLTransformer {

			private final String severity;

			ErrorReporter(String severity) {
				this.severity = severity;
			}

			private void log(String message) {
				if ("TRACE".equals(severity))
					LOGGER.trace(message);
				else if ("DEBUG".equals(severity))
					LOGGER.debug(message);
				else if ("INFO".equals(severity))
					LOGGER.info(message);
				else if ("WARN".equals(severity))
					LOGGER.warn(message);
				else if ("ERROR".equals(severity))
					LOGGER.error(message);
			}

			@Override
			public Runnable transform(Map<QName,InputValue<?>> input, Map<QName,OutputValue<?>> output) {
				QName _source = new QName("source");
				for (QName n : input.keySet())
					if (!n.equals(_source))
						throw new IllegalArgumentException("unexpected value on input port " + n);
				for (QName n : output.keySet())
					throw new IllegalArgumentException("unexpected value on output port " + n);
				InputValue<?> source = input.get(_source);
				if (source != null && !(source instanceof XMLInputValue))
					throw new IllegalArgumentException("input on 'source' port is not XML");
				return () -> report(((XMLInputValue<?>)source).ensureSingleItem().asXMLStreamReader());
			}

			private void report(XMLStreamReader reader) throws TransformerException {
				try {
					while (true)
						try {
							int event = reader.next();
							switch (event) {
							case START_ELEMENT:
								if (C_ERROR.equals(reader.getName())) {
									XProcError xprocError = parseXProcError(reader);
									log(xprocError.getMessage());
									LOGGER.debug(xprocError.toString());
								}
								break;
							default:
							}
						} catch (NoSuchElementException e) {
							break;
						}
				} catch (XMLStreamException e) {
					throw new TransformerException(e);
				}
			}
		}

		private static XProcError parseXProcError(XMLStreamReader reader) throws XMLStreamException {
			if (!C_ERROR.equals(reader.getName()))
				throw new IllegalArgumentException();
			String code = null;
			String message = null;
			List<SourceLocator> location = null;
			XProcError cause = null;
			for (int i = 0; i < reader.getAttributeCount(); i++) {
				QName name = reader.getAttributeName(i);
				String value = reader.getAttributeValue(i);
				if (_CODE.equals(name)) {
					code = value;
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
								cause = parseXProcError(reader);
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
							String _code = code;
							String _message = message;
							SourceLocator[] _location = location == null
								? new SourceLocator[]{}
								: location.toArray(new SourceLocator[location.size()]);
							XProcError _cause = cause;
							return new XProcError() {
								public String getCode() { return _code; }
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

		private static int skipElement(XMLStreamReader reader) throws XMLStreamException {
			int depth = 0;
			while (true)
				try {
					int event = reader.next();
					switch (event) {
					case START_ELEMENT:
						depth++;
						break;
					case END_ELEMENT:
						if (--depth < 0) return event;
						break;
					default:
					}
				} catch (NoSuchElementException e) {
					break;
				}
			throw new RuntimeException("coding error");
		}
	}
}
