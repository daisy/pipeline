package org.daisy.pipeline.braille.liblouis.impl;

import java.io.File;
import java.net.URI;
import java.util.NoSuchElementException;

import com.google.common.base.Function;
import static com.google.common.base.Functions.toStringFunction;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;

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
import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import org.daisy.pipeline.braille.common.WithSideEffect;

import org.daisy.pipeline.braille.liblouis.LiblouisTable;
import org.daisy.pipeline.braille.liblouis.LiblouisTableResolver;

import org.liblouis.Louis;
import org.liblouis.CompilationException;
import static org.liblouis.Logger.Level.ALL;
import static org.liblouis.Logger.Level.DEBUG;
import static org.liblouis.Logger.Level.INFO;
import static org.liblouis.Logger.Level.WARN;
import static org.liblouis.Logger.Level.ERROR;
import static org.liblouis.Logger.Level.FATAL;
import org.liblouis.TableResolver;
import org.liblouis.Translator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisTableJnaImplProvider",
	service = {
		LiblouisTableJnaImplProvider.class
	}
)
public class LiblouisTableJnaImplProvider extends AbstractTransformProvider<LiblouisTableJnaImplProvider.LiblouisTableJnaImpl> {

	// FIXME: isn't really a Transform
	public class LiblouisTableJnaImpl extends LiblouisTable implements Transform {
		
		private final Translator translator;
		
		private LiblouisTableJnaImpl(String table) throws CompilationException {
			super(table);
			translator = new Translator(table);
		}
		
		public Translator getTranslator() {
			return translator;
		}
		
		public String getIdentifier() {
			return toString();
		}
		
		public XProc asXProc() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
	
	private final static boolean LIBLOUIS_EXTERNAL = Boolean.getBoolean("org.daisy.pipeline.liblouis.external");
	
	private LiblouisTableRegistry tableRegistry;
	
	// Hold a reference to avoid garbage collection
	private TableResolver _tableResolver;
	private org.liblouis.Logger _logger;
	
	private File unicodeDisFile;
	private File spacesFile;
	
	@Activate
	protected void activate(ComponentContext context) {
		logger.debug("Loading liblouis service");
		logger.debug("liblouis version: {}", Louis.getLibrary().lou_version());
		try {
			tableRegistry.onPathChange(
				new Function<LiblouisTableRegistry,Void>() {
					public Void apply(LiblouisTableRegistry r) {
						indexed = false;
						invalidateCache();
						return null; }});
			final LiblouisTableResolver tableResolver = tableRegistry;
			_tableResolver = new TableResolver() {
				public File[] invoke(String table, File base) {
					logger.debug("Resolving " + table + (base != null ? " against base " + base : ""));
					File[] resolved = tableResolver.resolveLiblouisTable(new LiblouisTable(table), base);
					if (resolved != null)
						logger.debug("Resolved to " + join(resolved, ","));
					else
						logger.error("Table could not be resolved");
					return resolved; }};
			Louis.getLibrary().lou_registerTableResolver(_tableResolver);
			unicodeDisFile = new File(makeUnpackDir(context), "unicode.dis");
			unpack(
				context.getBundleContext().getBundle().getEntry("/tables/unicode.dis"),
				unicodeDisFile);
			spacesFile = new File(makeUnpackDir(context), "spaces.cti");
			unpack(
				context.getBundleContext().getBundle().getEntry("/tables/spaces.cti"),
				spacesFile);
			_logger = new org.liblouis.Logger() {
				public void invoke(int level, String message) {
					switch (level) {
					case ALL: logger.trace(message); break;
					case DEBUG: logger.debug(message); break;
					case INFO: logger.info(message); break;
					case WARN: logger.warn(message); break;
					case ERROR: logger.error(message); break;
					case FATAL: logger.error(message); break; }}};
			Louis.getLibrary().lou_registerLogCallback(_logger); }
		catch (Throwable e) {
			logger.error("liblouis service could not be loaded", e);
			throw e; }
	}
	
	private static File makeUnpackDir(ComponentContext context) {
		File directory;
		for (int i = 0; true; i++) {
			directory = context.getBundleContext().getDataFile("resources" + i);
			if (!directory.exists()) break; }
		directory.mkdirs();
		return directory;
	}
	
	private boolean indexed = false;
	
	private void lazyIndex() {
		if (indexed)
			return;
		logger.debug("Indexing tables");
		Louis.getLibrary().lou_indexTables(
			toArray(
				transform(
					tableRegistry.listAllTableFiles(),
					toStringFunction()),
			String.class));
		indexed = true;
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
		if (LIBLOUIS_EXTERNAL)
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
		return Objects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisTableJnaImplProvider");
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
							String type = "translation";
							boolean unicode = false;
							boolean whiteSpace = false;
							if (q.containsKey("unicode")) {
								q.removeOnly("unicode");
								unicode = true; }
							if (q.containsKey("white-space")) {
								q.removeOnly("white-space");
								whiteSpace = true; }
							if (q.containsKey("display")) {
								q.removeOnly("display");
								if (unicode) {
									logger.warn("A query with '(unicode)(display)' never matches anything");
									throw new NoSuchElementException(); }
								type = "display"; }
							if (q.containsKey("table"))
								table = q.removeOnly("table").getValue().get();
							else if (q.containsKey("liblouis-table"))
								table = q.removeOnly("liblouis-table").getValue().get();
							else if (!q.isEmpty()) {
								StringBuilder b = new StringBuilder();
								b.append("type:");
								b.append(type);
								b.append(" ");
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
								lazyIndex();
								table = Louis.getLibrary().lou_findTable(b.toString()); }
							if (table != null) {
								if (whiteSpace)
									table = asURI(spacesFile) + "," + table;
								if (unicode)
									table = asURI(unicodeDisFile) + "," + table;
								try {
									return new LiblouisTableJnaImpl(table); }
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
