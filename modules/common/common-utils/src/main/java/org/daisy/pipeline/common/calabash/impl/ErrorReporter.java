package org.daisy.pipeline.common.calabash.impl;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.google.common.collect.ImmutableMap;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.OutputValue;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.common.xproc.XProcError;

public class ErrorReporter implements XMLTransformer {

	private final static QName _SOURCE = new QName("source");
	private static final QName C_ERROR = new QName("http://www.w3.org/ns/xproc-step", "error");

	private final Consumer<XProcError> reporter;

	ErrorReporter(Consumer<XProcError> reporter) {
		this.reporter = reporter;
	}

	@Override
	public Runnable transform(Map<QName,InputValue<?>> input, Map<QName,OutputValue<?>> output) {
		input = XMLTransformer.validateInput(input, ImmutableMap.of(_SOURCE, InputType.MANDATORY_NODE_SINGLE));
		output = XMLTransformer.validateOutput(output, null);
		XMLInputValue<?> source = (XMLInputValue<?>)input.get(_SOURCE);
		return () -> report(source.asXMLStreamReader());
	}

	private void report(XMLStreamReader reader) throws TransformerException {
		try {
			while (true)
				try {
					int event = reader.next();
					switch (event) {
					case START_ELEMENT:
						if (C_ERROR.equals(reader.getName())) {
							XProcError xprocError = XProcError.parse(reader);
							reporter.accept(xprocError);
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