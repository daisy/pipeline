package org.daisy.pipeline.braille.liblouis.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static java.nio.file.Files.createTempDirectory;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Function;
import static com.google.common.base.Functions.toStringFunction;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.warn;
import org.daisy.pipeline.braille.common.NativePath;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.varyLocale;
import static org.daisy.pipeline.braille.common.util.Files.unpack;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.Files.normalize;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import org.daisy.pipeline.braille.common.WithSideEffect;
import org.daisy.pipeline.braille.liblouis.LiblouisTable;

import org.liblouis.CompilationException;
import org.liblouis.DisplayTable;
import org.liblouis.DisplayTable.Fallback;
import org.liblouis.Louis;
import org.liblouis.Table;
import org.liblouis.TableInfo;
import org.liblouis.TableResolver;
import org.liblouis.Translator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisTableJnaImplProvider",
	service = {
		LiblouisTableJnaImplProvider.class
	}
)
public class LiblouisTableJnaImplProvider extends AbstractTransformProvider<LiblouisTableJnaImplProvider.LiblouisTableJnaImpl> {

	// FIXME: isn't really a Transform but implements it so that we can use TransformProvider
	public class LiblouisTableJnaImpl extends LiblouisTable implements Transform {
		
		private final Translator translator;
		private final DisplayTable displayTable;
		private final TableInfo info;
		
		LiblouisTableJnaImpl(Translator translator, DisplayTable displayTable, TableInfo info) {
			super(translator.getTable());
			this.translator = translator;
			this.displayTable = displayTable;
			this.info = info;
		}
		
		public Translator getTranslator() {
			return translator;
		}
		
		public boolean usesCustomDisplayTable() {
			return displayTable != unicodeDisplayTable
				&& displayTable != unicodeDisplayTableWithNoBreakSpace;
		}
		
		public DisplayTable getDisplayTable() {
			return displayTable;
		}
		
		public String getIdentifier() {
			return toString();
		}
		
		public String getDisplayName() {
			return info != null ? info.get("display-name") : null;
		}
		
		public Normalizer.Form getUnicodeNormalizationForm() {
			if (info != null) {
				String form = info.get("unicode-form");
				if (form != null)
					try {
						return Normalizer.Form.valueOf(form.toUpperCase());
					} catch (IllegalArgumentException e) {}}
			return null;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper("LiblouisTableJnaImpl")
			                  .add("translator", super.toString())
			                  .add("displayTable", displayTable)
			                  .toString();
		}
	}
	
	private LiblouisTableRegistry tableRegistry;
	
	private DisplayTable unicodeDisplayTable;
	private DisplayTable unicodeDisplayTableWithNoBreakSpace;
	private File spacesFile;
	private File spacesDisFile;
	private File tempDir;
	
	private void registerTableResolver() {
		Louis.setTableResolver(new TableResolver() {
				private final Map<String,URL> aggregatorTables = new HashMap<String,URL>();
				@Override
				public URL resolve(String table, URL base) {
					logger.debug("Resolving " + table + (base != null ? " against base " + base : ""));
					// if we are resolving an include rule from a generated aggregator table, resolve without base
					if (aggregatorTables.containsValue(base))
						base = null;
					File baseFile = base == null ? null : asFile(base); // base is expected to be a file
					File[] resolved = tableRegistry.resolveLiblouisTable(new LiblouisTable(table), baseFile);
					if (resolved != null) {
						logger.debug("Resolved to " + join(resolved, ","));
						if (resolved.length == 1)
							return URLs.asURL(resolved[0]);
						else {
							// if it is a comma separated table list, create a single file that includes all the sub-tables
							if (aggregatorTables.containsKey(table)) {
								URL u = aggregatorTables.get(table);
								logger.debug("... aggregated into " + u);
								return u;
							}
							try {
								StringBuilder b = new StringBuilder();
								for (File f : resolved)
									b.append("include ").append(URLs.asURI(f.getCanonicalFile()).toASCIIString()).append('\n');
								InputStream in = new ByteArrayInputStream(b.toString().getBytes(StandardCharsets.UTF_8));
								File f = File.createTempFile("aggregator-", ".tbl", tempDir);
								f.deleteOnExit();
								f.delete();
								Files.copy(in, f.toPath());
								f = f.getCanonicalFile();
								URL u = URLs.asURL(f);
								aggregatorTables.put(table, u);
								logger.debug("... aggregated into " + u);
								return u;
							} catch (IOException e) {
								throw new RuntimeException(e); // should not happen
							}
						}
					}
					logger.debug("Table could not be resolved");
					return null;
				}
				@Override
				public Set<String> list() {
					return newHashSet(
						transform(
							tableRegistry.listAllTableFiles(),
							toStringFunction()));
				}
			}
		);
	}
	
	// WARNING: only one instance of LiblouisTableJnaImplProvider should be created because
	// setLibraryPath, setTableResolver and setLogger are global functions
	@Activate
	protected void activate() {
		logger.debug("Loading liblouis service");
		try {
			tempDir = normalize(createTempDirectory("pipeline-").toFile());
			tempDir.deleteOnExit();
		} catch (Exception e) {
			throw new RuntimeException("Could not create temporary directory", e);
		}
		try {
			tableRegistry.onPathChange(
				new Function<LiblouisTableRegistry,Void>() {
					public Void apply(LiblouisTableRegistry r) {
						// re-register table resolver so that liblouis-java re-indexes tables
						registerTableResolver();
						invalidateCache();
						return null; }});
			registerTableResolver();
			// invoke after table resolver registered because otherwise default tables will be unpacked for no reason
			logger.debug("liblouis version: {}", Louis.getVersion());
			File unicodeDisFile = new File(tempDir, "unicode.dis");
			unpack(
				URLs.getResourceFromJAR("/tables/unicode.dis", LiblouisTableJnaImplProvider.class),
				unicodeDisFile);
			unicodeDisFile.deleteOnExit();
			unicodeDisplayTable = DisplayTable.fromTable("" + URLs.asURI(unicodeDisFile), Fallback.MASK);
			spacesFile = new File(tempDir, "spaces.cti");
			unpack(
				URLs.getResourceFromJAR("/tables/spaces.cti", LiblouisTableJnaImplProvider.class),
				spacesFile);
			spacesFile.deleteOnExit();
			spacesDisFile = new File(tempDir, "spaces.dis");
			unpack(
				URLs.getResourceFromJAR("/tables/spaces.dis", LiblouisTableJnaImplProvider.class),
				spacesDisFile);
			spacesDisFile.deleteOnExit();
			unicodeDisplayTableWithNoBreakSpace = DisplayTable.fromTable(
				"" + URLs.asURI(unicodeDisFile) + "," + URLs.asURI(spacesDisFile), Fallback.MASK);
			Louis.setLogger(new org.liblouis.Logger() {
					@Override
					public void log(Level level, String message) {
						switch (level) {
						case ALL: logger.trace(message); break;
						case DEBUG: logger.debug(message); break;
						case INFO: logger.debug("INFO: " + message); break;
						case WARN: logger.debug("WARN: " + message); break;
						case ERROR:
						case FATAL:
							// ignore errors, they will be included in the exception
							break; }}}); }
		catch (Throwable e) {
			logger.error("liblouis service could not be loaded", e);
			throw e; }
	}
	
	@Deactivate
	protected void deactivate() {
		logger.debug("Unloading liblouis service");
	}
	
	@Reference(
		name = "LiblouisLibrary",
		unbind = "-",
		service = NativePath.class,
		target = "(identifier=http://www.liblouis.org/native/*)",
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindLibrary(NativePath path) {
		if (LiblouisExternalNativePath.LIBLOUIS_EXTERNAL)
			logger.info("Using external liblouis");
		else {
			URI libraryPath = path.get("liblouis").iterator().next();
			Louis.setLibraryPath(asFile(path.resolve(libraryPath)));
			logger.debug("Registering liblouis library: " + libraryPath); }
	}
	
	@Reference(
		name = "LiblouisTableRegistry",
		unbind = "-",
		service = LiblouisTableRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableRegistry(LiblouisTableRegistry registry) {
		tableRegistry = registry;
		logger.debug("Registering Liblouis table registry: " + registry);
	}
	
	public Iterable<LiblouisTableJnaImpl> _get(Query query) {
		return logSelect(query, _provider);
	}
	
	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper("LiblouisTableJnaImplProvider");
	}
	
	private TransformProvider<LiblouisTableJnaImpl> _provider
	= varyLocale(
		new AbstractTransformProvider<LiblouisTableJnaImpl>() {
			public Iterable<LiblouisTableJnaImpl> _get(final Query query) {
				return Iterables.of(
					new WithSideEffect<LiblouisTableJnaImpl,Logger>() {
						public LiblouisTableJnaImpl _apply() {
							MutableQuery q = mutableQuery(query);
							String table = null;
							String charset = null;
							TableInfo tableInfo = null;
							boolean whiteSpace = false;
							String dotsForUndefinedChar = null;
							Locale documentLocale = null;
							if (q.containsKey("white-space")) {
								q.removeOnly("white-space");
								whiteSpace = true; }
							if (q.containsKey("dots-for-undefined-char")) {
								dotsForUndefinedChar = q.removeOnly("dots-for-undefined-char").getValue().get();
								if (!dotsForUndefinedChar.matches("[\u2800-\u28FF]+")) {
									logger.warn(dotsForUndefinedChar + " is not a valid dot pattern string.");
									throw new NoSuchElementException();
								}
							}
							if (q.containsKey("document-locale"))
								documentLocale = parseLocale(q.removeOnly("document-locale").getValue().get());
							if (q.containsKey("charset") || q.containsKey("braille-charset"))
								charset = q.containsKey("charset")
									? q.removeOnly("charset").getValue().get()
									: q.removeOnly("braille-charset").getValue().get();
							if (q.containsKey("table") || q.containsKey("liblouis-table")) {
								table = q.containsKey("table")
									? q.removeOnly("table").getValue().get()
									: q.removeOnly("liblouis-table").getValue().get();
								tableInfo = new TableInfo(table);
								if (q.containsKey("locale")) {
									// locale is shorthand for language + region
									String locale = q.removeOnly("locale").getValue().get();
									q.add("language", locale);
									q.add("region", locale);
								}
								for (Feature f : q)
									if (!f.getValue().orElse("yes").equals(tableInfo.get(f.getKey()))) {
										logger.warn("Table " + table + " does not match " + f);
										throw new NoSuchElementException(); }
							} else {
								if (documentLocale != null && !q.containsKey("locale")) {
									// Liblouis table selection happens based on a language tag (primary target language
									// of the braille code) and an optional region tag (region or community in which the
									// braille code applies).
									if (!documentLocale.equals(new Locale(documentLocale.getLanguage(), documentLocale.getCountry()))) {
										// If the document locale has other subtags than language and region, we
										// interpret the locale as a language.
										if (!q.containsKey("language"))
											q.add("language", documentLocale.toLanguageTag());
									} else if (q.containsKey("region")) {
										// If the region is already specified in the query, we ignore the region subtag
										// of the document locale.
										if (!q.containsKey("language"))
											q.add("language", documentLocale.getLanguage());
									} else {
										// Otherwise we use the language subtag of the document locale as the language,
										// and the region subtag (if specified) as the region in which the braille code
										// applies.
										if (!q.containsKey("language"))
											q.add("language", documentLocale.getLanguage());
										if (!"".equals(documentLocale.getCountry()))
											q.add("region", documentLocale.toLanguageTag());
									}
								}
								if (q.isEmpty())
									throw new NoSuchElementException();
								StringBuilder b = new StringBuilder();
								
								// FIXME: if query does not contain (type:display), need to match for absence of "type:display"
								// -> i.e. Liblouis query syntax must support negation!
								// -> this used to be solved by matching "type:translation" but the downside of this
								//    is that this feature had to be added to every table which is not desired
								// -> another solution would be to let Liblouis return a list of possible matches and
								//    select the first match that does not end with ".dis" (or that does not have the
								//    feature "type:display")
								
								for (Feature f : q) {
									String k = f.getKey();
									if (!k.matches("[a-zA-Z0-9_-]+")) {
										__apply(
											warn("Invalid syntax for feature key: " + k));
										throw new NoSuchElementException(); }
									b.append(k);
									if (f.hasValue()) {
										String v = f.getValue().get();
										if (!v.matches("[a-zA-Z0-9_-]+")) {
											__apply(
												warn("Invalid syntax for feature value: " + v));
											throw new NoSuchElementException(); }
										b.append(":" + v); }
									b.append(" "); }
								try {
									Table t = Table.find(b.toString());
									table = t.getIdentifier();
									tableInfo = t.getInfo(); }
								catch (IllegalArgumentException e) {}
								catch (NoSuchElementException e) {}}
							if (table != null) {
								if (whiteSpace)
									table = URLs.asURI(spacesFile) + "," + table;
								DisplayTable displayTable = null;
								if (charset == null)
									displayTable = whiteSpace ? unicodeDisplayTableWithNoBreakSpace : unicodeDisplayTable;
								else
									try {
										if (whiteSpace)
											charset = "" + URLs.asURI(spacesDisFile) + "," + charset;
										// using Translator.asDisplayTable() and not DisplayTable.fromTable() so we can
										// catch CompilationException
										displayTable = new Translator(charset).asDisplayTable(); }
									catch (CompilationException e) {
										// the specified table is not a Liblouis table
										throw new NoSuchElementException(); }
								if (dotsForUndefinedChar != null) {
									try {
										File undefinedFile = File.createTempFile("undefined-", ".uti", tempDir);
										undefinedFile.deleteOnExit();
										undefinedFile.createNewFile();
										FileOutputStream writer = new FileOutputStream(undefinedFile);
										String dotPattern; {
											StringBuilder b = new StringBuilder();
											for (char c : dotsForUndefinedChar.toCharArray()) {
												b.append("-");
												c &= (char)0xFF;
												if (c == 0)
													b.append("0");
												else
													for (int k = 1; k <= 8; k++) {
														if ((c & (char)1) != 0)
															b.append(k);
														c = (char)(c >> 1); }}
											dotPattern = b.toString().substring(1); }
										writer.write(("undefined " + dotPattern + "\n").getBytes());
										writer.flush();
										writer.close();
										// adding the "undefined" rule to the end overwrites any previous rules
										table = table + "," + URLs.asURI(undefinedFile);
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								}
								try {
									return new LiblouisTableJnaImpl(new Translator(table), displayTable, tableInfo); }
								catch (CompilationException e) {
									__apply(
										warn("Could not compile table " + table));
									logger.warn("Could not compile table", e); }}
							throw new NoSuchElementException();
						}
					}
				);
			}
		}
	);
	
	private static final Logger logger = LoggerFactory.getLogger(LiblouisTableJnaImplProvider.class);
	
}
