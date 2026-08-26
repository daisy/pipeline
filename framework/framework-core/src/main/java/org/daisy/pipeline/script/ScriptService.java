package org.daisy.pipeline.script;

import java.util.Map;

import org.daisy.common.properties.Properties;

/**
 * {@link Script} proxy.
 */
public interface ScriptService<T extends Script> {

	/**
	 * Get the script ID.
	 */
	String getId();

	/**
	 * Get the script version.
	 */
	String getVersion();

	/**
	 * Get the {@link Script} object.
	 */
	default T load() {
		return load(Properties.getGlobalSnapshot());
	}

	/**
	 * @param properties Take into account these properties when creating the script object.
	 */
	T load(Map<String,String> properties);

}
