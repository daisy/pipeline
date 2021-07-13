package org.daisy.common.xproc.calabash;

import com.xmlcalabash.core.XProcConfiguration;

/**
 * Factory for creating XProcConfiguration objects.
 */
public interface XProcConfigurationFactory {

	/**
	 * Gets a new configuration object
	 */
	XProcConfiguration newConfiguration();

}
