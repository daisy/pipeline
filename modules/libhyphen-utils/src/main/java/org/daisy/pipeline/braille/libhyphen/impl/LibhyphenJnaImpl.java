package org.daisy.pipeline.braille.libhyphen.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import ch.sbs.jhyphen.CompilationException;
import ch.sbs.jhyphen.Hyphen;
import ch.sbs.jhyphen.Hyphenator;
import ch.sbs.jhyphen.StandardHyphenationException;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.cache.CacheBuilder;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultFullHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.debug;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.fromNullable;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.intersection;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.of;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import org.daisy.pipeline.braille.common.HyphenatorProvider;
import org.daisy.pipeline.braille.common.NativePath;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.Files.isAbsoluteFile;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import org.daisy.pipeline.braille.common.WithSideEffect;
import org.daisy.pipeline.braille.libhyphen.LibhyphenHyphenator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a <a href="http://hunspell.github.io/">Hyphen</a> based {@link
 * org.daisy.pipeline.braille.common.Hyphenator} implementation.
 *
 * @see <a href="../../../../../../../../../doc/">User documentation</a>.
 */
@Component(
	name = "org.daisy.pipeline.braille.libhyphen.LibhyphenJnaImpl",
	service = {
		LibhyphenHyphenator.Provider.class,
		HyphenatorProvider.class
	}
)
public class LibhyphenJnaImpl extends AbstractTransformProvider<LibhyphenHyphenator>
	                          implements LibhyphenHyphenator.Provider {
	
	private LibhyphenTableRegistry tableRegistry;
	
	@Activate
	protected void activate() {
		logger.debug("Loading libhyphen service");
	}
	
	@Deactivate
	protected void deactivate() {
		logger.debug("Unloading libhyphen service");
	}
	
	@Reference(
		name = "LibhyphenLibrary",
		unbind = "-",
		service = NativePath.class,
		target = "(identifier=http://hunspell.sourceforge.net/Hyphen/native/*)",
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindLibrary(NativePath path) {
		URI libraryPath = path.get("libhyphen").iterator().next();
		Hyphen.setLibraryPath(asFile(path.resolve(libraryPath)));
		logger.debug("Registering libhyphen library: " + libraryPath);
	}
	
	@Reference(
		name = "LibhyphenTableRegistry",
		unbind = "-",
		service = LibhyphenTableRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableRegistry(LibhyphenTableRegistry registry) {
		tableRegistry = registry;
		logger.debug("Registering libhyphen table registry: " + registry);
	}
	
	private final static Iterable<LibhyphenHyphenator> empty
	= Iterables.<LibhyphenHyphenator>empty();
	
	protected final Iterable<LibhyphenHyphenator> _get(Query query) {
		MutableQuery q = mutableQuery(query);
		if (q.containsKey("hyphenator")) {
			String v = q.removeOnly("hyphenator").getValue().get();
			if (!("hyphen".equals(v) || "libhyphen".equals(v))) {
				Iterable<LibhyphenHyphenator> ret;
				LibhyphenHyphenator h = fromId(v);
				if (h != null)
					ret = fromNullable(h);
				else
					ret = of(
						new WithSideEffect<LibhyphenHyphenator,Logger>() {
							public LibhyphenHyphenator _apply() {
								LibhyphenTable t = __apply(libhyphenTableProvider.get(URLs.asURI(v)));
								return __apply(logCreate(new LibhyphenHyphenatorImpl(t, null))); }});
				if (q.isEmpty())
					return ret;
				else
					return intersection(_get(q), ret); }}
		String tableKey;
		String table; {
			if (q.containsKey("libhyphen-table")) {
				table = q.removeOnly("libhyphen-table").getValue().get();
				tableKey = "libhyphen-table";
				if (q.containsKey("hyphen-table")) {
					logger.warn("A query with both 'libhyphen-table' and 'hyphen-table' never matches anything");
					return empty; }
				else if (q.containsKey("table")) {
					logger.warn("A query with both 'libhyphen-table' and 'table' never matches anything");
					return empty; }
			} else if (q.containsKey("hyphen-table")) {
				table = q.removeOnly("hyphen-table").getValue().get();
				tableKey = "hyphen-table";
				if (q.containsKey("table")) {
					logger.warn("A query with both 'hyphen-table' and 'table' never matches anything");
					return empty; }
			} else if (q.containsKey("table")) {
				table = q.removeOnly("table").getValue().get();
				tableKey = "table";
			} else {
				table = null;
				tableKey = null;
			}
		}
		Locale locale; {
			String loc = "und";
			if (q.containsKey("document-locale"))
				loc = q.removeOnly("document-locale").getValue().get();
			try {
				locale = parseLocale(loc); }
			catch (IllegalArgumentException e) {
				logger.error("Invalid locale", e);
				return empty; }
		}
		if (table != null) {
			if (!q.isEmpty()) {
				logger.warn("A query with both '" + tableKey + "' and '"
				            + q.iterator().next().getKey() + "' never matches anything");
				return empty; }
			return of(
				new WithSideEffect<LibhyphenHyphenator,Logger>() {
					public LibhyphenHyphenator _apply() {
						LibhyphenTable t = __apply(libhyphenTableProvider.get(URLs.asURI(table)));
						return __apply(logCreate(new LibhyphenHyphenatorImpl(t, null))); }}); }
		if (UND.equals(locale))
			return of(
				new WithSideEffect<LibhyphenHyphenator,Logger>() {
					public LibhyphenHyphenator _apply() {
						return __apply(logCreate(new LibhyphenHyphenatorImpl(null, UND))); }});
		return transform(
			libhyphenTableProvider.get(locale),
			new Function<LibhyphenTable,LibhyphenHyphenator>() {
				public LibhyphenHyphenator _apply(LibhyphenTable table) {
					return __apply(logCreate(new LibhyphenHyphenatorImpl(table, locale))); }});
	}
	
	private class LibhyphenTable {
		private URI table;
		private Hyphenator hyphenator;
		private LibhyphenTable(URI table) throws CompilationException, FileNotFoundException {
			this.table = table;
			this.hyphenator = compileTable(table);
		}
	}
	
	private final LibhyphenTableProvider libhyphenTableProvider = new LibhyphenTableProvider();
	
	private class LibhyphenTableProvider {
		public WithSideEffect<LibhyphenTable,Logger> get(final URI table) {
			try {
				return WithSideEffect.of(new LibhyphenTable(table)); }
			catch (CompilationException|FileNotFoundException e) {
				return new WithSideEffect<LibhyphenTable,Logger>() {
					public LibhyphenTable _apply() throws NoSuchElementException {
						__apply(debug("Could not create hyphenator for table " + table));
						throw new NoSuchElementException();
					}
				};
			}
		}
		public Iterable<LibhyphenTable> get(Locale locale) {
			return transform(
				tableRegistry.get(locale),
				new Function<URI,LibhyphenTable>() {
					public LibhyphenTable _apply(URI table) {
						return __apply(get(table)); }});
		}
	}

	private final static Locale UND = parseLocale("und");
	
	private class LibhyphenHyphenatorImpl extends AbstractHyphenator implements LibhyphenHyphenator {
		
		private final Locale mainLanguage;
		private final LibhyphenTable mainTable;
		private final org.daisy.pipeline.braille.common.Provider<Locale,WithSideEffect<LibhyphenTable,Logger>> subHyphenators;
		
		/**
		 * @param table        When {@code null}, there is no main hyphenation table. Each language
		 *                     is handled with an appropriate table.
		 * @param mainLanguage {@code null} means that the main hyphenation table (which may not be
		 *                     {@code null}) should be used to handle all languages.
		 */
		private LibhyphenHyphenatorImpl(LibhyphenTable table, Locale mainLanguage) {
			if (table == null && mainLanguage == null)
				throw new IllegalArgumentException();
			this.mainLanguage = mainLanguage;
			this.mainTable = table;
			this.subHyphenators = memoize(libhyphenTableProvider::get);
		}
		
		public URI asLibhyphenTable() {
			return mainTable != null ? mainTable.table : null;
		}
		
		@Override
		public FullHyphenator asFullHyphenator() {
			return fullHyphenator;
		}
		
		private final FullHyphenator fullHyphenator = new DefaultFullHyphenator() {
			protected boolean isCodePointAware() { return false; }
			protected boolean isLanguageAdaptive() { return true; }
			protected byte[] getHyphenationOpportunities(String textWithoutHyphens, Locale language)
					throws NonStandardHyphenationException, RuntimeException {
				LibhyphenTable table = getTable(language);
				if (table == null)
					// note that breaking after hard hyphens is handled by DefaultFullHyphenator
					return null;
				try {
					return table.hyphenator.hyphenate(textWithoutHyphens);
				} catch (StandardHyphenationException e) {
					throw new NonStandardHyphenationException(e);
				} catch (Exception e) {
					throw new RuntimeException("Error during libhyphen hyphenation", e);
				}
			}
			@Override
			public String toString() {
				return LibhyphenHyphenatorImpl.this.toString();
			}
		};
		
		// FIXME: this LineBreaker does not consider SHY or ZWSP characters in the input
		@Override
		public LineBreaker asLineBreaker() {
			return lineBreaker;
		}
		
		private final LineBreaker lineBreaker = new DefaultLineBreaker() {
			protected Break breakWord(String word, Locale language, int limit, boolean force) {
				LibhyphenTable table = getTable(language);
				if (table != null) {
					Hyphenator.Break br = table.hyphenator.hyphenate(word, limit);
					if (force && br.getBreakPosition() == 0)
						return new Break(word, limit, false);
					else
						return new Break(br.getText(), br.getBreakPosition(), br.hasHyphen());
				}
				return super.breakWord(word, language, limit, force);
			}
			@Override
			public String toString() {
				return LibhyphenHyphenatorImpl.this.toString();
			}
		};
		
		private LibhyphenTable getTable(Locale language) {
			if (mainLanguage == null || mainLanguage.equals(language))
				return mainTable;
			else if (language == null || UND.equals(language))
				return null;
			else
				try {
					return subHyphenators.get(language).iterator().next().apply(logger);
				} catch (NoSuchElementException e) {
					logger.warn("No hyphenator for language " + language);
					return null;
				}
		}
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("LibhyphenJnaImpl$LibhyphenHyphenatorImpl")
				.add("language", mainLanguage)
				.add("table", asLibhyphenTable());
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null)
				return false;
			if (getClass() != o.getClass())
				return false;
			LibhyphenHyphenatorImpl that = (LibhyphenHyphenatorImpl)o;
			if (this.mainTable == null) {
				if (that.mainTable != null)
					return false;
			} else if (that.mainTable == null)
				return false;
			else if (!this.mainTable.table.equals(that.mainTable.table))
				return false;
			return true;
		}
	}
	
	private Hyphenator compileTable(URI table) throws FileNotFoundException, CompilationException {
		if ("volatile-file".equals(table.getScheme()))
			try {
				table = new URI("file", table.getSchemeSpecificPart(), table.getFragment());
			} catch (Exception e) {
				// should not happen
				throw new IllegalStateException(e);
			}
		ModifiedFile tableFile = new ModifiedFile(resolveTable(table));
		Hyphenator hyphenator = tableCache.get(tableFile);
		if (hyphenator == null) {
			hyphenator = new Hyphenator(tableFile.file);
			tableCache.put(tableFile, hyphenator);
		}
		return hyphenator;
	}
	
	private File resolveTable(URI table) throws FileNotFoundException {
		URL resolvedTable = isAbsoluteFile(table) ? URLs.asURL(table) : tableRegistry.resolve(table);
		if (resolvedTable == null)
			throw new FileNotFoundException("Hyphenation table " + table + " could not be resolved");
		return asFile(resolvedTable);
	}

	private final Map<ModifiedFile,Hyphenator> tableCache
		= CacheBuilder.newBuilder()
		              .expireAfterAccess(300, TimeUnit.SECONDS)
		              .<ModifiedFile,Hyphenator>build()
		              .asMap();

	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper("LibhyphenJnaImpl");
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LibhyphenJnaImpl.class);
	
	private static class ModifiedFile {
		
		public final File file;
		public final FileTime lastModifiedTime;
		
		public ModifiedFile(File file) {
			this.file = file;
			try {
				BasicFileAttributes attrs = Files.readAttributes(
					file.toPath(),
					BasicFileAttributes.class);
				this.lastModifiedTime = attrs.lastModifiedTime();
			} catch (IOException e) {
				throw new RuntimeException(e); // should not happen
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + file.hashCode();
			result = prime * result + lastModifiedTime.hashCode();
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ModifiedFile other = (ModifiedFile)obj;
			if (!file.equals(other.file))
				return false;
			if (!lastModifiedTime.equals(other.lastModifiedTime))
				return false;
			return true;
		}
	}
}
