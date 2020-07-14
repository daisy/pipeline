package org.daisy.common.file;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.BasicFileAttributeView;
import static java.nio.file.Files.walkFileTree;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.nio.file.SimpleFileVisitor;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class URLs {

	private URLs() {
	}
	
	private static final Logger logger = LoggerFactory.getLogger(URLs.class);
	
	/* If object is a String, it is assumed to represent a URI */
	public static URL asURL(Object o) {
		if (o == null)
			return null;
		try {
			if (o instanceof String)
				return asURL(URIs.asURI(o));
			if (o instanceof File)
				return asURL(URIs.asURI(o));
			if (o instanceof URL)
				return (URL)o;
			if (o instanceof URI)
				return new URL(decode(o.toString())); }
		catch (Exception e) {}
		throw new RuntimeException("Object can not be converted to URL: " + o);
	}
	
	public static URL resolve(Object base, Object url) {
		if (url instanceof URI)
			return asURL(URIs.asURI(base).resolve((URI)url));
		if (url instanceof String) {
			try { return new URL(asURL(base), url.toString()); }
			catch (MalformedURLException e) { throw new RuntimeException(e); }}
		return asURL(url);
	}
	
	public static String relativize(Object base, Object url) {
		return decode(URIs.asURI(base).relativize(URIs.asURI(url)).toString());
	}
	
	@SuppressWarnings(
		"deprecation" // URLDecode.decode is deprecated
	)
	public static String decode(String uri) {
		// URIs treat the + symbol as is, but URLDecoder will decode both + and %20 into a space
		return URLDecoder.decode(uri.replace("+", "%2B"));
	}
	
	private static Map<String,Object> fsEnv = Collections.<String,Object>emptyMap();
	
	// "JAR" can also be a directory.
	public static URL getResourceFromJAR(String resource, Class<?> context) {
		if (OSGiHelper.inOSGiContext())
			return OSGiHelper.getResourceFromJAR(resource, context);
		else {
			URL jarFileURL = context.getProtectionDomain().getCodeSource().getLocation();
			if ("location:local".equals(jarFileURL.toString()) || jarFileURL.toString().startsWith("mvn:"))
				throw new RuntimeException("expected file URI");
			File jarFile = new File(URIs.asURI(jarFileURL));
			logger.trace("Getting resource {} from JAR (current class: {}; JAR file: {})", resource, context, jarFile);
			if (resource.startsWith("/"))
				resource = resource.substring(1);
			boolean requestedDirectory = resource.endsWith("/");
			if (requestedDirectory)
				resource = resource.substring(0, resource.length() - 1);
			if (!jarFile.exists())
				throw new RuntimeException("coding error");
			else if (jarFile.isDirectory()) {
				File f = new File(jarFile, resource);
				if (!f.exists())
					throw new RuntimeException("file does not exist");
				else if (!f.isDirectory() && requestedDirectory)
					throw new RuntimeException("is not a directory");
				else
					return asURL(f); }
			else {
				FileSystem fs; {
					try {
						fs = FileSystems.newFileSystem(URIs.asURI("jar:" + URIs.asURI(jarFileURL)), fsEnv); }
					catch (IOException e) {
						throw new RuntimeException(e); }}
				try {
					Path f = fs.getPath("/" + resource);
					boolean isDirectory = isDirectory(f);
					if (!isDirectory && requestedDirectory)
						throw new RuntimeException("is not a directory");
					else
						try {
							return new URL("jar:" + jarFileURL + "!/" + resource + (isDirectory ? "/" : "")); }
						catch (MalformedURLException e) {
							throw new RuntimeException(e); }}
				finally {
					try {
						fs.close(); }
					catch (IOException e) {
						throw new RuntimeException(e); }
				}
			}
		}
	}
	
	// "JAR" can also be a directory.
	public static Iterator<String> listResourcesFromJAR(String directory, Class<?> context) {
		if (OSGiHelper.inOSGiContext())
			return OSGiHelper.listResourcesFromJAR(directory, context);
		else {
			URL jarFileURL = context.getProtectionDomain().getCodeSource().getLocation();
			if ("location:local".equals(jarFileURL.toString()) || jarFileURL.toString().startsWith("mvn:"))
				throw new RuntimeException("expected file URI");
			File jarFile = new File(URIs.asURI(jarFileURL));
			if (directory.startsWith("/"))
				directory = directory.substring(1);
			if (directory.endsWith("/"))
				directory = directory.substring(0, directory.length() - 1);
			if (!jarFile.exists())
				throw new RuntimeException("coding error");
			else if (jarFile.isDirectory()) {
				File d = new File(jarFile, directory);
				if (!d.exists())
					throw new RuntimeException("file does not exist");
				else if (!d.isDirectory())
					throw new RuntimeException("is not a directory");
				else {
					ImmutableList.Builder<String> resources = ImmutableList.<String>builder();
					for (File f : d.listFiles())
						resources.add(directory + "/" + f.getName() + (f.isDirectory() ? "/" : ""));
					return resources.build().iterator(); }}
			else {
				FileSystem fs; {
					try {
						fs = FileSystems.newFileSystem(URIs.asURI("jar:" + URIs.asURI(jarFileURL)), fsEnv); }
					catch (IOException e) {
						throw new RuntimeException(e); }}
				try {
					Path d = fs.getPath("/" + directory);
					if (!isDirectory(d))
						throw new RuntimeException("is not a directory");
					final ImmutableList.Builder<String> resources = ImmutableList.<String>builder();
					final String _directory = directory;
					try {
						walkFileTree(d, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
							public FileVisitResult visitFile(Path f, BasicFileAttributes _) throws IOException {
								String fileName = f.getFileName().toString();
								if (!fileName.endsWith("/") && isDirectory(f))
									fileName += "/";
								resources.add(_directory + "/" + fileName);
								return FileVisitResult.CONTINUE; }}); }
					catch (NoSuchFileException e) {
						throw new RuntimeException(e); }
					catch (IOException e) {
						throw new RuntimeException(e); }
					return resources.build().iterator(); }
				finally {
					try {
						fs.close(); }
					catch (IOException e) {
						throw new RuntimeException(e); }
				}
			}
		}
	}
	
	/**
	 * @throws RuntimeException if the file does not exist
	 */
	private static boolean isDirectory(Path p) throws RuntimeException {
		BasicFileAttributes a; {
			try {
				a = java.nio.file.Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes(); }
			catch (NoSuchFileException e) {
				throw new RuntimeException("file does not exist"); }
			catch (FileSystemNotFoundException e) {
				throw new RuntimeException(e); }
			catch (IOException e) {
				throw new RuntimeException(e); }}
		return a.isDirectory();
	}
	
	// static nested class in order to delay class loading
	private static abstract class OSGiHelper {
		
		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}
		
		static URL getResourceFromJAR(String resource, Class<?> context) {
			if (resource.startsWith("/"))
				resource = resource.substring(1);
			Bundle bundle = FrameworkUtil.getBundle(context);
			if (bundle == null)
				throw new IllegalArgumentException("no bundle can be found for class " + context);
			URL url = bundle.getEntry(resource);
			if (url == null) {
				if (resource.endsWith("/")) {
					url = bundle.getEntry(resource.substring(0, resource.length() - 1));
					if (url != null)
						throw new RuntimeException("is not a directory"); }
				else {
					url = bundle.getEntry(resource + "/");
					if (url != null)
						return url; }
				throw new RuntimeException("file does not exist"); }
			else {
				if (!url.toString().endsWith("/")
				    && (resource.endsWith("/") || bundle.getEntry(resource + "/") != null))
					try {
						return new URL(url.toString() + "/"); }
					catch (MalformedURLException e) {
						throw new RuntimeException("should not happen", e); }
				else
					return url; }
		}
		
		static Iterator<String> listResourcesFromJAR(String directory, Class<?> context) {
			if (directory.startsWith("/"))
				directory = directory.substring(1);
			if (!directory.endsWith("/"))
				directory = directory + "/";
			Bundle bundle = FrameworkUtil.getBundle(context);
			if (bundle == null)
				throw new IllegalArgumentException("no bundle can be found for class " + context);
			Enumeration<String> resources =  bundle.getEntryPaths(directory);
			if (resources == null) {
				if (bundle.getEntry(directory.substring(0, directory.length() - 1)) != null)
					throw new RuntimeException("is not a directory");
				else
					throw new RuntimeException("file does not exist"); }
			else
				return Iterators.forEnumeration(resources);
		}
	}
}
