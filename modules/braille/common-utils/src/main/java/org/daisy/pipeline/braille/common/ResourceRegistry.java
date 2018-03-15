package org.daisy.pipeline.braille.common;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.daisy.pipeline.braille.common.ResourceResolver.util.MemoizingResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceRegistry<T extends ResourcePath> implements ResourceResolver {
	
	protected void register(T path) {
		if (paths.containsKey(path.getIdentifier()))
			throw new RuntimeException("Resource registry already contains resource path with identifier " + path.getIdentifier());
		paths.put(path.getIdentifier(), path);
		resolver.invalidateCache();
		logger.debug("Adding resource path to registry: {}", path.getIdentifier());
	}
	
	protected void unregister(T path) {
		paths.remove(path.getIdentifier());
		resolver.invalidateCache();
		logger.debug("Removing resource path from registry: {}", path.getIdentifier());
	}
	
	protected final Map<URI,T> paths = new HashMap<URI,T>();
	
	/*
	 * Iterate over all registered resource paths and return as soon as one
	 * path can resolve the resource.
	 */
	protected final MemoizingResolver resolver = new MemoizingResolver() {
		public URL _apply(URI resource) {
			for (T path : paths.values()) {
				URL resolved = path.resolve(resource);
				if (resolved != null)
					return resolved; }
			return null;
		}
	};
	
	public URL resolve(URI resource) {
		return resolver.resolve(resource);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceRegistry.class);
}
