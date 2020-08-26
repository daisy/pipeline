package org.daisy.pipeline.tts.config;

import java.util.Map;

public interface ConfigProperties {

	/**
	 * @return the properties retrieved from the configuration editable by end-users
	 */
	public Map<String, String> getDynamicProperties();

	/**
	 * @return the properties retrieved from the configuration loaded at start-up
	 */
	public Map<String, String> getStaticProperties();

	/**
	 * @return both kind of properties. The dynamic ones might be protected.
	 */
	public Map<String, String> getAllProperties();

}
