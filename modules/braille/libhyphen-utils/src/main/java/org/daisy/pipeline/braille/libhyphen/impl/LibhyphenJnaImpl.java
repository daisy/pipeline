package org.daisy.pipeline.braille.libhyphen.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import ch.sbs.jhyphen.CompilationException;
import ch.sbs.jhyphen.Hyphen;
import ch.sbs.jhyphen.Hyphenator;
import ch.sbs.jhyphen.StandardHyphenationException;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Splitter;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.AbstractHyphenator;
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
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.Files.isAbsoluteFile;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.insertHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.Strings.splitInclDelimiter;
import org.daisy.pipeline.braille.common.util.Tuple2;
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
	
	private final static char SHY = '\u00AD';
	private final static char ZWSP = '\u200B';
	
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
					ret = of(get(URLs.asURI(v)));
				if (q.isEmpty())
					return ret;
				else
					return intersection(_get(q), ret); }}
		String table = null;
		if (q.containsKey("libhyphen-table"))
			table = q.removeOnly("libhyphen-table").getValue().get();
		if (q.containsKey("hyphen-table"))
			if (table != null) {
				logger.warn("A query with both 'libhyphen-table' and 'hyphen-table' never matches anything");
				return empty; }
			else
				table = q.removeOnly("hyphen-table").getValue().get();
		if (q.containsKey("table"))
			if (table != null) {
				logger.warn("A query with both 'table' and '(lib)hyphen-table' never matches anything");
				return empty; }
			else
				table = q.removeOnly("table").getValue().get();
		if (table != null) {
			if (!q.isEmpty()) {
				logger.warn("A query with both 'table' or '(lib)hyphen-table' and '"
				            + q.iterator().next().getKey() + "' never matches anything");
				return empty; }
			return of(get(URLs.asURI(table))); }
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
		return transform(
			tableRegistry.get(locale),
			new Function<URI,LibhyphenHyphenator>() {
				public LibhyphenHyphenator _apply(URI table) {
					return __apply(get(table)); }});
	}
	
	private WithSideEffect<LibhyphenHyphenator,Logger> get(final URI table) {
		try {
			return logCreate((LibhyphenHyphenator)new LibhyphenHyphenatorImpl(table)); }
		catch (CompilationException|FileNotFoundException e) {
			return new WithSideEffect<LibhyphenHyphenator,Logger>() {
				public LibhyphenHyphenator _apply() throws NoSuchElementException {
					__apply(debug("Could not create hyphenator for table " + table));
					throw new NoSuchElementException();
				}
			};
		}
	}
	
	private final static char US = '\u001F';
	private final static Splitter SEGMENT_SPLITTER = Splitter.on(US);
	private final static Pattern ON_SPACE_SPLITTER = Pattern.compile("\\s+");
	
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
			public String transform(String text) throws NonStandardHyphenationException {
				return LibhyphenHyphenatorImpl.this.transform(text);
			}
			public String[] transform(String[] text) throws NonStandardHyphenationException {
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
			if (text.length() == 0)
				return text;
			try {
				Tuple2<String,byte[]> t = extractHyphens(text, false, SHY, ZWSP);
				if (t._1.length() == 0)
					return text;
				return insertHyphens(t._1, transform(t._2, t._1), false, SHY, ZWSP); }
			catch (NonStandardHyphenationException e) {
				throw e; }
			catch (Exception e) {
				throw new RuntimeException("Error during libhyphen hyphenation", e); }
		}
		
		private String[] transform(String[] text) {
			try {
				Tuple2<String,byte[]> t = extractHyphens(join(text, US), false, SHY, ZWSP);
				String[] unhyphenated = toArray(SEGMENT_SPLITTER.split(t._1), String.class);
				t = extractHyphens(t._2, t._1, false, null, null, US);
				String _text = t._1;
				// This byte array is used not only to track the hyphen
				// positions but also the segment boundaries.
				byte[] positions = t._2;
				positions = transform(positions, _text);
				_text = insertHyphens(_text, positions, false, SHY, ZWSP, US);
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
			catch (NonStandardHyphenationException e) {
				throw e; }
			catch (Exception e) {
				throw new RuntimeException("Error during libhyphen hyphenation", e); }
		}
		
		private byte[] transform(byte[] manualHyphens, String textWithoutManualHyphens) {
			if (textWithoutManualHyphens.length() == 0)
				return manualHyphens;
			boolean hasManualHyphens = false; {
				if (manualHyphens != null)
					for (byte b : manualHyphens)
						if (b == (byte)1 || b == (byte)2) {
							hasManualHyphens = true;
							break; }}
			if (hasManualHyphens) {
				// input contains SHY or ZWSP; hyphenate only the words without SHY or ZWSP
				byte[] hyphens = Arrays.copyOf(manualHyphens, manualHyphens.length);
				boolean word = true;
				int pos = 0;
				for (String segment : splitInclDelimiter(textWithoutManualHyphens, ON_SPACE_SPLITTER)) {
					if (word && segment.length() > 0) {
						int len = segment.length();
						boolean wordHasManualHyphens = false; {
							for (int k = 0; k < len - 1; k++)
								if (hyphens[pos + k] != 0) {
									wordHasManualHyphens = true;
									break; }}
						if (!wordHasManualHyphens) {
							try {
								byte[] wordHyphens = hyphenator.hyphenate(segment);
								for (int k = 0; k < len - 1; k++)
									hyphens[pos + k] |= wordHyphens[k];
							} catch (StandardHyphenationException e) {
								throw new NonStandardHyphenationException(e);
							}
						}
					}
					pos += segment.length();
					word = !word;
				}
				return hyphens;
			} else
				try {
					return hyphenator.hyphenate(textWithoutManualHyphens);
				} catch (StandardHyphenationException e) {
					throw new NonStandardHyphenationException(e);
				}
		}
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("o.d.p.b.libhyphen.impl.LibhyphenJnaImpl$LibhyphenHyphenatorImpl")
				.add("table", table);
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
			if (!this.table.equals(that.table))
				return false;
			return true;
		}
	}
	
	private File resolveTable(URI table) throws FileNotFoundException {
		URL resolvedTable = isAbsoluteFile(table) ? URLs.asURL(table) : tableRegistry.resolve(table);
		if (resolvedTable == null)
			throw new FileNotFoundException("Hyphenation table " + table + " could not be resolved");
		return asFile(resolvedTable);
	}
	
	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(LibhyphenJnaImpl.class.getName());
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LibhyphenJnaImpl.class);
	
}
