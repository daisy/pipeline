package org.daisy.pipeline.braille.libhyphen.impl;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import com.google.common.base.Predicate;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import static org.daisy.pipeline.braille.common.Provider.util.varyLocale;
import org.daisy.pipeline.braille.common.ResourcePath;
import org.daisy.pipeline.braille.common.ResourceRegistry;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.Files.fileName;
import static org.daisy.pipeline.braille.common.util.Predicates.matchesGlobPattern;
import org.daisy.pipeline.braille.libhyphen.LibhyphenTablePath;
import org.daisy.pipeline.braille.libhyphen.LibhyphenTableProvider;
import org.daisy.pipeline.braille.libhyphen.LibhyphenTableResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "org.daisy.pipeline.braille.libhyphen.impl.LibhyphenTableRegistry",
	service = {
		LibhyphenTableRegistry.class
	}
)
public class LibhyphenTableRegistry extends ResourceRegistry<LibhyphenTablePath>
	                                implements LibhyphenTableProvider, LibhyphenTableResolver {
	
	@Reference(
		name = "LibhyphenTablePath",
		unbind = "-",
		service = LibhyphenTablePath.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	protected void _register(LibhyphenTablePath path) {
		register(path);
		provider.invalidateCache();
	}
	
	protected void _unregister (LibhyphenTablePath path) {
		unregister(path);
		provider.invalidateCache();
	}
	
	/**
	 * Try to find a table based on the given locale.
	 * An automatic fallback mechanism is used: if nothing is found for
	 * language-COUNTRY-variant, then language-COUNTRY is searched, then language.
	 */
	public Iterable<URI> get(Locale locale) {
		return provider.get(locale);
	}
	
	private final Provider.util.MemoizingProvider<Locale,URI> provider
		= memoize(
			varyLocale(
				dispatch(paths.values())));
	
	@Override
	public URL resolve(URI resource) {
		URL resolved = super.resolve(resource);
		if (resolved == null)
			resolved = fileSystem.resolve(resource);
		return resolved;
	}
	
	private final ResourcePath fileSystem = new LibhyphenFileSystem();
	
	private static class LibhyphenFileSystem implements ResourcePath {

		private static final URI identifier = URLs.asURI("file:/");
		
		private static final Predicate<String> isLibhyphenTable = matchesGlobPattern("hyph_*.dic");
		
		public URI getIdentifier() {
			return identifier;
		}
		
		public URL resolve(URI resource) {
			try {
				resource = resource.normalize();
				resource = URLs.resolve(identifier, resource);
				File file = asFile(resource);
				if (file.exists() && isLibhyphenTable.apply(fileName(file)))
					return URLs.asURL(resource); }
			catch (Exception e) {}
			return null;
		}
		
		public URI canonicalize(URI resource) {
			return URLs.asURI(resolve(resource));
		}
	}
}
