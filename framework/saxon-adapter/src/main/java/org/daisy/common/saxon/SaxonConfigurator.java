package org.daisy.common.saxon;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.trans.packages.PackageDetails;
import net.sf.saxon.trans.packages.PackageLibrary;
import net.sf.saxon.trans.packages.VersionedPackageName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.daisy.common.xpath.saxon.XPathFunctionRegistry;

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
			if (packages != null && !packages.isEmpty()) {
				PackageLibrary packageLib = config.getDefaultXsltCompilerInfo().getPackageLibrary();
				for (PackageDetails pack : packages) {
					packageLib.addPackage(pack);
				}
			}
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

	public Iterable<PackageDetails> getXsltPackages() {
		if (packages != null)
			return Collections.unmodifiableCollection(packages);
		else
			return Collections.<PackageDetails>emptySet();
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
	private List<PackageDetails> packages = null;

	@Reference(
		name = "XPathFunctionRegistry",
		unbind = "-",
		service = XPathFunctionRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
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

	@Reference(
		name = "PackageDetails",
		unbind = "removePackageDetails",
		service = PackageDetails.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addPackageDetails(PackageDetails pack) {
		if (packages == null)
			packages = new ArrayList<PackageDetails>();
		packages.add(pack);
	}

	public void removePackageDetails(PackageDetails pack) {
		if (packages != null)
			packages.remove(pack);
	}

	private static final Logger logger = LoggerFactory.getLogger(SaxonConfigurator.class);

}
