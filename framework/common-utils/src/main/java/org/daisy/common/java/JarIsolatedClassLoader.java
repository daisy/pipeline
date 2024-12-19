package org.daisy.common.java;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.jar.Manifest;
import java.util.Set;
import java.util.TreeSet;

import org.daisy.common.file.URLs;

/**
 * Child-first delegating class loader that loads from JAR files embedded within the current JAR.
 *
 * Note that, unlike {@link URLClassLoader}, {@link JarIsolatedClassLoader} takes into account the
 * "Class-Path" defined in the MANIFEST.MF file of JAR files.
 */
public abstract class JarIsolatedClassLoader extends URLClassLoader {

	private final Set<String> classNames = new TreeSet<>();

	protected JarIsolatedClassLoader() {
		super(new URL[]{});
	}

	/**
	 * Add the current JAR file to the class path of this class loader. Or in other words, set up
	 * this class loader to load classes and resources located directly within the current JAR file.
	 *
	 * Calling this method is best to be avoided if possible, because it means that these classes
	 * and resources are also on the main class path, so it can not be guaranteed anymore that a
	 * class is not loaded by more than one class loader. (This is due to the fact that {@link
	 * URLClassLoader} uses parent-first delegation.)
	 */
	protected void addCurrentJar() {
		File currentJar = URLs.getCurrentJAR(getClass());
		addURL(URLs.asURL(currentJar));
		listClasses(currentJar.toPath(), "/", classNames::add);
	}

	private static void listClasses(Path jar, String dir, Consumer<String> collect) {
		Iterator<String> resources = URLs.listResourcesFromJAR(dir, jar);
		while (resources.hasNext()) {
			String p = resources.next();
			if (p.endsWith(".class")) {
				p = p.substring(0, p.length() - 6).replaceAll("/", ".");
				collect.accept(p);
			} else if (p.endsWith("/"))
				listClasses(jar, p, collect);
		}
	}

	/**
	 * Add the JAR file at the specified path within the current JAR to the class path of this class
	 * loader. This is done in a recursive way: the MANIFEST.MF of the JAR file is read, and all the
	 * paths in "Class-Path" resolved relatively against {@code path} and then added to the class
	 * path with {@link #addJarRecursively}.
	 */
	protected void addJarRecursively(String path) {
		addJarRecursively(URLs.getResourceFromJAR(path, URLs.getCurrentJAR(getClass()).toPath()));
	}

	private final Set<Path> pathsAdded = new HashSet<>();

	private void addJarRecursively(Path jar) {
		if (pathsAdded.add(jar)) {
			// copy to temporary file if needed, because URL and ClassLoader do not support nested JARs
			Path realJar = jar;
			if (jar.toUri().toString().matches("^jar:.+\\.jar$")) {
				try {
					realJar = Files.createTempFile(null, ".jar");
					Files.copy(jar, Files.newOutputStream(realJar));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				realJar.toFile().deleteOnExit();
			}
			addURL(URLs.asURL(realJar.toUri()));
			listClasses(realJar, "/", classNames::add);
			// URLClassLoader does not take into account MANIFEST.MF (see
			// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8168647),
			// so we need to do this ourselves
			Manifest manifest; {
				Path f = null;
				try {
					f = URLs.getResourceFromJAR("/META-INF/MANIFEST.MF", jar);
				} catch (RuntimeException e) {
				}
				if (f != null) {
					try {
						manifest = new Manifest(Files.newInputStream(f));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					String classPath = manifest.getMainAttributes().getValue("Class-Path");
					if (classPath != null)
						for (String p : classPath.trim().split("\\s+")) {
							Path dep = jar.getParent().resolve(p);
							if (Files.exists(dep)) // ignore dependency if it doesn't exist
								addJarRecursively(dep);
						}
				}
			}
		}
	}

	// override because by default loadClass first delegates to the parent classloader
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> loadedClass = findLoadedClass(name);
		if (loadedClass == null) {
			if (classNames.contains(name))
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


	// override in order to avoid discovery of SPI services from outside this JAR
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return findResources(name);
	}
}
