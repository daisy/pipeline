package org.daisy.dotify.engine.impl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineConfigurationException;
import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.api.obfl.ExpressionFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.formatter.impl.SPIHelper;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class LayoutEngineFactoryImpl implements FormatterEngineFactoryService {
	private FactoryManager factoryManager;

	public LayoutEngineFactoryImpl() {
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
	public LayoutEngineImpl newFormatterEngine(String locale, String mode, PagedMediaWriter writer) {
		setupFactoryManager();
		return new LayoutEngineImpl(locale, mode, writer, factoryManager);
	}

	@Override
	public FormatterEngine newFormatterEngine(FormatterConfiguration config, PagedMediaWriter writer) {
		setupFactoryManager();
		return new LayoutEngineImpl(config, writer, factoryManager);
	}

	@Override
	@Deprecated
	public <T> void setReference(Class<T> c, T factory) throws FormatterEngineConfigurationException {
		if (c.equals(FormatterFactory.class)) {
			setFormatterFactory((FormatterFactory)factory);
		} else if (c.equals(MarkerProcessorFactoryMakerService.class)) {
			setMarkerProcessor((MarkerProcessorFactoryMakerService)factory);
		} else if (c.equals(TextBorderFactoryMakerService.class)) {
			setTextBorderFactoryMaker((TextBorderFactoryMakerService)factory);
		} else if (c.equals(ExpressionFactory.class)) {
			setExpressionFactory((ExpressionFactory)factory);
		}
		
		else {
			throw new FormatterEngineConfigurationException("Unrecognized reference: " +factory);
		}
	}
	
	// FIXME: not a service
	@Reference
	public void setFormatterFactory(FormatterFactory formatterFactory) {
		factoryManager.setFormatterFactory(formatterFactory);
	}

	public void unsetFormatterFactory(FormatterFactory formatterFactory) {
		factoryManager.setFormatterFactory(null);
	}

	@Reference
	public void setMarkerProcessor(MarkerProcessorFactoryMakerService mp) {
		factoryManager.setMarkerProcessorFactory(mp);
	}

	public void unsetMarkerProcessor(MarkerProcessorFactoryMakerService mp) {
		factoryManager.setMarkerProcessorFactory(null);
	}

	@Reference
	public void setTextBorderFactoryMaker(TextBorderFactoryMakerService tbf) {
		factoryManager.setTextBorderFactory(tbf);
	}

	public void unsetTextBorderFactoryMaker(TextBorderFactoryMakerService tbf) {
		factoryManager.setTextBorderFactory(null);
	}

	@Reference
	public void setExpressionFactory(ExpressionFactory ef) {
		factoryManager.setExpressionFactory(ef);
	}

	public void unsetExpressionFactory(ExpressionFactory ef) {
		factoryManager.setExpressionFactory(null);
	}

	@Override
	public void setCreatedWithSPI() {
		setFormatterFactory(SPIHelper.getFormatterFactory());
		setMarkerProcessor(SPIHelper.getMarkerProcessorFactoryMaker());
		setTextBorderFactoryMaker(SPIHelper.getTextBorderFactoryMaker());
		setExpressionFactory(SPIHelper.getExpressionFactory());
		factoryManager.setTransformerFactory(TransformerFactory.newInstance());
	}

}
