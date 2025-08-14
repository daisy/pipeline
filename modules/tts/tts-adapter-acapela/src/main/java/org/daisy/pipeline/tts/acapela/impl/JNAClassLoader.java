package org.daisy.pipeline.tts.acapela.impl;

import org.daisy.common.java.JarIsolatedClassLoader;

/**
 * A child-first classloader that loads com.sun.jna.* classes from jnaerator-0.11-p1.jar.
 */
public class JNAClassLoader extends JarIsolatedClassLoader {

	public JNAClassLoader() {
		super();
		addCurrentJar();
		addJarRecursively("jnaerator-0.11-p1.jar");
	}
}
