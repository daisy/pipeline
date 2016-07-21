package org.daisy.pipeline.braille.common;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import static com.google.common.base.Functions.toStringFunction;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import static com.google.common.base.Predicates.alwaysTrue;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import static org.daisy.pipeline.braille.common.util.Files;
import static org.daisy.pipeline.braille.common.util.Iterators.partition;
import static org.daisy.pipeline.braille.common.util.Predicates.matchesGlobPattern;
import static org.daisy.pipeline.braille.common.util.Tuple2;
import static org.daisy.pipeline.braille.common.util.URIs;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.Files.normalize;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import static org.daisy.pipeline.braille.common.util.URLs.asURL;
import static org.daisy.pipeline.braille.common.util.URLs.decode;

import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BundledResourcePath implements ResourcePath {
	
	protected static final String IDENTIFIER = "identifier";
	protected static final String PATH = "path";
	protected static final String UNPACK = "unpack";
	protected static final String INCLUDES = "includes";
	
	protected URI identifier = null;
	protected URL path = null;
	protected File unpackDir = null;
	
	/* The included resources as relative paths */
	protected Collection<URI> resources = null;
	
	/**
	 * {@inheritDoc}
	 */
	public URI getIdentifier() {
		return identifier;
	}
	
	private final ResourceResolver resolver = new util.MemoizingResolver() {
		public URL _apply(URI resource) {
			logger.trace("Resolving " + resource + " within " + identifier + " (real path: " + path + "; unpack dir: " + unpackDir + ")");
			resource = resource.normalize();
			if (resource.equals(identifier) || resource.equals(path) || resource.equals(unpackDir))
				return maybeUnpack(asURI("."));
			URI relativePath = resource;
			if (relativePath.isAbsolute()) {
				relativePath = identifier.relativize(resource);
				if (relativePath.isAbsolute()) {
					relativePath = asURI(path).relativize(resource);
					if (relativePath.isAbsolute() && unpackDir != null) {
						relativePath = asURI(unpackDir).relativize(resource); }}}
			if (resources.contains(relativePath))
				return maybeUnpack(relativePath);
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
		return identifier.resolve(URIs.relativize(unpackDir != null ? unpackDir : path, resolved));
	}
	
	protected void activate(ComponentContext context, final Map<?, ?> properties) throws Exception {
		if (properties.get(IDENTIFIER) == null || properties.get(IDENTIFIER).toString().isEmpty()) {
			throw new IllegalArgumentException(IDENTIFIER + " property must not be empty"); }
		String identifierAsString = properties.get(IDENTIFIER).toString();
		if (!identifierAsString.endsWith("/")) identifierAsString += "/";
		try { identifier = asURI(identifierAsString); }
		catch (Exception e) {
			throw new IllegalArgumentException(IDENTIFIER + " could not be parsed into a URI"); }
		if (!identifier.isAbsolute())
			throw new IllegalArgumentException(IDENTIFIER + " must be an absolute URI");
		if (properties.get(PATH) == null || properties.get(PATH).toString().isEmpty()) {
			throw new IllegalArgumentException(PATH + " property must not be empty"); }
		final Bundle bundle = context.getBundleContext().getBundle();
		String pathAsRelativeFilePath = properties.get(PATH).toString();
		if (!pathAsRelativeFilePath.endsWith("/")) pathAsRelativeFilePath += "/";
		path = bundle.getEntry(pathAsRelativeFilePath);
		if (path == null)
			throw new IllegalArgumentException("Resource path at location " + pathAsRelativeFilePath + " could not be found");
		final Predicate<Object> includes =
			(properties.get(INCLUDES) != null && !properties.get(INCLUDES).toString().isEmpty()) ?
				Predicates.compose(
					matchesGlobPattern(properties.get(INCLUDES).toString()),
					Functions.compose(decode, toStringFunction())) :
				alwaysTrue();
		Function<String,Collection<String>> getFilePaths = new Function<String,Collection<String>>() {
			public Collection<String> apply(String path) {
				Tuple2<Collection<String>,Collection<String>> entries = partition(
					Iterators.<String>forEnumeration(bundle.getEntryPaths(path)),
					new Predicate<String>() { public boolean apply(String s) { return s.endsWith("/"); }});
				Collection<String> files = new ArrayList<String>();
				files.addAll(entries._2);
				for (String folder : entries._1) files.addAll(apply(folder));
				return files; }};
		resources = new ImmutableList.Builder<URI>()
			.addAll(
				Collections2.<URI>filter(
					Collections2.<String,URI>transform(
						getFilePaths.apply(pathAsRelativeFilePath),
						new Function<String,URI>() {
							public URI apply(String s) {
								return asURI(path).relativize(asURI(bundle.getEntry(s))); }}),
					includes))
			.build();
		if (properties.get(UNPACK) != null && (Boolean)properties.get(UNPACK))
			lazyUnpack(context);
	}
	
	protected URL maybeUnpack(URI resource) {
		return maybeUnpack.apply(resource);
	}
	
	private Function<URI,URL> maybeUnpack = new Function<URI,URL>() {
		public URL apply(URI resource) {
			return asURL(asURI(path).resolve(resource));
		}
	};
	
	protected void unpack(URL url, File file) {
		Files.unpack(url, file);
	}
	
	private void makeUnpackDir(ComponentContext context) {
		if (unpackDir == null) {
			File directory;
			for (int i = 0; true; i++) {
				directory = context.getBundleContext().getDataFile("resources" + i);
				if (!directory.exists()) break; }
			directory.mkdirs();
			unpackDir = normalize(directory); }
	}
	
	protected void lazyUnpack(final ComponentContext context) {
		maybeUnpack = new Function<URI,URL>() {
			private Map<URI,File> unpacked = new HashMap<URI,File>();
			public URL apply(URI resource) {
				File file = unpacked.get(resource);
				if (file == null) {
					makeUnpackDir(context);
					if (".".equals(resource.toString())) {
						for (URI r : resources)
							apply(r);
						file = unpackDir; }
					else {
						file = asFile(asURI(unpackDir).resolve(resource.toString()));
						unpack(asURL(asURI(path).resolve(resource)), file); }
					unpacked.put(resource, file); }
				return asURL(file);
			}
		};
	}
	
	private static final Logger logger = LoggerFactory.getLogger(BundledResourcePath.class);
	
	@Override
	public String toString() {
		return identifier.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;
		hash = prime * hash + ((identifier == null) ? 0 : identifier.hashCode());
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
		if (!this.identifier.equals(that.getIdentifier()))
			return false;
		return true;
	}
}
