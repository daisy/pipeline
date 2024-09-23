package org.daisy.common.saxon.impl;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xml.DocumentBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.w3c.dom.Document;

import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

@Component(
	name = "saxon-document-builder",
	service = { DocumentBuilder.class }
)
public class DocumentBuilderImpl implements DocumentBuilder {

	@Override
	public boolean supportsContentType(String type) {
		return type.matches("[^ ]*(/|\\+)xml");
	}

	/**
	 * @param input an XML file
	 */
	@Override
	public Document parse(InputSource input) throws IOException, SAXException {
		net.sf.saxon.s9api.DocumentBuilder builder = processor.newDocumentBuilder();
		builder.setDTDValidation(false);
		// builder.setLineNumbering(true); // should not be needed: line-numbering is enabled by default
		SAXSource src = new SAXSource(input);
		// hack to set the entity resolver
		{
			XMLReader reader = src.getXMLReader();
			if (reader == null) {
				try {
					reader = XMLReaderFactory.createXMLReader();
					src.setXMLReader(reader);
					reader.setEntityResolver(entityResolver);
				} catch (SAXException se) {
				}
			}
		}
		try {
			return (Document)DocumentOverNodeInfo.wrap(builder.build(src).getUnderlyingNode());
		} catch (IllegalArgumentException e) {
			throw new IOException("Unsupported argument type: " + input.getClass(), e);
		} catch (SaxonApiException e) {
			throw new SAXException("XML could not be parsed", e);
		}
	}

	private ProcessorImpl processor = null;
	private EntityResolver entityResolver = null;

	@Reference(
		name = "Processor",
		unbind = "-",
		service = ProcessorImpl.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setProcessor(ProcessorImpl processor) {
		this.processor = processor;
	}

	@Reference(
		name = "entity-resolver",
		unbind = "-",
		service = EntityResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}
}
