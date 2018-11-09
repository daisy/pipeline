package org.daisy.dotify.formatter.impl.obfl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.daisy.dotify.api.formatter.FormatterCore;
import org.daisy.dotify.api.formatter.FormatterException;
import org.daisy.dotify.api.formatter.RenderingScenario;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.obfl.Expression;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XSLTRenderingScenario implements RenderingScenario {
	private final ObflParserImpl parser;
	private final Transformer t;
	private final Node node;
	private final TextProperties tp;
	private final Expression ev;
	private final String exp;

	public XSLTRenderingScenario(ObflParserImpl parser, Transformer t, Node node, TextProperties tp, Expression ev, String exp) {
		this.parser = parser;
		this.t = t;
		this.node = node;
		this.tp = tp;
		this.ev = ev;
		this.exp = exp;
	}

	@Override
	public void renderScenario(FormatterCore formatter) throws FormatterException {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			t.transform(new DOMSource(node), new StreamResult(os));
			
			//TODO: could event reader report the errors reported by the dom parser? Specifically, more than one root node.
			newDocumentFromInputStream(new ByteArrayInputStream(os.toByteArray()), parser.getFactoryManager().getDocumentBuilderFactory());

			//render
			XMLInputFactory factory =  parser.getFactoryManager().getXmlInputFactory();
			XMLEventIterator input = new XMLEventReaderAdapter(factory.createXMLEventReader(new ByteArrayInputStream(os.toByteArray())));
			XMLEvent event;
			while (input.hasNext()) {
				event = input.nextEvent();
				if (ObflParserImpl.equalsStart(event, ObflQName.XML_PROCESSOR_RESULT)) {
					// ok
				} else if (event.isCharacters()) {
					formatter.addChars(event.asCharacters().getData(), tp);
				} else if (ObflParserImpl.equalsStart(event, ObflQName.BLOCK)) {
					parser.parseBlock(event, input, formatter, tp);
				} else if (ObflParserImpl.equalsStart(event, ObflQName.TABLE)) {
					parser.parseTable(event, input, formatter, tp);
				} else if (parser.processAsBlockContents(formatter, event, input, tp)) {
					//done
				} else if (ObflParserImpl.equalsEnd(event, ObflQName.XML_PROCESSOR_RESULT)) {
					break;
				}
				else {
					ObflParserImpl.report(event);
				}
			}
		} catch (FormatterException e) {
			throw e;
		} catch (Exception e) {
			throw new FormatterException(e);
		}
	}
	
	private static Document newDocumentFromInputStream(InputStream in, DocumentBuilderFactory factory) throws FormatterException {
		Document ret = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			ret = builder.parse(new InputSource(in));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new FormatterException(e);
		}
		return ret;
	}

	@Override
	public double calculateCost(Map<String, Double> variables) {
		for (String key : variables.keySet()) {
			ev.setVariable(key, ((Double)variables.get(key)));
		}
		Double ret = Double.parseDouble(ev.evaluate(exp).toString());
		return ret;
	}
}