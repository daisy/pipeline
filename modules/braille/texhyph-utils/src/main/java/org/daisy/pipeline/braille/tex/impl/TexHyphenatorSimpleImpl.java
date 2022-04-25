package org.daisy.pipeline.braille.tex.impl;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultFullHyphenator;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.fromNullable;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import org.daisy.pipeline.braille.common.HyphenatorProvider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.util.Files.isAbsoluteFile;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import static org.daisy.pipeline.braille.common.util.Tuple2;
import org.daisy.pipeline.braille.tex.TexHyphenator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.tex.impl.TexHyphenatorSimpleImpl",
	service = {
		TexHyphenator.Provider.class,
		HyphenatorProvider.class
	}
)
public class TexHyphenatorSimpleImpl extends AbstractTransformProvider<TexHyphenator>
	                                 implements TexHyphenator.Provider {
	
	private TexHyphenatorTableRegistry tableRegistry;
	
	@Activate
	protected void activate() {
		logger.debug("Loading TeX hyphenation service");
	}
	
	@Deactivate
	protected void deactivate() {
		logger.debug("Unloading TeX hyphenation service");
	}
	
	@Reference(
		name = "TexHyphenatorTableRegistry",
		unbind = "unbindTableRegistry",
		service = TexHyphenatorTableRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableRegistry(TexHyphenatorTableRegistry registry) {
		tableRegistry = registry;
		logger.debug("Registering Tex hyphenation table registry: " + registry);
	}
	
	protected void unbindTableRegistry(TexHyphenatorTableRegistry registry) {
		tableRegistry = null;
	}
	
	private final static Iterable<TexHyphenator> empty = Iterables.<TexHyphenator>empty();
	
	public Iterable<TexHyphenator> _get(Query query) {
		MutableQuery q = mutableQuery(query);
		if (q.containsKey("hyphenator")) {
			String v = q.removeOnly("hyphenator").getValue().get();
			if (!"texhyph".equals(v) && !"tex".equals(v))
				return fromNullable(fromId(v)); }
		if (q.containsKey("table")) {
			String v = q.removeOnly("table").getValue().get();
			if (!q.isEmpty()) {
				logger.warn("A query with both 'table' and '" + q.iterator().next().getKey() + "' never matches anything");
				return empty; }
			return fromNullable(get(URLs.asURI(v))); }
		Locale locale; {
			String loc;
			if (q.containsKey("document-locale"))
				loc = q.removeOnly("document-locale").getValue().get();
			else
				loc = "und";
			try {
				locale = parseLocale(loc); }
			catch (IllegalArgumentException e) {
				logger.error("Invalid locale", e);
				return empty; }
		}
		if (!q.isEmpty()) {
			logger.warn("A query with '" + q.iterator().next().getKey() + "' never matches anything");
			return empty; }
		if (tableRegistry != null) {
			return transform(
				tableRegistry.get(locale),
				new Function<URI,TexHyphenator>() {
					public TexHyphenator _apply(URI table) {
						return get(table); }}); }
		return empty;
	}
	
	private TexHyphenator get(URI table) {
		if (table.toString().endsWith(".tex")) {
			try { return new TexHyphenatorImpl(table); }
			catch (Exception e) {
				logger.warn("Could not create hyphenator for table " + table, e); }}
		return null;
	}
	
	private class TexHyphenatorImpl extends AbstractHyphenator implements TexHyphenator {
		
		private final URI table;
		private final net.davidashen.text.Hyphenator hyphenator;
		
		private TexHyphenatorImpl(URI table) throws IOException {
			this.table = table;
			hyphenator = new net.davidashen.text.Hyphenator();
			InputStream stream = resolveTable(table).openStream();
			hyphenator.loadTable(stream);
			stream.close();
		}
		
		public URI asTexHyphenatorTable() {
			return table;
		}
		
		@Override
		public FullHyphenator asFullHyphenator() {
			return fullHyphenator;
		}
		
		private final FullHyphenator fullHyphenator = new DefaultFullHyphenator() {

				private final static char SHY = '\u00AD';
				private final static char ZWSP = '\u200B';

				protected boolean isCodePointAware() { return true; }
				protected boolean isLanguageAdaptive() { return true; }
		
				/**
				 * @param language ignored
				 */
				protected byte[] getHyphenationOpportunities(String textWithoutHyphens, Locale language) throws RuntimeException {
					try {
						Tuple2<String,byte[]> t = extractHyphens(
							hyphenator.hyphenate(textWithoutHyphens), true, SHY, ZWSP);
						if (!t._1.equals(textWithoutHyphens))
							throw new RuntimeException("Unexpected output from " + hyphenator);
						return t._2; }
					catch (Exception e) {
						throw new RuntimeException("Error during TeX hyphenation", e); }
				}
			};
	}
	
	private URL resolveTable(URI table) {
		URL resolvedTable = isAbsoluteFile(table) ? URLs.asURL(table) : tableRegistry.resolve(table);
		if (resolvedTable == null)
			throw new RuntimeException("Hyphenation table " + table + " could not be resolved");
		return resolvedTable;
	}
	
	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(TexHyphenatorSimpleImpl.class.getName());
	}
	
	private static final Logger logger = LoggerFactory.getLogger(TexHyphenatorSimpleImpl.class);
	
}
