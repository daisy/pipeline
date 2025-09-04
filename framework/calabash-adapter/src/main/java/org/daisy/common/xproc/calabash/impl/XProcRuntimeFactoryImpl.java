package org.daisy.common.xproc.calabash.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.URIResolver;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.saxon.SaxonConfigurator;
import org.daisy.common.xproc.calabash.ConfigurationFileProvider;
import org.daisy.common.xproc.calabash.XProcRuntimeFactory;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.calabash.XProcStepRegistry;
import org.daisy.common.xproc.XProcMonitor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

@Component(
	name = "XProcRuntimeFactoryImpl",
	service = {
		XProcRuntimeFactory.class,
	}
)
public class XProcRuntimeFactoryImpl implements XProcRuntimeFactory, XProcStepRegistry {

	private static final Logger logger = LoggerFactory.getLogger(XProcRuntimeFactoryImpl.class);

	@Override
	public XProcRuntime newRuntime(XProcMonitor monitor, Map<String,String> properties) {
		return newRuntime(newProcessor(saxonConfigurator), monitor, properties);
	}

	@Override
	public XProcRuntime newRuntime(Configuration configuration, XProcMonitor monitor, Map<String,String> properties) {
		return newRuntime(new Processor(configuration), monitor, properties);
	}

	private XProcRuntime newRuntime(Processor processor, XProcMonitor monitor, Map<String,String> properties) {
		System.setProperty("com.xmlcalabash.config.user", ""); // skip loading configuration from ~/.calabash
		DynamicXProcConfiguration config = new DynamicXProcConfiguration(
			processor, this, monitor, properties);
		for (ConfigurationFileProvider f : configurationFiles) {
			logger.debug("Reading {}", f);
			loadConfigurationFile(config, f.get());
		}
		XProcRuntime runtime = new XProcRuntime(config);
		// XPath extension functions have not been registered yet
		saxonConfigurator.registerExtensionFunctions(
			config.getProcessor().getUnderlyingConfiguration(),
			Arrays.asList(runtime, monitor, properties));
		if (uriResolver != null)
			runtime.setURIResolver(uriResolver);
		if (entityResolver != null)
			runtime.setEntityResolver(entityResolver);
		XProcMessageListenerAggregator listeners = new XProcMessageListenerAggregator();
		listeners.add(new slf4jXProcMessageListener());
		runtime.setMessageListener(listeners);
		return runtime;
	}

	private static Processor newProcessor(SaxonConfigurator saxonConfigurator) {
		Processor processor = new Processor(false);
		saxonConfigurator.configure(processor, false, null); // XPath functions are not registered yet
		return processor;
	}

	private void loadConfigurationFile(XProcConfiguration conf, InputStream config) {
		SAXSource source = new SAXSource(new InputSource(config));
		DocumentBuilder builder = conf.getProcessor().newDocumentBuilder();
		XdmNode doc;
		try {
			doc = builder.build(source);
		} catch (SaxonApiException e) {
			logger.error("Error loading configuration file", e);
			throw new RuntimeException("error loading configuration file", e);
		}
		conf.parse(doc);
	}

	@Override
	public boolean hasStep(QName type) {
		return stepProviders.containsKey(type);
	}

	@Override
	public XProcStep newStep(QName type, XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
		XProcStepProvider stepProvider = stepProviders.get(type);
		return (stepProvider != null) ? stepProvider.newStep(runtime, step, monitor, properties) : null;
	}

	private URIResolver uriResolver = null;

	@Reference(
		name = "EntityResolver",
		unbind = "-",
		service = EntityResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	// used in CalabashXProcPipeline
	EntityResolver entityResolver = null;

	@Reference(
		name = "URIResolver",
		unbind = "-",
		service = URIResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}

	@Reference(
		name = "ConfigurationFileProvider",
		unbind = "-",
		service = ConfigurationFileProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addConfigurationFile(ConfigurationFileProvider provider) {
		logger.debug("Adding " + provider);
		configurationFiles.add(provider);
	}

	private final List<ConfigurationFileProvider> configurationFiles = new ArrayList<>();

	@Reference(
		name = "XProcStepProvider",
		unbind = "-",
		service = XProcStepProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addStep(XProcStepProvider stepProvider, Map<?,?> properties) {
		QName type = QName.fromClarkName((String) properties.get("type"));
		logger.debug("Adding step to registry: {}", type.toString());
		stepProviders.put(type, stepProvider);
	}

	private final Map<QName,XProcStepProvider> stepProviders = new HashMap<QName,XProcStepProvider>();
	@Reference(
		name = "SaxonConfigurator",
		unbind = "-",
		service = SaxonConfigurator.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setSaxonConfigurator(SaxonConfigurator configurator) {
		logger.debug("Setting Saxon configurator");
		saxonConfigurator = configurator;
	}

	private SaxonConfigurator saxonConfigurator = null;

}
