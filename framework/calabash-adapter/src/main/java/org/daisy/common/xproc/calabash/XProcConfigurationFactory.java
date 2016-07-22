package org.daisy.common.xproc.calabash;

import net.sf.saxon.s9api.Processor;

import com.xmlcalabash.core.XProcConfiguration;



/**
 * Factory for creating XProcConfiguration objects.
 */
public interface XProcConfigurationFactory {

	/**
	 * Gets a new configuration object
	 *
	 * @return the xproc configuration
	 */
	XProcConfiguration newConfiguration();

	/**
	 * Gets a new configuration object setting the engine schema awarness (see calabash documentation)
	 *
	 * @param schemaAware  schema aware?
	 * @return the x proc configuration
	 */
	XProcConfiguration newConfiguration(boolean schemaAware);

	/**
	 * Gets a new configuration object for the processor passed as argument
	 *
	 * @param processor the processor
	 * @return the x proc configuration
	 */
	XProcConfiguration newConfiguration(Processor processor);
}
