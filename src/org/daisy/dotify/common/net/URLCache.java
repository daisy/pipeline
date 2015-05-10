package org.daisy.dotify.common.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.daisy.dotify.common.io.FileIO;

public class URLCache {
	private final File root;
	
	public URLCache() {
		this(new File(System.getProperty("java.io.tmpdir"), "url-cache"));
	}

	public URLCache(File root) {
		this.root = root;
	}

	public InputStream openStream(URL url) throws IOException {
		File f;
		if (url.getProtocol().equals("file")) {
			try {
				f = new File(url.toURI());
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
		} else {
			f = getCacheFile(url);
			if (f==null) {
				return null;
			}
		}
		return new FileInputStream(f);
	}
	
	/**
	 * Updates the cache file
	 * @param url the url to update
	 * @return the cache file
	 * @throws IOException if download failed
	 */
	public File updateCacheFile(URL url) throws IOException {
		return getCacheFile(url, true);
	}
	
	/**
	 * Gets the cache file for the specified url
	 * @param url the url to get the file for
	 * @return returns the file, or null if the file isn't in the cache
	 * @throws IOException if download failed
	 */
	public File getCacheFile(URL url) throws IOException  {
		return getCacheFile(url, false);
	}
	
	/**
	 * Returns true if the entry is in the cache
	 * @param url
	 * @return returns true if the entry is in the cache
	 */
	public boolean hasEntry(URL url) {
		File f = toPath(url);
		return (f!=null ? f.exists() : false);
	}
	
	/**
	 * Removes the specified url from the cache.
	 * @param url the url to remove from the cache
	 * @return returns true if the entry was successfully removed
	 */
	public boolean removeEntry(URL url) {
		File f = toPath(url);
		if (f!=null) {
			return f.delete(); 
		} else {
			return false;
		}
	}

	private File getCacheFile(URL url, boolean overwrite) throws IOException {
		File f = toPath(url);
		if (f==null) {
			return null;
		}
		if (overwrite || !f.exists()) {
			//Download a copy
			f.getParentFile().mkdirs();
			FileOutputStream os = new FileOutputStream(f);
			FileIO.copy(url.openStream(), os);
		}
		return f;
	}

	private File toPath(URL url) {
		if (url.getProtocol()==null || url.getHost()==null || url.getPath()==null || 
				url.getProtocol().equals("") || url.getHost().equals("") || url.getPath().equals("")) {
			return null;
		}
		ArrayList<String> list = new ArrayList<String>();
		list.add(url.getProtocol());
		list.add(url.getHost());
		list.add(url.getPath());
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : list) {
			if (first) {
				first = false;
			} else {
				sb.append("/");
			}
			sb.append(s);
		}
		return new File(root, sb.toString());
	}
}
