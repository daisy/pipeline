package org.daisy.dotify.formatter.impl.obfl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;

import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.api.formatter.FormatterFactoryMaker;
import org.daisy.dotify.api.obfl.ExpressionFactory;
import org.daisy.dotify.api.obfl.ExpressionFactoryMaker;
import org.daisy.dotify.api.obfl.ObflParser;
import org.daisy.dotify.api.obfl.ObflParserFactoryService;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMaker;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMaker;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.formatter.impl.FactoryManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * Provides an expression factory implementation.
 * @author Joel Håkansson
 */
@Component
public class ObflParserFactoryImpl implements ObflParserFactoryService {
	private FactoryManager factoryManager;

	/**
	 * Creates a new obfl parser factory instance.
	 */
	public ObflParserFactoryImpl() {
		factoryManager = new FactoryManager();
	}
	
	private void setupFactoryManager() {
		//FIXME: all calls to newInstance below are OSGi violations that should be fixed.
		if (factoryManager.getTransformerFactory()==null) {
			factoryManager.setTransformerFactory(new net.sf.saxon.TransformerFactoryImpl());
		}
		if (factoryManager.getXpathFactory()==null) {
			factoryManager.setXpathFactory(XPathFactory.newInstance());
		}
		if (factoryManager.getDocumentBuilderFactory()==null) {
			factoryManager.setDocumentBuilderFactory(DocumentBuilderFactory.newInstance());
		}
		if (factoryManager.getXmlInputFactory()==null) {
			XMLInputFactory in = XMLInputFactory.newInstance();
			in.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
			in.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
			in.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
			in.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
			factoryManager.setXmlInputFactory(in);
		}
		if (factoryManager.getXmlOutputFactory()==null) {
			factoryManager.setXmlOutputFactory(XMLOutputFactory.newInstance());
		}
		if (factoryManager.getXmlEventFactory()==null) {
			factoryManager.setXmlEventFactory(XMLEventFactory.newInstance());
		}
	}

	@Override
	public ObflParser newObflParser() {
		setupFactoryManager();
		return new ObflParserImpl(factoryManager);
	}

	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setFormatterFactory(FormatterFactory service) {
		factoryManager.setFormatterFactory(service);
	}

	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetFormatterFactory(FormatterFactory service) {
		factoryManager.setFormatterFactory(null);
	}

	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setMarkerProcessor(MarkerProcessorFactoryMakerService service) {
		factoryManager.setMarkerProcessorFactory(service);
	}

	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetMarkerProcessor(MarkerProcessorFactoryMakerService service) {
		factoryManager.setMarkerProcessorFactory(null);
	}

	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setTextBorderFactoryMaker(TextBorderFactoryMakerService service) {
		factoryManager.setTextBorderFactory(service);
	}

	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetTextBorderFactoryMaker(TextBorderFactoryMakerService service) {
		factoryManager.setTextBorderFactory(null);
	}

	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setExpressionFactory(ExpressionFactory service) {
		factoryManager.setExpressionFactory(service);
	}

	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetExpressionFactory(ExpressionFactory service) {
		factoryManager.setExpressionFactory(null);
	}

	@Override
	public void setCreatedWithSPI() {
		setFormatterFactory(FormatterFactoryMaker.newInstance().getFactory());
		setMarkerProcessor(MarkerProcessorFactoryMaker.newInstance());
		setTextBorderFactoryMaker(TextBorderFactoryMaker.newInstance());
		setExpressionFactory(ExpressionFactoryMaker.newInstance().getFactory());
		factoryManager.setTransformerFactory(TransformerFactory.newInstance());
	}

}
