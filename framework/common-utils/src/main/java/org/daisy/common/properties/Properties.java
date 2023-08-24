package org.daisy.common.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Properties {

	private Properties() {
	}

	// System.getProperties() returns an object that can change afterwards, so make a copy to get
	// the initial system properties
	private final static Map<String,String> systemProperties = new HashMap<>(propertiesAsMap(System.getProperties()));
	private final static Map<String,String> systemEnv = System.getenv();
	private final static String PROPERTIES_FILE_PROPERTY = "org.daisy.pipeline.properties";
	private final static String propertiesFile = systemProperties.get(PROPERTIES_FILE_PROPERTY);
	private static Map<String,String> propertiesFromFile = null;

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
		synchronized (settableProperties) {
			for (String p : settableProperties.keySet())
				keys.add(p);
		}
		if (propertiesFromFile == null)
			propertiesFromFile = readPropertiesFromFile();
		if (propertiesFromFile != null)
			for (String p : propertiesFromFile.keySet())
				if (p.startsWith("org.daisy.pipeline."))
					keys.add(p);
		for (String p : systemProperties.keySet())
			keys.add(p);
		for (String envKey : systemEnv.keySet())
			if (envKey.startsWith("PIPELINE2_")) {
				String p = "org.daisy.pipeline." + envKey.substring(10).replace('_','.').toLowerCase();
				keys.add(p); }
		return keys;
	}

	/**
	 * Returns the momentary set of properties as a {@link Map} object.
	 */
	public static Map<String,String> getSnapshot() {
		synchronized (settableProperties) {
			java.util.Properties snapshot = new java.util.Properties();
			for (String prop : propertyNames()) {
				String val = getProperty(prop);
				if (val != null) // could be null for a settable property that has not been set
					snapshot.setProperty(prop, val);
			}
			return propertiesAsMap(snapshot);
		}
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
	 * @param key The name of the property.
	 * @param defaultValue A default value.
	 * @return The string value of the system property or environment variable, or the
	 *         default value if there is no property or variable with that key. Modifications to
	 *         settable properties are taken into account.
	 */
	public static String getProperty(String key, String defaultValue) {
		if (key == null)
			throw new IllegalArgumentException();
		// modified properties have highest priority
		synchronized (settableProperties) {
			SettableProperty p = settableProperties.get(key);
			if (p != null)
				// defaultValue ignored (only the defaultValue that was defined first is remembered)
				return p.getValue();
		}
		// then come environment variables
		if (key.startsWith("org.daisy.pipeline.")) {
			String envKey = "PIPELINE2_" + key.substring(19).replace('.', '_').toUpperCase();
			if (systemEnv.containsKey(envKey))
				return expand(systemEnv.get(envKey));
		}
		// then come system properties
		String v = systemProperties.get(key);
		if (v != null)
			return expand(v);
		// and finally properties defined in the pipeline.properties file
		if (key.startsWith("org.daisy.pipeline.")) {
			if (propertiesFromFile == null)
				propertiesFromFile = readPropertiesFromFile();
			if (propertiesFromFile != null) {
				v = propertiesFromFile.get(key);
				if (v != null)
					return expand(v);
			}
		}
		// return default value
		return defaultValue;
	}

	private static final Map<String,SettableProperty> settableProperties = new HashMap<>();

	/**
	 * @param settable Allow a property to be modified and get a {@link SettableProperty} object for
	 *                 reading future values.
	 */
	public static Property getProperty(String key, boolean settable, String description, boolean sensitive) {
		return getProperty(key, settable, description, sensitive, null);
	}

	public static Property getProperty(String key, boolean settable, String description, boolean sensitive, String defaultValue) {
		if (settable)
			synchronized (settableProperties) {
				if (settableProperties.containsKey(key)) {
					// defaultValue ignored (only the defaultValue that was defined first is remembered)
					return settableProperties.get(key);
				}
				SettableProperty prop = new SettableProperty(key, description, sensitive, defaultValue, getProperty(key));
				settableProperties.put(key, prop);
				return prop;
			}
		else
			return new Property(key, getProperty(key, defaultValue));
	}

	/**
	 * Get the current set of properties that can be modified.
	 */
	public static Set<SettableProperty> getSettableProperties() {
		synchronized (settableProperties) {
			return new HashSet<>(settableProperties.values());
		}
	}

	private static Map<String,String> readPropertiesFromFile() {
		if (propertiesFile != null) {
			java.util.Properties props = new java.util.Properties();
			try {
				props.load(new FileReader(propertiesFile));
				return propertiesAsMap(props);
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

	public static class Property {

		protected final String key;
		private final String value;

		private Property(String key, String value) {
			if (key == null)
				throw new IllegalArgumentException();
			this.key = key;
			this.value = value;
		}

		/**
		 * Get the property name
		 */
		public String getName() {
			return key;
		}

		/**
		 * Get the value
		 */
		public String getValue() {
			return value;
		}

		public String getValue(Map<String,String> snapshot) {
			return getValue();
		}
	}

	public static class SettableProperty extends Property {

		private final String description;
		private final boolean sensitive;
		private final String defaultValue;
		private String value;

		private SettableProperty(String key, String description, boolean sensitive, String defaultValue, String value) {
			super(key, defaultValue);
			this.description = description;
			this.sensitive = sensitive;
			this.value = value;
			this.defaultValue = defaultValue;
		}

		/**
		 * Set the value
		 */
		public synchronized void setValue(String value) {
			synchronized (settableProperties) {
				this.value = value;
			}
		}

		/**
		 * Get the current value, or the default value if the property has not been set yet and did
		 * not have an initial value.
		 */
		@Override
		public synchronized String getValue() {
			return value != null ? value : defaultValue;
		}

		/**
		 * Get the value that was in effect at the moment the snapshot was made, or the default
		 * value if the value was {@code null}.
		 */
		@Override
		public String getValue(Map<String,String> snapshot) {
			if (snapshot == null)
				throw new IllegalArgumentException();
			String v = snapshot.get(key);
			if (v != null)
				return v; // already expanded
			return defaultValue;
		}

		public String getDescription() {
			return description;
		}

		public boolean isSensitive() {
			return sensitive;
		}
	}

	private static Map<String,String> propertiesAsMap(java.util.Properties properties) {
		return (Map<String,String>)(Map)properties;
	}
}
