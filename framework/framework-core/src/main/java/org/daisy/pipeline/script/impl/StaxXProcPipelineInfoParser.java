package org.daisy.pipeline.script.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPipelineInfo.Builder;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.script.impl.XProcScriptConstants.Attributes;
import org.daisy.pipeline.script.impl.XProcScriptConstants.Elements;
import org.daisy.pipeline.script.impl.XProcScriptConstants.Values;

/**
 * Parser the xproc pipeline info, not only for scripts but for regular steps.
 */
public class StaxXProcPipelineInfoParser {
	/** The xmlinputfactory. */
	private XMLInputFactory mFactory;

	/**
	 * Sets the factory.
	 *
	 * @param factory the new factory
	 */
	public void setFactory(XMLInputFactory factory) {
		mFactory = factory;
	}

	/**
	 * Parses the info from the xproc pipeline located at the provided uri
	 *
	 * @param uri the uri
	 * @return the x proc pipeline info
	 */
	public XProcPipelineInfo parse(URL url) {
		if (mFactory == null) {
			throw new IllegalStateException();
		}
		XProcPipelineInfo.Builder infoBuilder = new XProcPipelineInfo.Builder();
		try {
			infoBuilder.withURI(url.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error converting URL to URI", e);
		}

		InputStream is = null;
		XMLEventReader reader = null;
		try {

			// count number of input and output ports
			boolean singleInput;
			boolean singleOutput; {
				int inputCount = 0;
				int outputCount = 0;
				is = url.openConnection().getInputStream();
				reader = mFactory.createXMLEventReader(is);
				int depth = 0;
				while (reader.hasNext()) {
					XMLEvent event = reader.nextEvent();
					if (event.isStartElement()) {
						if (depth == 1) {
							QName name = event.asStartElement().getName();
							if (name.equals(Elements.P_INPUT)) {
								inputCount++;
							} else if (name.equals(Elements.P_OUTPUT)) {
								outputCount++;
							}
							if (inputCount > 1 && outputCount > 1)
								break;
						}
						depth++;
					} else if (event.isEndElement()) {
						depth--;
					}
					// FIXME: break the loop when we reach the subpipeline
				}
				singleInput = (inputCount == 1);
				singleOutput = (outputCount == 1);
				is = null;
				reader = null;
			}

			// parse options, input and outputs
			is = url.openConnection().getInputStream();
			reader = mFactory.createXMLEventReader(is);
			int depth = 0;
			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				if (event.isStartElement()) {
					if (depth == 1) {
						StartElement elem = event.asStartElement();
						QName name = elem.getName();
						if (name.equals(Elements.P_OPTION)) {
							parseOption(reader, elem, infoBuilder);
						} else if (name.equals(Elements.P_INPUT)) {
							parsePort(reader, elem, infoBuilder, singleInput);
						} else if (name.equals(Elements.P_OUTPUT)) {
							parsePort(reader, elem, infoBuilder, singleOutput);
						} else {
							depth++;
						}
					} else {
						depth++;
					}
				} else if (event.isEndElement()) {
					depth--;
				}
				// FIXME: break the loop when we reach the subpipeline
			}
			return infoBuilder.build();
		} catch (XMLStreamException e) {
			throw new RuntimeException("Parsing error: " + e.getMessage(),
					e);
		} catch (IOException e) {
			throw new RuntimeException("io error while parsing"
					+ e.getMessage(), e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				// ignore;
			}
		}
	}

	/**
	 * Parses a p:input or p:output element.
	 *
	 * Consumes the whole element (everything until and including the end event of the element).
	 */
	private static void parsePort(XMLEventReader reader, StartElement elem, Builder infoBuilder, boolean onlyPort)
			throws XMLStreamException {
		QName elemName = elem.getName();
		boolean primary = false;
		boolean sequence = false;
		Attribute portAttr = elem.getAttributeByName(Attributes.PORT);
		Attribute kindAttr = elem.getAttributeByName(Attributes.KIND);
		Attribute primaryAttr = elem.getAttributeByName(Attributes.PRIMARY);
		Attribute sequenceAttr = elem.getAttributeByName(Attributes.SEQUENCE);
		if (primaryAttr != null) {
			primary = Values.TRUE.equals(primaryAttr.getValue());
		} else {
			primary = onlyPort;
		}
		if (sequenceAttr != null && Values.TRUE.equals(sequenceAttr.getValue())) {
			sequence = true;
		}
		boolean hasDefaultConnection = false; {
			int depth = 1;
			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				if (event.isStartElement()) {
					if (!hasDefaultConnection && depth == 1 && Elements.CONNECTIONS.contains(event.asStartElement().getName())) {
						hasDefaultConnection = true;
					}
					depth++;
				} else if (event.isEndElement()) {
					depth--;
					if (depth == 0)
						break;
				}
			}
		}
		XProcPortInfo info = null;
		if (portAttr != null) {
			if (kindAttr != null && elemName.equals(Elements.P_INPUT)
					&& Values.PARAMETER.equals(kindAttr.getValue())) {
				info = XProcPortInfo.newParameterPort(portAttr.getValue(), primary);
			} else if (elemName.equals(Elements.P_INPUT)) {
				boolean required = !hasDefaultConnection;
				info = XProcPortInfo.newInputPort(portAttr.getValue(), sequence, required, primary);
			} else if (elemName.equals(Elements.P_OUTPUT)) {
				info = XProcPortInfo.newOutputPort(portAttr.getValue(), sequence, primary);
			}
		}
		if (info != null) {
			infoBuilder.withPort(info);
		}
	}

	/**
	 * Parses a p:option element
	 *
	 ¨Consumes the whole element (everything until and including the end event of the element).
	 */
	private static void parseOption(XMLEventReader reader, StartElement elem, final Builder infoBuilder)
			throws XMLStreamException {
		Attribute hiddenAttr = elem.getAttributeByName(Attributes.PX_HIDDEN);
		if (hiddenAttr == null || !Values.TRUE.equals(hiddenAttr.getValue())) {
			QName name = null;
			boolean required = false;
			String select = null;
			Attribute nameAttr = elem.getAttributeByName(Attributes.NAME);
			Attribute requiredAttr = elem.getAttributeByName(Attributes.REQUIRED);
			Attribute selectAttr = elem.getAttributeByName(Attributes.SELECT);
			if (nameAttr != null) {
				String nameVal = nameAttr.getValue();
				if (nameVal.contains(":")) {
					String prefix = nameVal.substring(0, nameVal.indexOf(":"));
					String namespace = elem.getNamespaceURI(prefix);
					String localPart = nameVal.substring(prefix.length() + 1, nameVal.length());
					name = new QName(namespace, localPart, prefix);
				} else {
					name = new QName(nameVal);
				}
			}
			if (requiredAttr != null) {
				if (Values.TRUE.equals(requiredAttr.getValue())) {
					required = true;
				}
			}
			if (selectAttr != null) {
				select = selectAttr.getValue();
			}
			infoBuilder.withOption(XProcOptionInfo.newOption(name, required, select));
		}
		// consume whole element
		int depth = 1;
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				depth++;
			} else if (event.isEndElement()) {
				depth--;
				if (depth == 0)
					break;
			}
		}
	}
}
