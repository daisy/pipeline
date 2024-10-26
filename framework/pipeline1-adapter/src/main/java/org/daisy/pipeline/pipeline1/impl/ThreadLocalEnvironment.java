package org.daisy.pipeline.pipeline1.impl;

import java.util.Properties;

import io.bmuskalla.internal.system.properties.DelegatingProperties;

public class ThreadLocalEnvironment implements AutoCloseable {

	private boolean restoreContextClassLoader = false;
	private ClassLoader originalContextClassLoader = null;
	private boolean restoreSystemProperties = false;
	private Properties originalSystemProperties = null;

	public ThreadLocalEnvironment(ClassLoader contextClassLoader, Properties systemProperties) {
		super();
		setContextClassLoader(contextClassLoader);
		if (systemProperties == null)
			throw new IllegalArgumentException();
		setSystemProperties(systemProperties);
	}

	public ThreadLocalEnvironment(ClassLoader contextClassLoader) {
		super();
		setContextClassLoader(contextClassLoader);
	}

	public ThreadLocalEnvironment(Properties systemProperties) {
		super();
		if (systemProperties == null)
			throw new IllegalArgumentException();
		setSystemProperties(systemProperties);
	}

	private void setContextClassLoader(ClassLoader contextClassLoader) {
		restoreContextClassLoader = true;
		originalContextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

	private void setSystemProperties(Properties systemProperties) {
		restoreSystemProperties = true;
		originalSystemProperties = System.getProperties();
		System.setProperties(new DelegatingProperties(originalSystemProperties));
		if (systemProperties != originalSystemProperties) {
			for (String p : System.getProperties().stringPropertyNames())
				System.clearProperty(p);
			for (String p : systemProperties.stringPropertyNames())
				System.setProperty(p, systemProperties.getProperty(p));
		}
	}

	@Override
	public void close() {
		if (restoreContextClassLoader)
			Thread.currentThread().setContextClassLoader(originalContextClassLoader);
		if (restoreSystemProperties)
			System.setProperties(originalSystemProperties);
	}
}
