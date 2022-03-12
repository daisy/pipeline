package org.daisy.pipeline.common.calabash.impl;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.OutputValue;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.common.xproc.XProcError;

public class ErrorReporter implements XMLTransformer {

	private static final QName C_ERROR = new QName("http://www.w3.org/ns/xproc-step", "error");

	private final Consumer<XProcError> reporter;

	ErrorReporter(Consumer<XProcError> reporter) {
		this.reporter = reporter;
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