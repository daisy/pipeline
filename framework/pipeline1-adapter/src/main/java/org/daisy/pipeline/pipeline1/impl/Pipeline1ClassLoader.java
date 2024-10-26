package org.daisy.pipeline.pipeline1.impl;

import org.daisy.common.java.JarIsolatedClassLoader;

public class Pipeline1ClassLoader extends JarIsolatedClassLoader {

	public Pipeline1ClassLoader() {
		super();
		addCurrentJar();
		addJarRecursively("pipeline.jar");
	}
}
