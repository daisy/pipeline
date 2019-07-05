package org.daisy.pipeline.modules;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.daisy.pipeline.modules.ResourceLoader;

public class JarModuleBuilder extends AbstractModuleBuilder<JarModuleBuilder> {
	
	public JarModuleBuilder withJarFile(final File jarFile) {
		withLoader(new ResourceLoader() {
			
			// Can't use ClassLoader.getResource() because there can be name
			// clashes between resources in different JARs. Alternative
			// solution would be to have a ClassLoader for each JAR.
			@Override
			@SuppressWarnings(
				"deprecation" // URLDecode.decode is deprecated
			)
			public URL loadResource(String path) {
				// Paths are assumed to be relative to META-INF
				if (!path.startsWith("../")) {
					throw new RuntimeException("Paths must start with '../' but got '" + path + "'");
				}
				path = path.substring(3);
				try {
					return jarFile.isDirectory() ?
						new URL(         URLDecoder.decode((jarFile.toURI().toASCIIString() + "/" + path).replace("+", "%2B"))) :
						new URL("jar:" + URLDecoder.decode((jarFile.toURI().toASCIIString() + "!/" + path).replace("+", "%2B")));
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public Iterable<URL> loadResources(String path) {
				//throw new UnsupportedOperationException("Not supported without OSGi.");
				ArrayList<URL> result = new ArrayList<URL>();
				if(path.startsWith("../")) path = path.substring(3); // Delete ../
				
				File currentElement;
				if(jarFile.isDirectory()) { // Resource directory within module registry tests
					currentElement = new File(jarFile,path);
					if( !currentElement.exists()) {
						throw new RuntimeException(currentElement.toString() + " was not found in the resources of the module (" + jarFile.toString() + ")");
					} else if(currentElement.isFile()) {
						try {
							result.add(currentElement.toURI().toURL()); }
						catch (MalformedURLException e) {
							throw new RuntimeException(e); }
					} else {
						for (String fileName : currentElement.list()) {
							ArrayList<URL> temp = (ArrayList<URL>) this.loadResources(
									path + 
									(!path.endsWith("/") ? "/" : "") + 
									fileName);
							result.addAll(temp);
						}
					}
					return result;
				} else try { // in a Jar archive (in a module tests)
					final String finalPath = path;
					ZipFile temp = new ZipFile(jarFile);
					temp.stream()
						.map(ZipEntry::getName)
						.forEachOrdered(new Consumer<String>() {

							@Override
							public void accept(String t) {
								if(t.startsWith(finalPath) && !t.endsWith("/")) {
									try {
										result.add(
												new URL("jar:" + URLDecoder.decode((jarFile.toURI().toASCIIString() + "!/" + t).replace("+", "%2B"))));
									} catch (MalformedURLException e) {
										throw new RuntimeException("URL : " + "jar:" + URLDecoder.decode((jarFile.toURI().toASCIIString() + "!/" + t).replace("+", "%2B")),e);
									}
								}
							}
						});
					temp.close();
				} catch (IOException e) {
					throw new RuntimeException("Error while reading jar file " + jarFile.toString(),e);
				}
				return result;
			}
		});
		return this;
	}
	
	public JarModuleBuilder self() {
		return this;
	}
}
