package org.daisy.common.xproc.calabash;

import java.util.Map;

import com.xmlcalabash.core.XProcRuntime;

import net.sf.saxon.Configuration;

import org.daisy.common.xproc.XProcMonitor;

/**
 * Factory for creating {@code XProcRuntime} objects.
 */
public interface XProcRuntimeFactory {

	/**
	 * Create a new {@code XProcRuntime} object
	 */
	XProcRuntime newRuntime(XProcMonitor monitor, Map<String,String> properties);

	/**
	 * Create a new {@code XProcRuntime} object based on an existing {@code Configuration} object.
	 */
	XProcRuntime newRuntime(Configuration saxonConfiguration, XProcMonitor monitor, Map<String,String> properties);

}
