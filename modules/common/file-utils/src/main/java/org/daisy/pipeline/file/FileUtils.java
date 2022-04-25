package org.daisy.pipeline.file;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.daisy.common.file.URLs;

/**
 * A collection of file/document/URI related utility functions.
 */
public final class FileUtils {

	private FileUtils() {}

	/**
	 * Create a <a href="https://www.w3.org/TR/xproc/#cv.result"><code>c:result</code></a> document
	 *
	 * @param text the value of the text node that the c:result element should contain
	 * @return the document as a Reader object
	 */
	public static Reader cResultDocument(String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("<c:result xmlns:c=\"http://www.w3.org/ns/xproc-step\">")
			.append(text)
			.append("</c:result>");
		return new StringReader(sb.toString());
	}

	/* ============= */
	/* URI functions */
	/* ============= */

	/**
	 * Relativize a URI against a base URI.
	 *
	 * This functions differs from {@link URI#relativize} in that it is also able to find
	 * relative paths when the URI does not start with the base URI (in which case the
	 * resulting URI will contain ".." segments).
	 */
	public static URI relativizeURI(URI uri, URI base) {
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

	public static URI normalizeURI(URI uri) {
		return normalizeURI(uri, false);
	}

	public static URI normalizeURI(URI uri, boolean dropFragment) {
		try {
			if (uri.isOpaque())
				return uri;
			if ("jar".equals(uri.getScheme()))
				return URLs.asURI("jar:" + normalizeURI(URLs.asURI(uri.toASCIIString().substring(4))).toASCIIString());
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
}
