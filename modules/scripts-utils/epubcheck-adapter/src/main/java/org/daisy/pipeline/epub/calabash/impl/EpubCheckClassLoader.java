package org.daisy.pipeline.epub.calabash.impl;

import org.daisy.common.java.JarIsolatedClassLoader;

/**
 * A child-first classloader that loads net.sf.saxon.* classes from Saxon-HE-11.4.jar.
 */
public class EpubCheckClassLoader extends JarIsolatedClassLoader {

	public EpubCheckClassLoader() {
		super();
		addCurrentJar();
		addJarRecursively("Saxon-HE-11.4.jar");
	}
}
