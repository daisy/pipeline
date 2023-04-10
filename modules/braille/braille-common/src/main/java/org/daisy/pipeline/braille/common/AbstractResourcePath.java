package org.daisy.pipeline.braille.common;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.daisy.common.file.URLs;
import static org.daisy.pipeline.braille.common.util.Files;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.OS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResourcePath implements ResourcePath {
	
	/**
	 * @param resource: relative URI
	 */
	protected abstract boolean containsResource(URI resource);
	
	protected Collection<URI> listResources() {
		throw new UnsupportedOperationException();
	}
	
	protected abstract URL getBasePath();
	
	protected abstract boolean isUnpacking();
	
	private File unpackDir;
	
	/**
	 * Override if isUnpacking returns true
	 */
	protected File makeUnpackDir() {
		throw new UnsupportedOperationException();
	}
	
	protected abstract boolean isExecutable(URI resource);
	
	private final ResourceResolver resolver = new util.MemoizingResolver() {
		public URL _apply(URI resource) {
			logger.trace("Resolving " + resource + " within " + getIdentifier() + " (real path: " + getBasePath() + "; unpack dir: " + unpackDir + ")");
			resource = resource.normalize();
			if (resource.equals(getIdentifier()))
				if (isUnpacking())
					try {
						// try to unpack whole path
						return URLs.asURL(maybeUnpack(URLs.asURI("."))); }
					catch (UnsupportedOperationException e) {
						return null; }
				else
					return getBasePath();
			URI relativePath = resource;
			if (relativePath.isAbsolute()) {
				relativePath = getIdentifier().relativize(resource);
				if (relativePath.isAbsolute()) {
					relativePath = URLs.relativize(URLs.asURI(getBasePath()), resource);
					if (relativePath.isAbsolute() && unpackDir != null) {
						relativePath = URLs.relativize(URLs.asURI(unpackDir), resource); }}}
			if (containsResource(relativePath))
				if (isUnpacking())
					return URLs.asURL(maybeUnpack(relativePath));
				else
					return URLs.asURL(URLs.resolve(URLs.asURI(getBasePath()), relativePath));
			if (!resource.toString().endsWith("/"))
				return resolve(URLs.asURI(resource.toString() + "/"));
			return null;
		}
	};
	
	/**
	 * Resolve a resource from a URI.
	 * @param resource The URI can be one of the following:
	 * - a relative URI, or
	 * - an absolute URI that can be relativized against, or equal to:
	 *   - the BundledResourcePath's identifier,
	 *   - the BundledResourcePath's actual path in the bundle, or
	 *   - the directory where resources are unpacked.
	 * @return The resolved URL, or null if the resource cannot be resolved.
	 *         The URL will be a file URL if the BundledResourcePath is "unpacking".
	 */
	public URL resolve(URI resource) {
		return resolver.resolve(resource);
	}
	
	public URI canonicalize(URI resource) {
		URL resolved = resolve(resource);
		if (resolved == null)
			return null;
		return URLs.resolve(getIdentifier(),
		                    unpackDir != null
		                        ? URLs.relativize(URLs.asURI(unpackDir), URLs.asURI(resolved))
		                        : URLs.relativize(URLs.asURI(getBasePath()), URLs.asURI(resolved)));
	}
	
	private Map<URI,File> unpacked = new HashMap<URI,File>();
	
	/**
	 * @param resource: relative URI
	 */
	private File maybeUnpack(URI resource) {
		File file = unpacked.get(resource);
		if (file == null) {
			if (unpackDir == null)
				unpackDir = makeUnpackDir();
			if (".".equals(resource.toString())) {
				for (URI r : listResources())
					maybeUnpack(r);
				file = unpackDir; }
			else {
				file = asFile(URLs.resolve(URLs.asURI(unpackDir), resource));
				// assuming that file is a temporary file
				// this assumption can be made because BundledResourcePath.makeUnpackDir() returns a temporary
				// directory, and BundledResourcePath is the only class that extends AbstractResourcePath
				LinkedList<File> deleteFilesOnExit = new LinkedList<>(); {
					File f = file;
					while (true) {
						if (f.equals(unpackDir))
							break;
						if (!f.toString().startsWith(unpackDir.toString() + File.separator))
							throw new IllegalStateException(); // file must be contained inside unpackDir
						deleteFilesOnExit.push(f);
						f = f.getParentFile();
					}
				}
				for (File f : deleteFilesOnExit)
					f.deleteOnExit();
			}
			Files.unpack(URLs.asURL(URLs.resolve(URLs.asURI(getBasePath()), resource)), file);
			if (isExecutable(resource) && !OS.isWindows())
				Files.chmod775(file);
			unpacked.put(resource, file); }
		return file;
	}
	
	@Override
	public String toString() {
		return getIdentifier().toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;
		hash = prime * hash + ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
		return hash;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		ResourcePath that = (ResourcePath)object;
		if (!this.getIdentifier().equals(that.getIdentifier()))
			return false;
		return true;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourcePath.class);
	
}
