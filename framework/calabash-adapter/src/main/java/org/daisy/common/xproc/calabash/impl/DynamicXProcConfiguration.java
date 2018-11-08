package org.daisy.common.xproc.calabash.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.daisy.common.xproc.calabash.XProcStepRegistry;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.Input;

/**
 * This class allows to add new steps dynamically to calabash.
 */
public class DynamicXProcConfiguration extends XProcConfiguration {

	/** The step registry. */
	XProcStepRegistry stepRegistry;

	// see http://www.saxonica.com/documentation9.5/configuration/configuration%2Dfile
	final static Input saxonCfg = new Input(null, null, Input.Type.XML) {
		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream((
				"<configuration xmlns='http://saxon.sf.net/ns/configuration'>" +
				"   <global expandAttributeDefaults='false'/>" +
				"</configuration>\n"
				).getBytes(StandardCharsets.UTF_8));
		}
	};

	/**
	 * Instantiates a new dynamic x proc configuration which holds the given step registry.
	 *
	 * @param stepRegistry the step registry
	 */
	public DynamicXProcConfiguration(XProcStepRegistry stepRegistry) {
		super(saxonCfg);
		this.stepRegistry = stepRegistry;
		// FIXME: This is a hack to disable the Calabash hack that makes sure the Saxon processor
		// uses our resolver for everything. The way Calabash does it does not work in OSGi. Also I
		// don't understand why the call to net.sf.saxon.Configuration.setURIResolver() isn't
		// enough. Which cases are not covered by this?
		setSaxonProperties.add(net.sf.saxon.lib.FeatureKeys.ENTITY_RESOLVER_CLASS);
		setSaxonProperties.add(net.sf.saxon.lib.FeatureKeys.URI_RESOLVER_CLASS);
	}

	/**
	 * Instantiates a new dynamic x proc configuration.
	 *
	 * @param schemaAware the schema aware
	 * @param stepRegistry the step registry
	 */
	public DynamicXProcConfiguration(boolean schemaAware,
			XProcStepRegistry stepRegistry) {
		// FIXME: also use saxonCfg?
		super(schemaAware);
		this.stepRegistry = stepRegistry;
		setSaxonProperties.add(net.sf.saxon.lib.FeatureKeys.ENTITY_RESOLVER_CLASS);
		setSaxonProperties.add(net.sf.saxon.lib.FeatureKeys.URI_RESOLVER_CLASS);
	}

	/**
	 * Instantiates a new dynamic x proc configuration.
	 *
	 * @param processor the processor
	 * @param stepRegistry the step registry
	 */
	public DynamicXProcConfiguration(Processor processor,
			XProcStepRegistry stepRegistry) {
		super(processor);
		this.stepRegistry = stepRegistry;
		setSaxonProperties.add(net.sf.saxon.lib.FeatureKeys.ENTITY_RESOLVER_CLASS);
		setSaxonProperties.add(net.sf.saxon.lib.FeatureKeys.URI_RESOLVER_CLASS);
	}


	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcConfiguration#isStepAvailable(net.sf.saxon.s9api.QName)
	 */
	@Override
	public boolean isStepAvailable(QName type) {
		return stepRegistry.hasStep(type) || super.isStepAvailable(type);
	}


	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcConfiguration#newStep(com.xmlcalabash.core.XProcRuntime, com.xmlcalabash.runtime.XAtomicStep)
	 */
	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {

		if (step == null) {
			return null;
		} else {
			XProcStep xprocStep = stepRegistry.newStep(step.getType(), runtime,
					step);
			return (xprocStep != null) ? xprocStep : super.newStep(runtime,
					step);
		}
	}

}
