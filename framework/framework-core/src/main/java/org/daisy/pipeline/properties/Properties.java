package org.daisy.pipeline.properties;

import java.util.Map;

public class Properties {

	private final static java.util.Properties systemProperties = System.getProperties();
	private final static Map<String,String> systemEnv = System.getenv();

	public static String getProperty(String key) {
		return getProperty(key, null);
	}

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
