package org.daisy.pipeline.braille.liblouis.impl;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.ResourcePath;
import org.daisy.pipeline.braille.common.ResourceRegistry;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.Files.fileName;
import static org.daisy.pipeline.braille.common.util.Predicates.matchesGlobPattern;
import org.daisy.pipeline.braille.liblouis.LiblouisTable;
import org.daisy.pipeline.braille.liblouis.LiblouisTablePath;
import org.daisy.pipeline.braille.liblouis.LiblouisTableResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisTableRegistry",
	service = {
		LiblouisTableRegistry.class,
		LiblouisTableResolver.class
	}
)
public class LiblouisTableRegistry extends ResourceRegistry<LiblouisTablePath> implements LiblouisTableResolver {
	
	@Reference(
		name = "LiblouisTablePath",
		unbind = "_unregister",
		service = LiblouisTablePath.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void _register(LiblouisTablePath path) {
		register(path);
		applyPathChangeCallbacks();
	}
	
	protected void _unregister(LiblouisTablePath path) {
		unregister(path);
		applyPathChangeCallbacks();
	}
	
	@Override
	public URL resolve(URI resource) {
		URL resolved = super.resolve(resource);
		if (resolved == null)
			resolved = fileSystem.resolve(resource);
		return resolved;
	}
	
	public File[] resolveLiblouisTable(LiblouisTable table, File base) {
		URI[] subTables = table.asURIs();
		File[] tableFiles = new File[subTables.length];
		List<ResourcePath> paths = new ArrayList<ResourcePath>(this.paths.values());
		paths.add(fileSystem);
		for (int i = 0; i < subTables.length; i++) {
			URI subTable = subTables[i];
			if (base != null)
				subTable = URLs.resolve(URLs.asURI(base), subTable);
			for (ResourcePath path : paths) {
				tableFiles[i] = asFile(path.resolve(subTable));
				if (tableFiles[i] != null) {
					logger.trace("Table " + subTable + " was found in " + path);
					// moving path the first position, so that it will be tried first next time
					paths.remove(path);
					paths.add(0, path);
					break; }}
			if (tableFiles[i] == null)
				return null; }
		return tableFiles;
	}
	
	private final ResourcePath fileSystem = new LiblouisFileSystem();
	
	private static class LiblouisFileSystem implements ResourcePath {

		private static final URI identifier = URLs.asURI("file:/");
		
		private static final Predicate<String> isLiblouisTable = matchesGlobPattern("*.{dis,ctb,cti,uti,utb,dic,tbl}");
		
		public URI getIdentifier() {
			return identifier;
		}
		
		public URL resolve(URI resource) {
			try {
				resource = resource.normalize();
				resource = identifier.resolve(resource);
				File file = asFile(resource);
				if (file.exists() && isLiblouisTable.apply(fileName(file)))
					return URLs.asURL(resource); }
			catch (Exception e) {}
			return null;
		}
		
		public URI canonicalize(URI resource) {
			return URLs.asURI(resolve(resource));
		}
		
		@Override
		public String toString() {
			return identifier.toString();
		}
	}
	
	private Collection<Function<LiblouisTableRegistry,Void>> pathChangeCallbacks
		= new ArrayList<Function<LiblouisTableRegistry,Void>>();
	
	public void onPathChange(Function<LiblouisTableRegistry,Void> callback) {
		pathChangeCallbacks.add(callback);
	}
	
	private void applyPathChangeCallbacks() {
		for (Function<LiblouisTableRegistry,Void> f : pathChangeCallbacks)
			try {
				f.apply(this); }
			catch (RuntimeException e) {
				logger.error("Could not apply callback function " + f, e); }
	}
	
	private static Function<LiblouisTablePath,Iterable<URI>> listTableFiles = new Function<LiblouisTablePath,Iterable<URI>>() {
		public Iterable<URI> apply(LiblouisTablePath path) {
			return path.listTableFiles();
		}
	};
	
	public Iterable<URI> listAllTableFiles() {
		return concat(
			transform(
				paths.values(),
				listTableFiles));
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LiblouisTableRegistry.class);
	
}
