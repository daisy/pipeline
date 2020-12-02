package org.daisy.common.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Properties {

	private Properties() {
	}

	// System.getProperties() returns an object that is always up to date
	private static java.util.Properties propertiesFromFile = null;
	private final static java.util.Properties systemProperties = System.getProperties();
	private final static Map<String,String> systemEnv = System.getenv();
	private final static Set<String> internalProperties = ImmutableSet.of(
		"org.daisy.pipeline.version",
		"org.daisy.pipeline.xproc.configuration",
		"org.daisy.pipeline.updater.bin",
		"org.daisy.pipeline.updater.deployPath",
		"org.daisy.pipeline.updater.releaseDescriptor");
	private static Logger logger;
	private static Logger logger() {
		if (logger == null) logger = LoggerFactory.getLogger(Properties.class);
		return logger;
	}

	/**
	 * Returns a set of all the keys in this property list.
	 */
	public static Set<String> propertyNames() {
		Set<String> keys = new HashSet<String>();
		if (propertiesFromFile == null)
			propertiesFromFile = readPropertiesFromFile();
		if (propertiesFromFile != null)
			for (String p : propertiesFromFile.stringPropertyNames())
				if (p.startsWith("org.daisy.pipeline.") && !internalProperties.contains(p))
					keys.add(p);
		for (String p : systemProperties.stringPropertyNames())
			if (!internalProperties.contains(p))
				keys.add(p);
		for (String envKey : systemEnv.keySet())
			if (envKey.startsWith("PIPELINE2_")) {
				String p = "org.daisy.pipeline." + envKey.substring(10).replace('_','.').toLowerCase();
				if (!internalProperties.contains(p))
					keys.add(p); }
		return keys;
	}

	/**
	 * Gets the system property or environment variable indicated by the specified key.
	 *
	 * If the key starts with "org.daisy.pipeline", the function first looks for an environment
	 * variable, then for a system property, and then reads the pipeline.properties file. Otherwise
	 * only system properties are considered. The name of the environment variable is derived from
	 * the name of the system property.
	 *
	 * For example, if the key is "org.daisy.pipeline.ws.host", an environment variable
	 * "PIPELINE2_WS_HOST" will have precedence over the system property.
	 *
	 * Variable interpolation is supported, meaning that the value of a property may dereference
	 * other properties, using the "${...}" syntax.
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
		// environment variables have highest priority
		if (key.startsWith("org.daisy.pipeline.")) {
			String envKey = "PIPELINE2_" + key.substring(19).replace('.', '_').toUpperCase();
			if (systemEnv.containsKey(envKey))
				if (!internalProperties.contains(key))
					return expand(systemEnv.get(envKey));
				else
					logger().warn("Environment variable '{}' ignored; "
					              + "expected to be specified through system property", envKey);
		}
		// then come system properties
		String v = systemProperties.getProperty(key);
		if (v != null)
			return expand(v);
		// and finally properties defined in the pipeline.properties file
		if (key.startsWith("org.daisy.pipeline.")) {
			if (!internalProperties.contains(key)) {
				if (propertiesFromFile == null)
					propertiesFromFile = readPropertiesFromFile();
				if (propertiesFromFile != null) {
					v = propertiesFromFile.getProperty(key);
					if (v != null)
						return expand(v);
				}
			} else
				logger().warn("Property '{}' in pipeline.properties file ignored; "
				              + "expected to be specified through system property", key);
		}
		return defaultValue != null ? expand(defaultValue) : null;
	}

	private static java.util.Properties readPropertiesFromFile() {
		String propertiesFile = System.getProperty("org.daisy.pipeline.properties");
		if (propertiesFile != null) {
			java.util.Properties props = new java.util.Properties();
			try {
				props.load(new FileReader(propertiesFile));
				return props;
			} catch (IOException e) {
				// should normally not happen
				return null;
			}
		} else
			// should normally not happen
			return null;
	}

	private static Pattern variableReference = Pattern.compile("\\$\\{(?<var>[^\\}]+)\\}");

	private static String expand(String value) {
		Matcher m = variableReference.matcher(value);
		if (m.find()) {
			StringBuilder s = new StringBuilder();
			int i = 0;
			do {
				s.append(value.substring(i, m.start()));
				s.append(getProperty(m.group("var"), m.group()));
				i = m.end();
			} while (m.find());
			s.append(value.substring(i, value.length()));
			return s.toString();
		} else
			return value;
	}
}
