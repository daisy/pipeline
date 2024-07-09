package org.daisy.pipeline.tts.acapela.impl;

import java.net.URL;
import java.net.URLClassLoader;

import org.daisy.common.file.URLs;

/**
 * A child-first classloader that loads com.sun.jna.* classes from jnaerator-0.11-p1.jar.
 */
public class JNAClassLoader extends URLClassLoader {

	public JNAClassLoader() {
		super(new URL[]{URLs.getResourceFromJAR("/", JNAClassLoader.class),
		                URLs.getResourceFromJAR("jnaerator-0.11-p1.jar", JNAClassLoader.class)});
	}

	// override because by default loadClass first delegates to the parent classloader
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> loadedClass = findLoadedClass(name);
		if (loadedClass == null) {
			if (name.startsWith("org.daisy.pipeline.tts.acapela.impl.") || name.startsWith("com.sun.jna."))
				try {
					loadedClass = findClass(name);
				} catch (ClassNotFoundException e) {
					// should not happen, but try parent classloader
				}
			if (loadedClass == null)
				// delegate to parent classloader
				loadedClass = super.loadClass(name, resolve);
		}
		if (resolve)
			resolveClass(loadedClass);
		return loadedClass;
	}
}
