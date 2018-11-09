package org.daisy.dotify.formatter.impl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;

import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.api.obfl.ExpressionFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;

/**
 * Provides a factory manager that can be used in OSGi as well as in SPI contexts.
 * The factory manager maintains a reference to all factory implementations needed
 * by this bundle.
 * @author Joel Håkansson
 */
public class FactoryManager {
	private FormatterFactory formatterFactory;
	private MarkerProcessorFactoryMakerService markerProcessorFactory;
	private TextBorderFactoryMakerService textBorderFactory;
	private ExpressionFactory expressionFactory;
	private XMLEventFactory xmlEventFactory;
	private XMLOutputFactory xmlOutputFactory;
	private XMLInputFactory xmlInputFactory;
	private DocumentBuilderFactory documentBuilderFactory;
	private XPathFactory xpathFactory;
	private TransformerFactory transformerFactory;

	/**
	 * Creates a new empty factory manager (all factories are null).
	 */
	public FactoryManager() {
	}

	/**
	 * Gets the formatter factory associated with this manager.
	 * @return returns a formatter factory, or null if not set
	 */
	public FormatterFactory getFormatterFactory() {
		return formatterFactory;
	}

	/** Sets the formatter factory associated with this manager.
	 * @param formatterFactory the formatter factory
	 */
	public void setFormatterFactory(FormatterFactory formatterFactory) {
		this.formatterFactory = formatterFactory;
	}

	/**
	 * Gets the marker processor factory associated with this manager.
	 * @return returns a marker processor factory, or null if not set
	 */
	public MarkerProcessorFactoryMakerService getMarkerProcessorFactory() {
		return markerProcessorFactory;
	}

	/** Sets the marker processor factory associated with this manager.
	 * @param markerProcessorFactory the marker processor factory
	 */
	public void setMarkerProcessorFactory(MarkerProcessorFactoryMakerService markerProcessorFactory) {
		this.markerProcessorFactory = markerProcessorFactory;
	}

	/**
	 * Gets the text border factory associated with this manager.
	 * @return returns a text border factory, or null if not set
	 */
	public TextBorderFactoryMakerService getTextBorderFactory() {
		return textBorderFactory;
	}

	/** Sets the text border factory associated with this manager.
	 * @param textBorderFactory the text border factory
	 */
	public void setTextBorderFactory(TextBorderFactoryMakerService textBorderFactory) {
		this.textBorderFactory = textBorderFactory;
	}

	/**
	 * Gets the expression factory associated with this manager.
	 * @return returns a expression factory, or null if not set
	 */
	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}

	/** Sets the expression factory associated with this manager.
	 * @param expressionFactory the expression factory
	 */
	public void setExpressionFactory(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
	}

	/**
	 * Gets the xml event factory associated with this manager.
	 * @return returns a xml event factory, or null if not set
	 */
	public XMLEventFactory getXmlEventFactory() {
		return xmlEventFactory;
	}

	/** Sets the xml event factory associated with this manager.
	 * @param xmlEventFactory the xml event factory
	 */
	public void setXmlEventFactory(XMLEventFactory xmlEventFactory) {
		this.xmlEventFactory = xmlEventFactory;
	}

	/**
	 * Gets the xml output factory associated with this manager.
	 * @return returns a xml output factory, or null if not set
	 */
	public XMLOutputFactory getXmlOutputFactory() {
		return xmlOutputFactory;
	}

	/** Sets the xml output factory associated with this manager.
	 * @param xmlOutputFactory the xml output factory
	 */
	public void setXmlOutputFactory(XMLOutputFactory xmlOutputFactory) {
		this.xmlOutputFactory = xmlOutputFactory;
	}

	/**
	 * Gets the xml input factory associated with this manager.
	 * @return returns a xml input factory, or null if not set
	 */
	public XMLInputFactory getXmlInputFactory() {
		return xmlInputFactory;
	}

	/** Sets the xml input factory associated with this manager.
	 * @param xmlInputFactory the xml input factory
	 */
	public void setXmlInputFactory(XMLInputFactory xmlInputFactory) {
		this.xmlInputFactory = xmlInputFactory;
	}

	/**
	 * Gets the document builder factory associated with this manager.
	 * @return returns a document builder factory, or null if not set
	 */
	public DocumentBuilderFactory getDocumentBuilderFactory() {
		return documentBuilderFactory;
	}

	/** Sets the document builder factory associated with this manager.
	 * @param documentBuilderFactory the document builder factory
	 */
	public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}

	/**
	 * Gets the xpath factory associated with this manager.
	 * @return returns a xpath factory, or null if not set
	 */
	public XPathFactory getXpathFactory() {
		return xpathFactory;
	}

	/** Sets the xpath factory associated with this manager.
	 * @param xpathFactory the xpath factory
	 */
	public void setXpathFactory(XPathFactory xpathFactory) {
		this.xpathFactory = xpathFactory;
	}

	/**
	 * Gets the transformer factory associated with this manager.
	 * @return returns a transformer factory, or null if not set
	 */
	public TransformerFactory getTransformerFactory() {
		return transformerFactory;
	}

	/** Sets the transformer factory associated with this manager.
	 * @param transformerFactory the transformer factory
	 */
	public void setTransformerFactory(TransformerFactory transformerFactory) {
		this.transformerFactory = transformerFactory;
	}

}
