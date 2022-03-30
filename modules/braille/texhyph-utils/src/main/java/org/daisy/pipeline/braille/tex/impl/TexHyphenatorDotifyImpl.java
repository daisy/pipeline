package org.daisy.pipeline.braille.tex.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import net.davidashen.text.Utf8TexParser.TexParserException;

import org.daisy.common.file.URLs;
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
	name = "org.daisy.pipeline.braille.tex.impl.TexHyphenatorDotifyImpl",
	service = {
		TexHyphenator.Provider.class,
		HyphenatorProvider.class
	}
)
public class TexHyphenatorDotifyImpl extends AbstractTransformProvider<TexHyphenator>
	                                 implements TexHyphenator.Provider {
	
	private TexHyphenatorTableRegistry tableRegistry;
	
	@Activate
	protected void activate() {
		logger.debug("Loading TeX hyphenation service (Dotify impl)");
	}
	
	@Deactivate
	protected void deactivate() {
		logger.debug("Unloading TeX hyphenation service (Dotify impl)");
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
		try {
			URL resolved = resolveTable(table);
			Properties properties = new Properties();
			URI base = null;
			if (table.toString().endsWith(".tex")) {
				properties.setProperty(TexHyphenatorImpl.PATTERN_PATH_KEY, URLs.asURI(resolved).toASCIIString());
				properties.setProperty(TexHyphenatorImpl.MODE_KEY, TexHyphenatorImpl.BYTE_MODE); }
			else if (table.toString().endsWith(".properties")) {
				base = URLs.asURI(resolved);
				InputStream stream = resolved.openStream();
				properties.load(stream);
				stream.close(); }
			else if (table.toString().endsWith(".xml")) {
				base = URLs.asURI(resolved);
				InputStream stream = resolved.openStream();
				properties.loadFromXML(stream);
				stream.close(); }
			else
				return null;
			try {
				return new TexHyphenatorImpl(properties, base); }
			catch (Exception e) {
				logger.warn("Could not create a hyphenator for properties " + properties, e); }}
		catch (Exception e) {
			logger.warn("Could not create a hyphenator for table", e); }
		return null;
	}
	
	private URL resolveTable(URI table) {
		URL resolvedTable = isAbsoluteFile(table) ? URLs.asURL(table) : tableRegistry.resolve(table);
		if (resolvedTable == null)
			throw new RuntimeException("Hyphenation table " + table + " could not be resolved");
		return resolvedTable;
	}
	
	/*
	 * Code originally taken from org.daisy.dotify.impl.hyphenator.latex.HyphenationConfig
	 */
	private class TexHyphenatorImpl extends AbstractHyphenator implements TexHyphenator {
		
		private final static String LEFT_HYPHEN_MIN_KEY = "beginLimit";
		private final static String RIGHT_HYPHEN_MIN_KEY = "endLimit";
		private final static String ENCODING_KEY = "encoding";
		private final static String PATTERN_PATH_KEY = "patternPath";
		private final static String MODE_KEY = "mode";
		private final static String BYTE_MODE = "byte";
		private final static String CHARACTER_MODE = "character";
		
		private final URI table;
		private final net.davidashen.text.Hyphenator hyphenator;
		private final int beginLimit;
		private final int endLimit;
		
		private TexHyphenatorImpl(Properties props, URI base) throws IOException, TexParserException {
			String patternPath = props.getProperty(PATTERN_PATH_KEY);
			if (patternPath == null)
				throw new RuntimeException("Required property named '" + PATTERN_PATH_KEY + "' missing.");
			table = base == null ? URLs.asURI(patternPath) : URLs.resolve(base, URLs.asURI(patternPath));
			hyphenator = new net.davidashen.text.Hyphenator();
			String leftHyphenMinStr = props.getProperty(LEFT_HYPHEN_MIN_KEY);
			if (leftHyphenMinStr != null)
				beginLimit = Integer.parseInt(leftHyphenMinStr);
			else
				beginLimit = 1;
			String rightHyphenMinStr = props.getProperty(RIGHT_HYPHEN_MIN_KEY);
			if (rightHyphenMinStr != null)
				endLimit = Integer.parseInt(rightHyphenMinStr);
			else
				endLimit = 1;
			String encoding = props.getProperty(ENCODING_KEY);
			String modeStr = props.getProperty(MODE_KEY);
			if (modeStr == null)
				throw new RuntimeException("Required property named '" + MODE_KEY + "' missing.");
			else if (modeStr.equals(BYTE_MODE)) {
				if (encoding != null)
					logger.warn("Configuration problem: Encoding has no effect in byte mode.");
				hyphenator.loadTable(URLs.asURL(table).openStream()); }
			else if (modeStr.equals(CHARACTER_MODE)) {
				if (encoding == null)
					logger.warn("Configuration problem: Encoding should be set in character mode.");
				hyphenator.loadTable(new InputStreamReader(URLs.asURL(table).openStream(), Charset.forName(encoding))); }
			else
				throw new RuntimeException("Unrecognized mode. Allowed values are " + BYTE_MODE + " and " + CHARACTER_MODE);
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
				return hyphenator.hyphenate(text, beginLimit, endLimit); }
			catch (Exception e) {
				throw new RuntimeException("Error during TeX hyphenation", e); }
		}
		
		public String[] transform(String[] text) {
			String[] hyphenated = new String[text.length];
			for (int i = 0; i < text.length; i++)
				try {
					hyphenated[i] = hyphenator.hyphenate(text[i], beginLimit, endLimit); }
				catch (Exception e) {
					throw new RuntimeException("Error during TeX hyphenation", e); }
			return hyphenated;
		}
	}
	
	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(TexHyphenatorDotifyImpl.class.getName());
	}
	
	private static final Logger logger = LoggerFactory.getLogger(TexHyphenatorDotifyImpl.class);
	
}
