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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import static org.daisy.pipeline.braille.common.util.Strings.join;
import org.daisy.pipeline.braille.common.WithSideEffect;
import org.daisy.pipeline.braille.liblouis.LiblouisTable;
import org.daisy.pipeline.braille.liblouis.LiblouisTableResolver;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.ValidationResult;

import org.liblouis.Louis;
import org.liblouis.CompilationException;
import org.liblouis.Logger.Level;
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

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisTableJnaImplProvider",
	service = {
		LiblouisTableJnaImplProvider.class,
		DatatypeService.class
	}
)
public class LiblouisTableJnaImplProvider extends AbstractTransformProvider<LiblouisTableJnaImplProvider.LiblouisTableJnaImpl>
                                          implements DatatypeService {

	// FIXME: isn't really a Transform but implements it so that we can use TransformProvider
	public class LiblouisTableJnaImpl extends LiblouisTable implements Transform {
		
		private final Translator translator;
		private final TableInfo info;
		
		private LiblouisTableJnaImpl(String table, TableInfo info) throws CompilationException {
			super(table);
			translator = new Translator(table);
			this.info = info;
		}
		
		public Translator getTranslator() {
			return translator;
		}
		
		public String getIdentifier() {
			return toString();
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
	}
	
	private LiblouisTableRegistry tableRegistry;
	
	private File unicodeDisFile;
	private File spacesFile;
	
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
								File f = createTempFile("aggregator-", ".tbl");
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
					logger.error("Table could not be resolved");
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
			unicodeDisFile = new File(makeUnpackDir(), "unicode.dis");
			unpack(
				URLs.getResourceFromJAR("/tables/unicode.dis", LiblouisTableJnaImplProvider.class),
				unicodeDisFile);
			spacesFile = new File(makeUnpackDir(), "spaces.cti");
			unpack(
				URLs.getResourceFromJAR("/tables/spaces.cti", LiblouisTableJnaImplProvider.class),
				spacesFile);
			Louis.setLogger(new org.liblouis.Logger() {
					@Override
					public void log(Level level, String message) {
						switch (level) {
						case ALL: logger.trace(message); break;
						case DEBUG: logger.debug(message); break;
						case INFO: logger.debug("INFO: " + message); break;
						case WARN: logger.debug("WARN: " + message); break;
						// FIXME: capture these and include them into CompilationException or TranslationException
						case ERROR: logger.error(message); break;
						case FATAL: logger.error(message); break; }}}); }
		catch (Throwable e) {
			logger.error("liblouis service could not be loaded", e);
			throw e; }
	}
	
	private static File makeUnpackDir() {
		File tmpDirectory; {
			try {
				tmpDirectory = createTempDirectory("pipeline-").toFile(); }
			catch (Exception e) {
				throw new RuntimeException("Could not create temporary directory", e); }
			tmpDirectory.deleteOnExit();
		}
		return normalize(tmpDirectory);
	}
	
	private static File createTempFile(String prefix, String suffix) throws IOException {
		return File.createTempFile(prefix, suffix, makeUnpackDir());
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
		return MoreObjects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisTableJnaImplProvider");
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
							TableInfo tableInfo = null;
							boolean unicode = false;
							boolean whiteSpace = false;
							String dotsForUndefinedChar = null;
							boolean display = false;
							if (q.containsKey("unicode")) {
								q.removeOnly("unicode");
								unicode = true; }
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
							if (q.containsKey("display")) {
								q.removeOnly("display");
								if (unicode) {
									logger.warn("A query with '(unicode)(display)' never matches anything");
									throw new NoSuchElementException(); }
								display = true; }
							if (q.containsKey("table"))
								// FIXME: display and remaining features in query are ignored
								table = q.removeOnly("table").getValue().get();
							else if (q.containsKey("liblouis-table"))
								// FIXME: display and remaining features in query are ignored
								table = q.removeOnly("liblouis-table").getValue().get();
							else if (!q.isEmpty()) {
								StringBuilder b = new StringBuilder();
								if (display)
									b.append("type:display ");
								
								// FIXME: if !display, need to match for absence of "type:display"
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
								if (unicode)
									table = URLs.asURI(unicodeDisFile) + "," + table;
								if (dotsForUndefinedChar != null) {
									try {
										File undefinedFile = createTempFile("undefined-", ".uti");
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
									return new LiblouisTableJnaImpl(table, tableInfo); }
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
	
	/* --------------- */
	/* DatatypeService */
	/* --------------- */

	private static final String id ="liblouis-table-query";
	private Document xmlDefinition = null;
	private List<String> enumerationValues = null;

	public String getId() {
		return id;
	}

	public Document asDocument() throws Exception {
		if (xmlDefinition == null)
			createDatatype();
		return xmlDefinition;
	}

	public ValidationResult validate(String content) {
		if (enumerationValues == null)
			try {
				createDatatype();
			} catch (Exception e) {
				return ValidationResult.notValid("Failed to determine allowed values");
			}
		if (enumerationValues.contains(content))
			return ValidationResult.valid();
		else
			return ValidationResult.notValid("'" + content + "' is not in the list of allowed values.");
	}

	private void createDatatype() throws ParserConfigurationException, DOMException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		                                     .getDOMImplementation().createDocument(null, "choice", null);
		List<String> values = new ArrayList<>();
		Element choice = doc.getDocumentElement();
		values.add("");
		choice.appendChild(doc.createElement("value"))
		      .appendChild(doc.createTextNode(""));
		choice.appendChild(doc.createElementNS("http://relaxng.org/ns/compatibility/annotations/1.0", "documentation"))
		      .appendChild(doc.createTextNode("-"));
		List<Table> tables = new ArrayList<>();
		tables.addAll(Louis.listTables());
		Collections.sort(tables,
		                 new Comparator<Table>() {
				public int compare(Table o1, Table o2) {
					String s1 = o1.getInfo().get("index-name");
					String s2 = o2.getInfo().get("index-name");
					if (s1 == null)
						if (s2 == null)
							return 0;
						else
							return -1;
					else if (s2 == null)
						return 1;
					else
						return s1.compareTo(s2); }});
		for (Table table : tables) {
			// We can only do this because we know the identifier can be used to construct a new
			// Translator, but this is not an official feature. A more correct solution would be to
			// cache the Table objects and use Table.getTranslator() to get the Translator.
			String value = "(liblouis-table:\"" + table.getIdentifier().replace("http://www.liblouis.org/tables/", "") + "\")";
			values.add(value);
			choice.appendChild(doc.createElement("value"))
			      .appendChild(doc.createTextNode(value));
			String indexName = table.getInfo().get("index-name");
			if (indexName != null) {
				choice.appendChild(doc.createElementNS("http://relaxng.org/ns/compatibility/annotations/1.0", "documentation"))
				      .appendChild(doc.createTextNode(indexName));
			}
		}
		xmlDefinition = doc;
		enumerationValues = values;
	}

	private static final Logger logger = LoggerFactory.getLogger(LiblouisTableJnaImplProvider.class);
	
}
