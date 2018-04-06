package org.daisy.pipeline.properties;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Properties {

	private Properties() {
	}

	// System.getProperties() returns an object that is always up to date
	private final static java.util.Properties systemProperties = System.getProperties();
	private final static Map<String,String> systemEnv = System.getenv();

	/**
	 * Returns a set of all the keys in this property list.
	 */
	public static Set<String> propertyNames() {
		Set<String> keys = new HashSet<String>();
		keys.addAll(systemProperties.stringPropertyNames());
		for (String envKey : systemEnv.keySet())
			if (envKey.startsWith("PIPELINE2_"))
				keys.add("org.daisy.pipeline." + envKey.substring(10).replace('_','.').toLowerCase());
		return keys;
	}

	/**
	 * Gets the system property or environment variable indicated by the specified key.
	 *
	 * If the key starts with "org.daisy.pipeline", the function first looks for an
	 * environment variable and falls back to a system property. Otherwise it looks only
	 * for a system property. The name of the environment variable is derived from the
	 * name of the system property.
	 *
	 * For example, if the key is "org.daisy.pipeline.ws.host", an environment variable
	 * "PIPELINE2_WS_HOST" will have precedence over the system property.
	 *
	 * @param key The name of the system property.
	 * @return The string value of the system property or environment variable, or null if
	 *         there is no property or variable with that key.
	 */
	public static String getProperty(String key) {
		return getProperty(key, null);
	}

	/**
	 * @param key The name of the system property.
	 * @param defaultValue A default value.
	 * @return The string value of the system property or environment variable, or the
	 *         default value if there is no property or variable with that key.
	 */
	public static String getProperty(String key, String defaultValue) {
		if (key == null)
			throw new NullPointerException();
		if (key.startsWith("org.daisy.pipeline.")) {
			String envKey = "PIPELINE2_" + key.substring(19).replace('.', '_').toUpperCase();
			if (systemEnv.containsKey(envKey))
				return systemEnv.get(envKey);
		}
		return systemProperties.getProperty(key, defaultValue);
	}
}
