package org.daisy.common.file;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
	
	/**
	 * @param file A file
	 */
	public static URL asURL(File file) {
		return asURL(asURI(file));
	}
	
	/**
	 * @param url An encoded absolute URL
	 * @throws IllegalArgumentException
	 */
	public static URL asURL(String url) {
		return asURL(asURI(url));
	}
	
	/**
	 * @param url An encoded absolute URL
	 * @throws IllegalArgumentException
	 */
	public static URL asURL(URI url) {
		try {
			return url.toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * @param url A file
	 */
	public static URI asURI(File file) {
		return file.toURI();
	}
	
	/**
	 * @param url An encoded absolute or relative URL
	 * @throws IllegalArgumentException
	 */
	public static URI asURI(String url) {
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * @param url An encoded absolute or relative URL
	 * @throws IllegalArgumentException
	 */
	public static URI asURI(URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * @param url An unencoded absolute URL
	 * @return An encoded absolute URL
	 */
	public static URI encode(URL url) {
		try {
			if (url.getProtocol().equals("jar"))
				return new URI("jar:" + new URI(null, url.getAuthority(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString());
			else {
				String authority = (url.getPort() != -1) ?
					url.getHost() + ":" + url.getPort() :
					url.getHost();
				return new URI(url.getProtocol(), authority, url.getPath(), url.getQuery(), url.getRef());
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException("coding error", e);
		}
	}
	
	/**
	 * @param base An encoded absolute or relative URL
	 * @param url An encoded absolute or relative URL
	 * @return An encoded absolute or relative URL
	 */
	public static URI resolve(URI base, URI url) {
		if (base.toString().startsWith("jar:") && !url.isAbsolute())
			return asURI("jar:" + asURI(base.toASCIIString().substring(4)).resolve(url).toASCIIString());
		else
			return base.resolve(url);
	}
	
	/**
	 * @param base An encoded absolute or relative URL
	 * @param url An encoded absolute or relative URL
	 * @return An encoded absolute or relative URL
	 */
	public static URI relativize(URI base, URI url) {
		if (base.toString().startsWith("jar:") && url.toString().startsWith("jar:")) {
			base = asURI(base.toASCIIString().substring(4));
			url = asURI(url.toASCIIString().substring(4));
		}
		return base.relativize(url);
	}
	
	private static Map<String,Object> fsEnv = Collections.<String,Object>emptyMap();
	
	/**
	 * @param resource The (not URL-encoded) path of a resource inside the specified JAR or class directory
	 * @param context A class from the JAR or class directory that is to be searched for resources
	 * @return An encoded absolute URL
	 */
	public static URL getResourceFromJAR(String resource, Class<?> context) {
		if (OSGiHelper.inOSGiContext())
			return OSGiHelper.getResourceFromJAR(resource, context);
		else {
			URL jarFileURL = context.getProtectionDomain().getCodeSource().getLocation();
			if ("location:local".equals(jarFileURL.toString()) || jarFileURL.toString().startsWith("mvn:"))
				throw new RuntimeException("expected file URI");
			File jarFile = new File(asURI(jarFileURL));
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
						fs = FileSystems.newFileSystem(asURI("jar:" + asURI(jarFileURL)), fsEnv); }
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
	
	/**
	 * @param resource The (not URL-encoded) path of a directory inside the specified JAR or class directory
	 * @param context A class from the JAR or class directory that is to be searched for resources
	 * @return A list of resource paths (not URL-encoded)
	 */
	public static Iterator<String> listResourcesFromJAR(String directory, Class<?> context) {
		if (OSGiHelper.inOSGiContext())
			return OSGiHelper.listResourcesFromJAR(directory, context);
		else {
			URL jarFileURL = context.getProtectionDomain().getCodeSource().getLocation();
			if ("location:local".equals(jarFileURL.toString()) || jarFileURL.toString().startsWith("mvn:"))
				throw new RuntimeException("expected file URI");
			File jarFile = new File(asURI(jarFileURL));
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
						fs = FileSystems.newFileSystem(asURI("jar:" + asURI(jarFileURL)), fsEnv); }
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
						return asURL(encode(url)); }
				throw new RuntimeException("file does not exist"); }
			else {
				url = asURL(encode(url));
				if (!url.toString().endsWith("/")
				    && (resource.endsWith("/") || bundle.getEntry(resource + "/") != null))
					return asURL(url.toString() + "/");
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
			Enumeration<String> resources = bundle.getEntryPaths(directory);
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
