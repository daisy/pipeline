package org.daisy.pipeline.braille.common;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.daisy.common.file.URIs;
import static org.daisy.common.file.URIs.asURI;
import static org.daisy.common.file.URLs.asURL;
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
						return asURL(maybeUnpack(asURI("."))); }
					catch (UnsupportedOperationException e) {
						return null; }
				else
					return getBasePath();
			URI relativePath = resource;
			if (relativePath.isAbsolute()) {
				relativePath = getIdentifier().relativize(resource);
				if (relativePath.isAbsolute()) {
					relativePath = asURI(getBasePath()).relativize(resource);
					if (relativePath.isAbsolute() && unpackDir != null) {
						relativePath = asURI(unpackDir).relativize(resource); }}}
			if (containsResource(relativePath))
				if (isUnpacking())
					return asURL(maybeUnpack(relativePath));
				else
					return asURL(URIs.resolve(getBasePath(), relativePath));
			if (!resource.toString().endsWith("/"))
				return resolve(asURI(resource.toString() + "/"));
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
		return getIdentifier().resolve(URIs.relativize(unpackDir != null ? unpackDir : getBasePath(), resolved));
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
			else
				file = asFile(URIs.resolve(unpackDir, resource));
			Files.unpack(asURL(URIs.resolve(getBasePath(), resource)), file);
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
