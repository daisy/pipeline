package org.daisy.pipeline.braille.common;

import java.io.File;
import java.net.URI;
import java.net.URL;
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
import com.google.common.collect.Iterators;

import static org.daisy.pipeline.braille.common.util.Iterators.partition;
import static org.daisy.pipeline.braille.common.util.Predicates.matchesGlobPattern;
import static org.daisy.pipeline.braille.common.util.Tuple2;
import static org.daisy.pipeline.braille.common.util.Files.normalize;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import static org.daisy.pipeline.braille.common.util.URLs.decode;

import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

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
	private ComponentContext context = null;
	
	/* The included resources as relative paths */
	private Collection<URI> resources = null;
	
	protected void activate(ComponentContext context, final Map<?,?> properties) throws Exception {
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
			unpacking = true;
		if (properties.get(EXECUTABLES) != null && (Boolean)properties.get(EXECUTABLES))
			executables = true;
		this.context = context;
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
			File directory;
			for (int i = 0; true; i++) {
				directory = context.getBundleContext().getDataFile("resources" + i);
				if (!directory.exists()) break; }
			directory.mkdirs();
			unpackDir = normalize(directory); }
		return unpackDir;
	}
	
	protected boolean isExecutable(URI resources) {
		return executables;
	}
}
