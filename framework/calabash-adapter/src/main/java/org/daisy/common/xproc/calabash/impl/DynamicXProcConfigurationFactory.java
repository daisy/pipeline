package org.daisy.common.xproc.calabash.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.properties.Properties;
import org.daisy.common.saxon.SaxonConfigurator;
import org.daisy.common.xproc.calabash.ConfigurationFileProvider;
import org.daisy.common.xproc.calabash.XProcConfigurationFactory;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.calabash.XProcStepRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.runtime.XAtomicStep;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Mainly thought to be used throught OSGI this class creates configuration
 * objects used with the calabash engine wrapper.
 */
@Component(
	name = "calabash-config-factory",
	service = {
		XProcConfigurationFactory.class,
		XProcStepRegistry.class
	}
)
public class DynamicXProcConfigurationFactory implements XProcConfigurationFactory, XProcStepRegistry {

	public static final String CONFIG_PATH = "org.daisy.pipeline.xproc.configuration";

	private static final Logger logger = LoggerFactory.getLogger(DynamicXProcConfigurationFactory.class);

	private final Map<QName, XProcStepProvider> stepProviders = new HashMap<QName, XProcStepProvider>();
	private SaxonConfigurator saxonConfigurator = null;
	private final List<ConfigurationFileProvider> configurationFiles = new ArrayList<>();

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.daisy.common.xproc.calabash.XProcConfigurationFactory#newConfiguration
	 * ()
	 */
	@Override
	public XProcConfiguration newConfiguration() {
		System.setProperty("com.xmlcalabash.config.user", ""); // skip loading configuration from ~/.calabash
		Processor processor = new Processor(false);
		saxonConfigurator.configure(processor);
		XProcConfiguration config = new DynamicXProcConfiguration(processor, this);
		loadMainConfigurationFile(config);
		for (ConfigurationFileProvider f : configurationFiles) {
			logger.debug("Reading {}", f);
			loadConfigurationFile(config, f.get());
		}
		return config;
	}

	@Activate
	public void activate() {
		logger.trace("Activating XProc Configuration Factory");
	}

	/**
	 * Adds a step to the registry.
	 */
	@Reference(
		name = "XProcStepProvider",
		unbind = "removeStep",
		service = XProcStepProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addStep(XProcStepProvider stepProvider, Map<?, ?> properties) {
		QName type = QName.fromClarkName((String) properties.get("type"));
		logger.debug("Adding step to registry: {}", type.toString());
		stepProviders.put(type, stepProvider);
	}

	/**
	 * Removes a step from the registry.
	 */
	public void removeStep(XProcStepProvider stepProvider, Map<?, ?> properties) {
		QName type = QName.fromClarkName((String) properties.get("type"));
		logger.debug("Removing step from registry: {}", type.toString());
		stepProviders.remove(type);
	}

	/**
	 * Add a configuration file
	 */
	@Reference(
		name = "ConfigurationFileProvider",
		unbind = "removeConfigurationFile",
		service = ConfigurationFileProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addConfigurationFile(ConfigurationFileProvider provider) {
		logger.debug("Adding " + provider);
		configurationFiles.add(provider);
	}

	/**
	 * Removes a configuration file
	 */
	public void removeConfigurationFile(ConfigurationFileProvider provider) {
		logger.debug("Removing " + provider);
		configurationFiles.remove(provider);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.daisy.common.xproc.calabash.XProcStepRegistry#hasStep(net.sf.saxon
	 * .s9api.QName)
	 */
	@Override
	public boolean hasStep(QName type) {
		return stepProviders.containsKey(type);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.daisy.common.xproc.calabash.XProcStepRegistry#newStep(net.sf.saxon
	 * .s9api.QName, com.xmlcalabash.core.XProcRuntime,
	 * com.xmlcalabash.runtime.XAtomicStep)
	 */
	@Override
	public XProcStep newStep(QName type, XProcRuntime runtime, XAtomicStep step) {
		XProcStepProvider stepProvider = stepProviders.get(type);
		return (stepProvider != null) ? stepProvider.newStep(runtime, step)
				: null;
	}

	/**
	 * Loads the custom configuration file located in CONFIG_PATH
	 */
	private void loadMainConfigurationFile(XProcConfiguration conf) {
		// TODO cleanup and cache
		String prop = Properties.getProperty(CONFIG_PATH);
		if (prop != null) {
			File configPath; {
				if (prop.startsWith("file:")) {
					configPath = new File(URI.create(prop));
				} else {
					configPath = new File(prop);
				}
			}
			logger.debug("Reading Calabash configuration from {}", configPath);
			try {
				loadConfigurationFile(conf, new FileInputStream(configPath));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private void loadConfigurationFile(XProcConfiguration conf, InputStream config) {
		SAXSource source = new SAXSource(new InputSource(config));
		DocumentBuilder builder = conf.getProcessor().newDocumentBuilder();
		XdmNode doc;
		try {
			doc = builder.build(source);
		} catch (SaxonApiException e) {
			logger.error("Error loading configuration file", e);
			throw new RuntimeException("error loading configuration file",
					e);
		}
		conf.parse(doc);
	}

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
}
