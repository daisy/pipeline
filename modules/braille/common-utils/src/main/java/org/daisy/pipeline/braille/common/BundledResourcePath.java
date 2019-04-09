package org.daisy.pipeline.braille.common;

import java.io.File;
import java.net.URI;
import java.net.URL;
import static java.nio.file.Files.createTempDirectory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import static com.google.common.base.Functions.toStringFunction;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import static com.google.common.base.Predicates.alwaysTrue;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import static org.daisy.common.file.URIs.asURI;
import static org.daisy.common.file.URIs.relativize;
import org.daisy.common.file.URLs;
import static org.daisy.pipeline.braille.common.util.Iterators.partition;
import static org.daisy.pipeline.braille.common.util.Predicates.matchesGlobPattern;
import static org.daisy.pipeline.braille.common.util.Tuple2;
import static org.daisy.pipeline.braille.common.util.Files.normalize;

public class BundledResourcePath extends AbstractResourcePath {
	
	protected static final String IDENTIFIER = "identifier";
	protected static final String PATH = "path";
	protected static final String UNPACK = "unpack";
	protected static final String EXECUTABLES = "executables";
	protected static final String INCLUDES = "includes";
	
	private URI identifier = null;
	private URL path = null;
	private File unpackDir = null;
	private boolean unpacking = false;
	private boolean executables = false;
	
	/* The included resources as relative paths */
	private Collection<URI> resources = null;
	
	protected void activate(final Map<?,?> properties, Class<?> context) throws IllegalArgumentException {
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
		String pathAsRelativeFilePath = properties.get(PATH).toString();
		path = URLs.getResourceFromJAR(pathAsRelativeFilePath, context);
		if (path == null)
			throw new IllegalArgumentException("Resource path at location " + pathAsRelativeFilePath + " could not be found");
		final Predicate<Object> includes =
			(properties.get(INCLUDES) != null
			 && !properties.get(INCLUDES).toString().isEmpty()
			 && !properties.get(INCLUDES).toString().equals("*")) ?
				Predicates.compose(
					matchesGlobPattern(properties.get(INCLUDES).toString()),
					Functions.compose(URLs::decode, toStringFunction())) :
				alwaysTrue();
		Function<String,Collection<String>> getFilePaths = new Function<String,Collection<String>>() {
			public Collection<String> apply(String path) {
				Tuple2<Collection<String>,Collection<String>> entries = partition(
					URLs.listResourcesFromJAR(path, context),
					s -> s.endsWith("/"));
				Collection<String> files = new ArrayList<String>();
				files.addAll(entries._2);
				for (String folder : entries._1) files.addAll(apply(folder));
				return files; }};
		resources = new ImmutableList.Builder<URI>()
			.addAll(
				Collections2.<URI>filter(
					Collections2.<String,URI>transform(
						getFilePaths.apply(pathAsRelativeFilePath),
						s -> relativize(path, URLs.getResourceFromJAR(s, context))),
					includes))
			.build();
		if (properties.get(UNPACK) != null && (Boolean)properties.get(UNPACK))
			unpacking = true;
		if (properties.get(EXECUTABLES) != null && (Boolean)properties.get(EXECUTABLES))
			executables = true;
	}
	
	public URI getIdentifier() {
		return identifier;
	}
	
	protected boolean containsResource(URI resource) {
		return resources.contains(resource);
	}
	
	protected Collection<URI> listResources() {
		return resources;
	}
	
	protected URL getBasePath() {
		return path;
	}
	
	protected boolean isUnpacking() {
		return unpacking;
	}
	
	@Override
	protected File makeUnpackDir() {
		if (!unpacking)
			return null;
		if (unpackDir == null) {
			File tmpDirectory; {
				try {
					tmpDirectory = createTempDirectory("pipeline-").toFile(); }
				catch (Exception e) {
					throw new RuntimeException("Could not create temporary directory", e); }
				tmpDirectory.deleteOnExit();
			}
			unpackDir = normalize(tmpDirectory); }
		return unpackDir;
	}
	
	protected boolean isExecutable(URI resources) {
		return executables;
	}
}
