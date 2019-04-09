package org.daisy.pipeline.braille.tex.impl;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import static org.daisy.common.file.URIs.asURI;
import static org.daisy.common.file.URLs.asURL;
import org.daisy.pipeline.braille.common.AbstractHyphenator;
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
			return fromNullable(get(asURI(v))); }
		Locale locale; {
			String loc;
			if (q.containsKey("locale"))
				loc = q.removeOnly("locale").getValue().get();
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
		
		private final FullHyphenator fullHyphenator = new FullHyphenator() {
			public String transform(String text) {
				return TexHyphenatorImpl.this.transform(text);
			}
			public String[] transform(String[] text) {
				return TexHyphenatorImpl.this.transform(text);
			}
		};
		
		public String transform(String text) {
			try {
				return hyphenator.hyphenate(text); }
			catch (Exception e) {
				throw new RuntimeException("Error during TeX hyphenation", e); }
		}
		
		public String[] transform(String[] text) {
			String[] hyphenated = new String[text.length];
			for (int i = 0; i < text.length; i++)
				try {
					hyphenated[i] = hyphenator.hyphenate(text[i]); }
				catch (Exception e) {
					throw new RuntimeException("Error during TeX hyphenation", e); }
			return hyphenated;
		}
	}
	
	private URL resolveTable(URI table) {
		URL resolvedTable = isAbsoluteFile(table) ? asURL(table) : tableRegistry.resolve(table);
		if (resolvedTable == null)
			throw new RuntimeException("Hyphenation table " + table + " could not be resolved");
		return resolvedTable;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(TexHyphenatorSimpleImpl.class);
	
}
