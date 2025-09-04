package org.daisy.common.xproc.calabash.impl;

import java.util.Map;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.xproc.calabash.XProcStepRegistry;
import org.daisy.common.xproc.XProcMonitor;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;

/**
 * This class allows to add new steps dynamically to calabash.
 */
public class DynamicXProcConfiguration extends XProcConfiguration {

	private final XProcStepRegistry stepRegistry;
	private final XProcMonitor monitor;
	private final Map<String,String> properties;

	/**
	 * Instantiates a new DynamicXProcConfiguration which holds the given step registry.
	 */
	public DynamicXProcConfiguration(Processor processor,
	                                 XProcStepRegistry stepRegistry,
	                                 XProcMonitor monitor,
	                                 Map<String,String> properties) {
		// Note that we create a new Processor for every now XProcConfigration, rather than using a singleton
		// Processor instance. We do this because XProcConfiguration mutates he Processor (registers XPath
		// extension functions) and this can't be undone.
		super(processor);
		this.stepRegistry = stepRegistry;
		this.monitor = monitor;
		this.properties = properties;
		extensionValues = true;
		sequenceAsContext = true;
		// FIXME: This is a hack to disable the Calabash hack that makes sure the Saxon processor
		// uses our resolver for everything. The way Calabash does it does not work in OSGi. Also I
		// don't understand why the call to net.sf.saxon.Configuration.setURIResolver() isn't
		// enough. Which cases are not covered by this?
		setSaxonProperties.add(net.sf.saxon.lib.FeatureKeys.ENTITY_RESOLVER_CLASS);
		setSaxonProperties.add(net.sf.saxon.lib.FeatureKeys.URI_RESOLVER_CLASS);
	}

	@Override
	public boolean isStepAvailable(QName type) {
		return stepRegistry.hasStep(type) || super.isStepAvailable(type);
	}

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		if (step == null) {
			return null;
		} else {
			XProcStep xprocStep = stepRegistry.newStep(step.getType(), runtime, step, monitor, properties);
			return (xprocStep != null) ? xprocStep : super.newStep(runtime, step);
		}
	}
}
