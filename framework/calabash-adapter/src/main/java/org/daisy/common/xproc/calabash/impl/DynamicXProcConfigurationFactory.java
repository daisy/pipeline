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

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.xproc.calabash.ConfigurationFileProvider;
import org.daisy.common.xproc.calabash.XProcConfigurationFactory;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.calabash.XProcStepRegistry;
import org.daisy.pipeline.xpath.XPathFunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.runtime.XAtomicStep;

/**
 * Mainly thought to be used throught OSGI this class creates configuration
 * objects used with the calabash engine wrapper.
 */
public class DynamicXProcConfigurationFactory implements
		XProcConfigurationFactory, XProcStepRegistry {

	/** The Constant CONFIG_PATH. */
	public static final String CONFIG_PATH = "org.daisy.pipeline.xproc.configuration";

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory
			.getLogger(DynamicXProcConfigurationFactory.class);

	/** The step providers. */
	private final Map<QName, XProcStepProvider> stepProviders = new HashMap<QName, XProcStepProvider>();

	// private FunctionLibraryList mFunctionLibrary=new FunctionLibraryList();
	private XPathFunctionRegistry mXPathRegistry = null;
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
		XProcConfiguration config = new DynamicXProcConfiguration(this);
		loadMainConfigurationFile(config);
		for (ConfigurationFileProvider f : configurationFiles) {
			logger.debug("Reading {}", f);
			loadConfigurationFile(config, f.get());
		}
		registerExtensionFunctions(config);
		// config.getProcessor().getUnderlyingConfiguration().addExtensionBinders(mFunctionLibrary);

		return config;
	}

	/**
	 * Activate (OSGI)
	 */
	public void activate() {
		logger.trace("Activating XProc Configuration Factory");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.daisy.common.xproc.calabash.XProcConfigurationFactory#newConfiguration
	 * (boolean)
	 */
	@Override
	public XProcConfiguration newConfiguration(boolean schemaAware) {
		XProcConfiguration config = new DynamicXProcConfiguration(schemaAware,
				this);
		loadMainConfigurationFile(config);
		for (ConfigurationFileProvider f : configurationFiles) {
			logger.debug("Reading {}", f);
			loadConfigurationFile(config, f.get());
		}
		registerExtensionFunctions(config);
		// config.getProcessor().getUnderlyingConfiguration().addExtensionBinders(mFunctionLibrary);
		return config;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.daisy.common.xproc.calabash.XProcConfigurationFactory#newConfiguration
	 * (net.sf.saxon.s9api.Processor)
	 */
	@Override
	public XProcConfiguration newConfiguration(Processor processor) {
		XProcConfiguration config = new DynamicXProcConfiguration(processor,
				this);
		loadMainConfigurationFile(config);
		for (ConfigurationFileProvider f : configurationFiles) {
			logger.debug("Reading {}", f);
			loadConfigurationFile(config, f.get());
		}
		registerExtensionFunctions(config);
		// config.getProcessor().getUnderlyingConfiguration().addExtensionBinders(mFunctionLibrary);
		return config;
	}

	/**
	 * Adds the step.
	 *
	 * @param stepProvider
	 *            the step provider
	 * @param properties
	 *            the properties
	 */
	public void addStep(XProcStepProvider stepProvider, Map<?, ?> properties) {
		QName type = QName.fromClarkName((String) properties.get("type"));
		logger.debug("Adding step to registry: {}", type.toString());
		stepProviders.put(type, stepProvider);
	}

	/**
	 * Removes the step from the registry
	 *
	 * @param stepProvider
	 *            the step provider
	 * @param properties
	 *            the properties
	 */
	public void removeStep(XProcStepProvider stepProvider, Map<?, ?> properties) {
		QName type = QName.fromClarkName((String) properties.get("type"));
		logger.debug("Removing step from registry: {}", type.toString());
		stepProviders.remove(type);
	}

	/**
	 * Adds a configuration file
	 */
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
	 *
	 * @param conf
	 *            the conf
	 */
	private void loadMainConfigurationFile(XProcConfiguration conf) {
		// TODO cleanup and cache
		String prop = System.getProperty(CONFIG_PATH);
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

	public void setXPathFunctionRegistry(XPathFunctionRegistry xpathFunctions) {
		logger.debug("Setting function registry");
		// mFunctionLibrary.addFunctionLibrary(xpathFunctions);
		mXPathRegistry = xpathFunctions;
	}

	private void registerExtensionFunctions(XProcConfiguration config) {
		if (mXPathRegistry != null) {
			for (ExtensionFunctionDefinition func : mXPathRegistry
					.getFunctions()) {
				config.getProcessor().registerExtensionFunction(func);
			}
		}
	}
}
