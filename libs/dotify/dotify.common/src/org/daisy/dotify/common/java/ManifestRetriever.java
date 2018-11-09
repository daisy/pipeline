package org.daisy.dotify.common.java;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Retrieves a jar manifest, if available
 * @author Joel Håkansson
 *
 */
public class ManifestRetriever {
	private static final Logger logger = Logger.getLogger(ManifestRetriever.class.getCanonicalName());
	private final Class<?> clazz;
	private boolean failed = false;
	private Manifest manifest;

	/**
	 * 
	 * @param clazz a class in the jar to get a manifest for
	 */
	public ManifestRetriever(Class<?> clazz) {
		this.clazz = clazz;
		this.manifest = null;
	}
	/**
	 * Returns the jar manifest associated with this instance

	 * @return returns a manifest. If the class is not in a jar, the manifest is empty
	 */
	public Manifest getManifest() {
		if (failed || manifest!=null) {
			return manifest;
		}
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		if (!classPath.startsWith("jar")) {
		  // Class not from JAR
			failed = true;
			manifest = new Manifest();
		} else {
			String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + 
			    "/META-INF/MANIFEST.MF";
			try {
				manifest = new Manifest(new URL(manifestPath).openStream());
			} catch (IOException e) {
				failed = true;
				logger.log(Level.WARNING, "Failed to read manifest.", e);
			}
		}
		return manifest;
	}
	
	/**
	 * Returns true if <code>getManifest</code> has been called and the manifest could not be read
	 * @return true if <code>getManifest</code> previously failed, false otherwise
	 */
	public boolean failed() {
		return failed;
	}
}
