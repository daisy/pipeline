package org.daisy.pipeline.braille.libhyphen.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import ch.sbs.jhyphen.CompilationException;
import ch.sbs.jhyphen.Hyphen;
import ch.sbs.jhyphen.Hyphenator;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Splitter;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;

import static org.daisy.common.file.URIs.asURI;
import static org.daisy.common.file.URLs.asURL;
import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.debug;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.fromNullable;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.of;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import org.daisy.pipeline.braille.common.HyphenatorProvider;
import org.daisy.pipeline.braille.common.NativePath;
import org.daisy.pipeline.braille.common.ResourceResolver;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.Files.isAbsoluteFile;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.insertHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import org.daisy.pipeline.braille.common.util.Tuple2;
import org.daisy.pipeline.braille.common.WithSideEffect;
import org.daisy.pipeline.braille.libhyphen.LibhyphenHyphenator;
import org.daisy.pipeline.braille.libhyphen.LibhyphenTableProvider;
import org.daisy.pipeline.braille.libhyphen.LibhyphenTableResolver;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.libhyphen.LibhyphenJnaImpl",
	service = {
		LibhyphenHyphenator.Provider.class,
		HyphenatorProvider.class
	}
)
public class LibhyphenJnaImpl extends AbstractTransformProvider<LibhyphenHyphenator>
	                          implements LibhyphenHyphenator.Provider {
	
	private final static char SHY = '\u00AD';
	private final static char ZWSP = '\u200B';
	
	private ResourceResolver tableResolver;
	private LibhyphenTableProvider tableProvider;
	
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
		name = "LibhyphenTableResolver",
		unbind = "-",
		service = LibhyphenTableResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableResolver(LibhyphenTableResolver resolver) {
		tableResolver = resolver;
		logger.debug("Registering libhyphen table resolver: " + resolver);
	}
	
	@Reference(
		name = "LibhyphenTableProvider",
		unbind = "-",
		service = LibhyphenTableProvider.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableProvider(LibhyphenTableProvider provider) {
		tableProvider = provider;
		logger.debug("Registering libhyphen table provider: " + provider);
	}
	
	private final static Iterable<LibhyphenHyphenator> empty
	= Iterables.<LibhyphenHyphenator>empty();
	
	protected final Iterable<LibhyphenHyphenator> _get(Query query) {
		MutableQuery q = mutableQuery(query);
		if (q.containsKey("hyphenator")) {
			String v = q.removeOnly("hyphenator").getValue().get();
			if (!"hyphen".equals(v))
				return fromNullable(fromId(v)); }
		String table = null;
		if (q.containsKey("libhyphen-table"))
			table = q.removeOnly("libhyphen-table").getValue().get();
		if (q.containsKey("table"))
			if (table != null) {
				logger.warn("A query with both 'table' and 'libhyphen-table' never matches anything");
				return empty; }
			else
				table = q.removeOnly("table").getValue().get();
		if (table != null) {
			if (!q.isEmpty()) {
				logger.warn("A query with both 'table' or 'libhyphen-table' and '"
				            + q.iterator().next().getKey() + "' never matches anything");
				return empty; }
			return of(get(asURI(table))); }
		if (tableProvider != null) {
			Locale locale; {
				String loc = "und";
				if (q.containsKey("locale"))
					loc = q.removeOnly("locale").getValue().get();
				try {
					locale = parseLocale(loc); }
				catch (IllegalArgumentException e) {
					logger.error("Invalid locale", e);
					return empty; }
			}
			return transform(
				tableProvider.get(locale),
				new Function<URI,LibhyphenHyphenator>() {
					public LibhyphenHyphenator _apply(URI table) {
						return __apply(get(table)); }}); }
		return empty;
	}
	
	private WithSideEffect<LibhyphenHyphenator,Logger> get(final URI table) {
		try {
			return logCreate((LibhyphenHyphenator)new LibhyphenHyphenatorImpl(table)); }
		catch (final Throwable e) {
			return new WithSideEffect<LibhyphenHyphenator,Logger>() {
				public LibhyphenHyphenator _apply() throws CompilationException, FileNotFoundException {
					__apply(debug("Could not create hyphenator for table " + table));
					throw e;
				}
			};
		}
	}
	
	private final static char US = '\u001F';
	private final static Splitter SEGMENT_SPLITTER = Splitter.on(US);
	
	private class LibhyphenHyphenatorImpl extends AbstractHyphenator implements LibhyphenHyphenator {
		
		private final URI table;
		private final Hyphenator hyphenator;
		
		private LibhyphenHyphenatorImpl(URI table) throws CompilationException, FileNotFoundException {
			this.table = table;
			hyphenator = new Hyphenator(resolveTable(table));
		}
		
		public URI asLibhyphenTable() {
			return table;
		}
		
		@Override
		public FullHyphenator asFullHyphenator() {
			return fullHyphenator;
		}
		
		private final FullHyphenator fullHyphenator = new FullHyphenator() {
			public String transform(String text) {
				return LibhyphenHyphenatorImpl.this.transform(text);
			}
			public String[] transform(String[] text) {
				return LibhyphenHyphenatorImpl.this.transform(text);
			}
		};
		
		@Override
		public LineBreaker asLineBreaker() {
			return lineBreaker;
		}
		
		private final LineBreaker lineBreaker = new DefaultLineBreaker() {
			protected Break breakWord(String word, int limit, boolean force) {
				Hyphenator.Break br = hyphenator.hyphenate(word, limit);
				if (force && br.getBreakPosition() == 0)
					return new Break(word, limit, false);
				else
					return new Break(br.getText(), br.getBreakPosition(), br.hasHyphen());
			}
		};
		
		private String transform(String text) {
			try {
				Tuple2<String,byte[]> t = extractHyphens(text, SHY, ZWSP);
				byte[] hyphens = hyphenator.hyphenate(t._1);
				if (t._2 != null)
					for (int i = 0; i < hyphens.length; i++)
						hyphens[i] += t._2[i];
				return insertHyphens(t._1, hyphens, SHY, ZWSP); }
			catch (Exception e) {
				throw new RuntimeException("Error during libhyphen hyphenation", e); }
		}
		
		private String[] transform(String[] text) {
			try {
				// This byte array is used not only to track the hyphen
				// positions but also the segment boundaries.
				byte[] positions;
				Tuple2<String,byte[]> t = extractHyphens(join(text, US), SHY, ZWSP);
				String[] unhyphenated = toArray(SEGMENT_SPLITTER.split(t._1), String.class);
				t = extractHyphens(t._2, t._1, null, null, US);
				String _text = t._1;
				if (t._2 != null)
					positions = t._2;
				else
					positions = new byte[_text.length() - 1];
				byte[] autoHyphens = hyphenator.hyphenate(_text);
				for (int i = 0; i < autoHyphens.length; i++)
					positions[i] += autoHyphens[i];
				_text = insertHyphens(_text, positions, SHY, ZWSP, US);
				if (text.length == 1)
					return new String[]{_text};
				else {
					String[] rv = new String[text.length];
					int i = 0;
					for (String s : SEGMENT_SPLITTER.split(_text)) {
						while (unhyphenated[i].length() == 0)
							rv[i++] = "";
						rv[i++] = s; }
					while(i < text.length)
						rv[i++] = "";
					return rv; }}
			catch (Exception e) {
				throw new RuntimeException("Error during libhyphen hyphenation", e); }
		}
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("o.d.p.b.libhyphen.impl.LibhyphenJnaImpl$LibhyphenHyphenatorImpl")
				.add("table", table);
		}
	}
	
	private File resolveTable(URI table) {
		URL resolvedTable = isAbsoluteFile(table) ? asURL(table) : tableResolver.resolve(table);
		if (resolvedTable == null)
			throw new RuntimeException("Hyphenation table " + table + " could not be resolved");
		return asFile(resolvedTable);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LibhyphenJnaImpl.class);
	
}
