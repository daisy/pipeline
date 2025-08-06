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
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.SimpleFileVisitor;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of URI related utility functions.
 */
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
	 * Convert an absolute file URI to a {@link Path} denoting a file or an entry in a zip file.
	 */
	public static Path asPath(URI uri) throws IllegalArgumentException, URISyntaxException {
		if (!uri.isAbsolute())
			throw new IllegalArgumentException("expected absolute URI");
		String protocol = uri.getScheme();
		if (!"file".equals(protocol))
			throw new IllegalArgumentException("expected file URI");
		String query = uri.getQuery();
		if (query != null && !query.isEmpty())
			throw new IllegalArgumentException("expected URI without query");
		String fragment = uri.getFragment();
		if (fragment != null && !fragment.isEmpty())
			throw new IllegalArgumentException("expected URI without fragment");
		String path = uri.getPath();
		String zipPath = null;
		if (path.contains("!/")) {
			// it is a path to a ZIP entry
			zipPath = path.substring(path.indexOf("!/")+1);
			path = path.substring(0, path.indexOf("!/"));
		}
		File file = new File(new URI(protocol, null, path, null, null));
		if (file.exists() && zipPath != null)
			try {
				return FileSystems.newFileSystem(file.toPath(), null).getPath(zipPath);
			} catch (ProviderNotFoundException e) {
				throw new RuntimeException(e); // file is not a zip file
			} catch (IOException e) {
				throw new RuntimeException(e); // should not happen
			}
		else
			return file.toPath();
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
	 * Relativize a URI against a base URI.
	 *
	 * This functions differs from {@link URI#relativize} in that it is also able to find
	 * relative paths when the URI does not start with the base URI (in which case the
	 * resulting URI will contain ".." segments).
	 *
	 * @param base An encoded absolute or relative URL
	 * @param url An encoded absolute or relative URL
	 * @return An encoded absolute or relative URL
	 */
	public static URI relativize(URI base, URI uri) {
		if (base.toString().startsWith("jar:") && uri.toString().startsWith("jar:")) {
			base = asURI(base.toASCIIString().substring(4));
			uri = asURI(uri.toASCIIString().substring(4));
			return relativize(base, uri);
		}
		try {
			if (base.isOpaque() || uri.isOpaque()
			    || !Optional.ofNullable(base.getScheme()).orElse("").equalsIgnoreCase(Optional.ofNullable(uri.getScheme()).orElse(""))
			    || !Optional.ofNullable(base.getAuthority()).equals(Optional.ofNullable(uri.getAuthority())))
				return uri;
			else {
				String up = uri.normalize().getPath();
				String bp = base.normalize().getPath();
				String relativizedPath;
				if (up.startsWith("/")) {
					String[] upSegments = up.split("/", -1);
					String[] bpSegments = bp.split("/", -1);
					int i = bpSegments.length - 1;
					int j = 0;
					while (i > 0) {
						if (bpSegments[j].equals(upSegments[j])) {
							i--;
							j++; }
						else
							break; }
					relativizedPath = "";
					while (i > 0) {
						relativizedPath += "../";
						i--; }
					while (j < upSegments.length) {
						relativizedPath += upSegments[j] + "/";
						j++; }
					relativizedPath = relativizedPath.substring(0, relativizedPath.length() - 1); }
				else
					relativizedPath = up;
				if (relativizedPath.isEmpty())
					relativizedPath = "./";
				return new URI(null, null, relativizedPath, uri.getQuery(), uri.getFragment()); }
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static URI normalize(URI uri) {
		return normalize(uri, false);
	}

	public static URI normalize(URI uri, boolean dropFragment) {
		try {
			if (uri.isOpaque())
				return uri;
			if ("jar".equals(uri.getScheme()))
				return URLs.asURI("jar:" + normalize(URLs.asURI(uri.toASCIIString().substring(4))).toASCIIString());
			uri = uri.normalize();
			String scheme = uri.getScheme();
			if (scheme != null) scheme = scheme.toLowerCase();
			String authority = uri.getAuthority();
			if (authority != null) authority = authority.toLowerCase();
			if (authority != null && "http".equals(scheme) && authority.endsWith(":80"))
				authority = authority.substring(0, authority.length() - 3);
			uri = new URI(scheme, authority, uri.getPath(), uri.getQuery(), dropFragment ? null : uri.getFragment());
			// fix path
			String path = uri.getPath(); {
				// add "/" after trailing ".."
				path = path.replaceAll("(^|/)\\.\\.$", "$1../");
				// remove leading "/.."
				path = path.replaceAll("^/(\\.\\./)+", "/");
			}
			uri = new URI(uri.getScheme(), uri.getAuthority(), path, uri.getQuery(), uri.getFragment());
			uri = expand83(uri);
			return uri;
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Expand 8.3 encoded path segments.
	 *
	 * For instance `C:\DOCUME~1\file.xml` will become `C:\Documents and
	 * Settings\file.xml`
	 */
	public static String expand83(String uri) throws URISyntaxException, IOException {
		if (uri == null || !uri.startsWith("file:/")) {
			return uri;
		}
		return expand83(URLs.asURI(uri)).toASCIIString();
	}

	private static URI expand83(URI uri) throws URISyntaxException, IOException {
		if (uri == null || !"file".equals(uri.getScheme())) {
			return uri;
		}
		String protocol = "file";
		String path = uri.getPath();
		String zipPath = null;
		if (path.contains("!/")) {
			// it is a path to a ZIP entry
			zipPath = path.substring(path.indexOf("!/")+1);
			path = path.substring(0, path.indexOf("!/"));
		}
		String query = uri.getQuery();
		String fragment = uri.getFragment();
		File file = new File(new URI(protocol, null, path, null, null));
		URI expandedUri = expand83(file, path.endsWith("/"));
		if (expandedUri == null) {
			return uri;
		} else {
			path = expandedUri.getPath();
			if (zipPath != null)
				path = path + "!" + new URI(null, null, zipPath, null, null).getPath();
			return new URI(protocol, null, path, query, fragment);
		}
	}

	public static URI expand83(File file) throws URISyntaxException, IOException {
		return expand83(file, false);
	}

	private static URI expand83(File file, boolean isDir) throws URISyntaxException, IOException {
		if (file.exists()) {
			return URLs.asURI(file.getCanonicalFile());
		} else {
			// if the file does not exist a parent directory may exist which can be canonicalized
			String relPath = file.getName();
			if (isDir)
				relPath += "/";
			File dir = file.getParentFile();
			while (dir != null) {
				if (dir.exists())
					return URLs.resolve(URLs.asURI(dir.getCanonicalFile()), new URI(null, null, relPath, null, null));
				relPath = dir.getName() + "/" + relPath;
				dir = dir.getParentFile();
			}
			return URLs.asURI(file);
		}
	}
	
	private static final Map<String,Object> fsEnv = Collections.<String,Object>emptyMap();
	
	/**
	 * @param resource The (not URL-encoded) path of a resource inside the specified JAR or class directory
	 * @param context A class from the JAR or class directory that is to be searched for resources
	 * @return An encoded absolute URL
	 */
	public static URL getResourceFromJAR(String resource, Class<?> context) {
		if (OSGiHelper.inOSGiContext())
			return OSGiHelper.getResourceFromJAR(resource, context);
		else {
			File jarFile = getCurrentJAR(context);
			logger.trace("Getting resource {} from JAR (current class: {}; JAR file: {})", resource, context, jarFile);
			if (!jarFile.exists())
				throw new RuntimeException("coding error");
			Path p = getResourceFromJAR(resource, jarFile.toPath());
			URL u = asURL(p.toUri());
			if (isDirectory(p) && !u.toString().endsWith("/"))
				u = asURL(u.toString() + "/");
			try {
				p.getFileSystem().close();
			} catch (UnsupportedOperationException e) {
				// default file system
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return u;
		}
	}

	public static File getCurrentJAR(Class<?> context) {
		URL jarFileURL = context.getProtectionDomain().getCodeSource().getLocation();
		if ("location:local".equals(jarFileURL.toString()) || jarFileURL.toString().startsWith("mvn:"))
			throw new RuntimeException("expected file URI");
		File jarFile = new File(URLs.asURI(jarFileURL));
		if (!jarFile.exists())
			throw new RuntimeException("coding error");
		return jarFile;
	}

	/**
	 * Closing the file system of the returned path is the responsibility of the caller.
	 */
	public static Path getResourceFromJAR(String resource, Path jarFilePath) {
		if (resource.startsWith("/"))
			resource = resource.substring(1);
		boolean requestedDirectory = resource.endsWith("/");
		if (requestedDirectory)
			resource = resource.substring(0, resource.length() - 1);
		Path f = null; {
			if (isDirectory(jarFilePath))
				f = jarFilePath.resolve(resource);
			else {
				FileSystem fs; {
					try {
						fs = FileSystems.newFileSystem(jarFilePath, (ClassLoader)null); }
					catch (IOException e) {
						throw new RuntimeException(e); }}
				try {
					f = fs.getPath("/" + resource);
				} finally {
					if (f == null)
						try {
							fs.close(); }
						catch (IOException e) {
							throw new RuntimeException(e); }
				}
			}
		}
		if (!Files.exists(f))
			throw new RuntimeException("file does not exist: " + resource);
		else if (requestedDirectory && !isDirectory(f))
			throw new RuntimeException("not a directory: " + resource);
		else
			return f;
	}
	
	/**
	 * @param directory The (not URL-encoded) path of a directory inside the specified JAR or class directory
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
			if (!jarFile.exists())
				throw new RuntimeException("coding error");
			return listResourcesFromJAR(directory, jarFile.toPath());
		}
	}

	/**
	 * @param directory The (not URL-encoded) path of a directory inside the specified JAR or class directory
	 * @param jarFilePath The location of the JAR file in a file system. Does not need to be be representable by a {@link File}.
	 * @return A list of resource paths (not URL-encoded)
	 */
	public static Iterator<String> listResourcesFromJAR(String directory, Path jarFilePath) {
		if (directory.startsWith("/"))
			directory = directory.substring(1);
		if (directory.endsWith("/"))
			directory = directory.substring(0, directory.length() - 1);
		final String _directory = directory;
		final ImmutableList.Builder<String> resources = ImmutableList.<String>builder();
		Path d;
		FileSystem fs = null;
		try {
			if (isDirectory(jarFilePath))
				d = jarFilePath.resolve(directory);
			else {
				try {
					fs = FileSystems.newFileSystem(jarFilePath, (ClassLoader)null); }
				catch (IOException e) {
					throw new RuntimeException(e); }
				d = fs.getPath("/" + directory);
			}
			if (!Files.exists(d))
				throw new RuntimeException("file does not exist: " + directory);
			else if (!isDirectory(d))
				throw new RuntimeException("not a directory: " + directory);
			else {
				try {
					Files.walkFileTree(d, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
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
				return resources.build().iterator();
			}
		} finally {
			if (fs != null)
				try {
					fs.close(); }
				catch (IOException e) {
					throw new RuntimeException(e); }
		}
	}
	
	/**
	 * @throws RuntimeException if the file does not exist
	 */
	private static boolean isDirectory(Path p) throws RuntimeException {
		BasicFileAttributes a; {
			try {
				a = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes(); }
			catch (NoSuchFileException e) {
				throw new RuntimeException("file does not exist: " + p); }
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
						throw new RuntimeException("not a directory: " + resource); }
				else {
					url = bundle.getEntry(resource + "/");
					if (url != null)
						return asURL(encode(url)); }
				throw new RuntimeException("file does not exist: " + resource); }
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
					throw new RuntimeException("not a directory: " + directory);
				else
					throw new RuntimeException("file does not exist: " + directory); }
			else
				return Iterators.forEnumeration(resources);
		}
	}
}
