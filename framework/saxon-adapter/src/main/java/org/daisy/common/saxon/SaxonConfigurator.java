package org.daisy.common.saxon;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
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
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.daisy.pipeline.modules.XSLTPackage;

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
		return newConfiguration(true, null);
	}

	public Configuration newConfiguration(boolean registerExtensionFunctions, Collection<Object> context) {
		try {
			Configuration config = Configuration.readConfiguration(
				new SAXSource(new InputSource(getConfigurationAsStream())));
			setURIResolver(config);
			if (registerExtensionFunctions)
				registerExtensionFunctions(config, context);
			addXsltPackages(config);
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
			"           suppressXsltNamespaceCheck='true'" +
			"           stripSpace='none'/>" +
			"</configuration>\n"
			).getBytes(StandardCharsets.UTF_8));
	}

	public void registerExtensionFunctions(Configuration config, Collection<Object> context) {
		if (xpathExtensionFunctions != null) {
			context = context != null ? new HashSet<>(context) : new HashSet<>();
			context.add(config);
			for (ExtensionFunctionDefinition function : getExtensionFunctions(context))
				config.registerExtensionFunction(function);
		}
	}

	public void addXsltPackages(Configuration config) {
		if (packages != null && !packages.isEmpty()) {
			PackageLibrary packageLib = config.getDefaultXsltCompilerInfo().getPackageLibrary();
			for (PackageDetails pack : packages)
				packageLib.addPackage(pack);
		}
	}

	public void setURIResolver(Configuration config) {
		if (uriResolver != null)
			config.setURIResolver(uriResolver);
	}

	/**
	 * @deprecated Use {@link #registerExtensionFunctions()}
	 */
	@Deprecated
	public Iterable<ExtensionFunctionDefinition> getExtensionFunctions() {
		return getExtensionFunctions(null);
	}

	private Iterable<ExtensionFunctionDefinition> getExtensionFunctions(Collection<Object> context) {
		if (xpathExtensionFunctions != null)
			return Collections.unmodifiableCollection(xpathExtensionFunctions.getFunctions(context));
		else
			return Collections.<ExtensionFunctionDefinition>emptySet();
	}

	/**
	 * @deprecated Use {@link #setURIResolver()}
	 */
	@Deprecated
	public URIResolver getURIResolver() {
		return uriResolver;
	}

	/**
	 * @deprecated Use {@link #addXsltPackages()}
	 */
	@Deprecated
	public Iterable<PackageDetails> getXsltPackages() {
		if (packages != null)
			return Collections.unmodifiableCollection(packages);
		else
			return Collections.<PackageDetails>emptySet();
	}

	public void configure(Processor processor) {
		configure(processor, true, null);
	}

	public void configure(Processor processor, boolean registerExtensionFunctions, Collection<Object> context) {
		Configuration config = newConfiguration(registerExtensionFunctions, context);
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
		unbind = "-",
		service = PackageDetails.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addPackageDetails(PackageDetails pack) {
		if (packages == null)
			packages = new ArrayList<PackageDetails>();
		packages.add(pack);
	}

	@Reference(
		name = "ModuleRegistry",
		unbind = "-",
		service = ModuleRegistry.class,
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.STATIC
	)
	public void setModuleRegistry(ModuleRegistry moduleRegistry) {
		for (Module module : moduleRegistry) {
			for (XSLTPackage pack : module.getXSLTPackages()) {
				addPackageDetails(
					new PackageDetails() {{
						try {
							nameAndVersion = new VersionedPackageName(pack.getName(), pack.getVersion());
							URL url = pack.getResource();
							sourceLocation = new StreamSource(url.openStream(), url.toURI().toASCIIString());
						} catch (XPathException|IOException|URISyntaxException e) {
							throw new IllegalStateException(
								"Failed to create PackageDetails object for package '" + pack.getName() + "'", e);
						}
					}}
				);
			}
		}
	}

	public void removePackageDetails(PackageDetails pack) {
		if (packages != null)
			packages.remove(pack);
	}

	private static final Logger logger = LoggerFactory.getLogger(SaxonConfigurator.class);

}
