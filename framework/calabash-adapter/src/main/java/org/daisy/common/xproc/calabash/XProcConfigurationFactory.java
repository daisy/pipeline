package org.daisy.common.xproc.calabash;

import java.util.Map;

import com.xmlcalabash.core.XProcConfiguration;

import org.daisy.common.xproc.XProcMonitor;

/**
 * Factory for creating XProcConfiguration objects.
 */
public interface XProcConfigurationFactory {

	/**
	 * Gets a new configuration object
	 */
	XProcConfiguration newConfiguration(XProcMonitor monitor, Map<String,String> properties);

}
