package org.daisy.pipeline.saxon;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.daisy.pipeline.xpath.XPathFunctionRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.xml.sax.InputSource;

@Component(
	name = "saxon-configurator",
	service = { SaxonConfigurator.class }
)
public class SaxonConfigurator {

	public Configuration newConfiguration() {
		try {
			Configuration config = Configuration.readConfiguration(
				new SAXSource(new InputSource(getConfigurationAsStream())));
			if (uriResolver != null)
				config.setURIResolver(uriResolver);
			if (xpathExtensionFunctions != null)
				for (ExtensionFunctionDefinition function : xpathExtensionFunctions.getFunctions())
					config.registerExtensionFunction(function);
			return config;
		} catch (XPathException e) {
			throw new RuntimeException(e);
		}
	}

	public InputStream getConfigurationAsStream() {
		// see http://www.saxonica.com/documentation9.5/configuration/configuration%2Dfile
		return new ByteArrayInputStream((
			"<configuration xmlns='http://saxon.sf.net/ns/configuration'>" +
			"   <global expandAttributeDefaults='false'" +
			"           lineNumbering='true'" +
			"           suppressXsltNamespaceCheck='true'/>" +
			"</configuration>\n"
			).getBytes(StandardCharsets.UTF_8));
	}

	public Iterable<ExtensionFunctionDefinition> getExtensionFunctions() {
		if (xpathExtensionFunctions != null)
			return Collections.unmodifiableCollection(xpathExtensionFunctions.getFunctions());
		else
			return Collections.<ExtensionFunctionDefinition>emptySet();
	}

	public URIResolver getURIResolver() {
		return uriResolver;
	}

	public void configure(Processor processor) {
		Configuration config = newConfiguration();
		processor.setConfigurationProperty(FeatureKeys.CONFIGURATION, config);
		config.setProcessor(processor);
	}

	public void configure(TransformerFactoryImpl transformerFactory) {
		configure(transformerFactory.getProcessor());
	}

	public void configure(XPathFactoryImpl xpathFactory) {
		xpathFactory.setConfiguration(newConfiguration());
	}
	
	private URIResolver uriResolver = null;
	private XPathFunctionRegistry xpathExtensionFunctions = null;
	
	@Reference(
		name = "FunctionLibrary",
		unbind = "-",
		service = XPathFunctionRegistry.class,
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.STATIC
	)
	public void setXPathFunctionRegistry(XPathFunctionRegistry xpathFunctions) {
		logger.debug("Setting function registry");
		xpathExtensionFunctions = xpathFunctions;
	}

	@Reference(
		name = "URIResolver",
		unbind = "-",
		service = URIResolver.class,
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.STATIC
	)
	public void setURIResolver(URIResolver resolver) {
		uriResolver = resolver;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(SaxonConfigurator.class);
	
}
