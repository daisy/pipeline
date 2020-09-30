package org.daisy.pipeline.braille.liblouis.impl;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;

import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.HyphenatorProvider;
import org.daisy.pipeline.braille.common.Provider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.insertHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.Strings.splitInclDelimiter;
import static org.daisy.pipeline.braille.common.util.Tuple2;

import org.daisy.pipeline.braille.liblouis.LiblouisHyphenator;
import org.daisy.pipeline.braille.liblouis.LiblouisTable;
import org.daisy.pipeline.braille.liblouis.impl.LiblouisTableJnaImplProvider.LiblouisTableJnaImpl;

import org.liblouis.TranslationException;
import org.liblouis.Translator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisHyphenatorJnaImplProvider",
	service = {
		LiblouisHyphenator.Provider.class,
		HyphenatorProvider.class
	}
)
public class LiblouisHyphenatorJnaImplProvider implements LiblouisHyphenator.Provider {
	
	private LiblouisTableJnaImplProvider tableProvider;
	
	@Reference(
		name = "LiblouisTableJnaImplProvider",
		unbind = "unbindLiblouisTableJnaImplProvider",
		service = LiblouisTableJnaImplProvider.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindLiblouisTableJnaImplProvider(LiblouisTableJnaImplProvider provider) {
		tableProvider = provider;
		logger.debug("Registering Liblouis JNA translator provider: " + provider);
	}
	
	protected void unbindLiblouisTableJnaImplProvider(LiblouisTableJnaImplProvider provider) {
		tableProvider = null;
	}
	
	public TransformProvider<LiblouisHyphenator> withContext(Logger context) {
		return this;
	}
	
	public Iterable<LiblouisHyphenator> get(Query query) {
		return provider.get(query);
	}
	
	private final static Iterable<LiblouisHyphenator> empty = Optional.<LiblouisHyphenator>absent().asSet();
	
	private Provider.util.MemoizingProvider<Query,LiblouisHyphenator> provider
	= new Provider.util.Memoize<Query,LiblouisHyphenator>() {
		public Iterable<LiblouisHyphenator> _get(Query query) {
			MutableQuery q = mutableQuery(query);
			if (q.containsKey("hyphenator"))
				if (!"liblouis".equals(q.removeOnly("hyphenator").getValueOrNull()))
					return empty;
			String table = null;
			if (q.containsKey("liblouis-table"))
				table = q.removeOnly("liblouis-table").getValue().get();
			if (q.containsKey("table"))
				if (table != null) {
					logger.warn("A query with both 'table' and 'liblouis-table' never matches anything");
					return empty; }
				else
					table = q.removeOnly("table").getValue().get();
			String v = null;
			v = null;
			if (q.containsKey("locale"))
				v = q.removeOnly("locale").getValue().get();
			final String locale = v;
			if (table != null && !q.isEmpty()) {
				logger.warn("A query with both 'table' or 'liblouis-table' and '"
				            + q.iterator().next().getKey() + "' never matches anything");
				return empty; }
			if (table != null)
				q.add("table", table);
			if (locale != null)
				try {
					q.add("locale", parseLocale(locale).toLanguageTag()); }
				catch (IllegalArgumentException e) {
					logger.error("Invalid locale", e);
					return empty; }
			Iterable<LiblouisTableJnaImpl> tables = tableProvider.get(q.asImmutable());
			return transform(
				tables,
				new Function<LiblouisTableJnaImpl,LiblouisHyphenator>() {
					public LiblouisHyphenator apply(LiblouisTableJnaImpl table) {
						return new LiblouisHyphenatorImpl(table.getTranslator()); }});
		}
	};
	
	private static class LiblouisHyphenatorImpl extends AbstractHyphenator implements LiblouisHyphenator {
		
		private final LiblouisTable table;
		private final Translator translator;
		
		private LiblouisHyphenatorImpl(Translator translator) {
			this.table = new LiblouisTable(translator.getTable());
			this.translator = translator;
		}
		
		public LiblouisTable asLiblouisTable() {
			return table;
		}
		
		@Override
		public FullHyphenator asFullHyphenator() {
			return fullHyphenator;
		}
		
		private final FullHyphenator fullHyphenator = new FullHyphenator() {
			public String transform(String text) {
				return LiblouisHyphenatorImpl.this.transform(text);
			}
			public String[] transform(String[] text) {
				return LiblouisHyphenatorImpl.this.transform(text);
			}
		};
		
		private final static char SHY = '\u00AD';
		private final static char ZWSP = '\u200B';
		private final static char US = '\u001F';
		private final static Splitter SEGMENT_SPLITTER = Splitter.on(US);
		private final static Pattern ON_SPACE_SPLITTER = Pattern.compile("\\s+");
		
		private String transform(String text) {
			if (text.length() == 0)
				return text;
			Tuple2<String,byte[]> t = extractHyphens(text, SHY, ZWSP);
			if (t._1.length() == 0)
				return text;
			return insertHyphens(t._1, transform(t._2, t._1), SHY, ZWSP);
		}
		
		private String[] transform(String text[]) {
			Tuple2<String,byte[]> t = extractHyphens(join(text, US), SHY, ZWSP);
			String[] unhyphenated = toArray(SEGMENT_SPLITTER.split(t._1), String.class);
			t = extractHyphens(t._2, t._1, null, null, US);
			String _text = t._1;
			// This byte array is used not only to track the hyphen
			// positions but also the segment boundaries.
			byte[] positions = t._2;
			positions = transform(positions, _text);
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
				return rv; }
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
							byte[] wordHyphens = doHyphenate(segment);
							for (int k = 0; k < len - 1; k++)
								hyphens[pos + k] |= wordHyphens[k];
						}
					}
					pos += segment.length();
					word = !word;
				}
				return hyphens;
			} else
				return doHyphenate(textWithoutManualHyphens);
		}
		
		private byte[] doHyphenate(String text) {
			try { return translator.hyphenate(text); }
			catch (TranslationException e) {
				throw new RuntimeException(e); }
		}
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisHyphenatorJnaImplProvider$LiblouisHyphenatorImpl")
				.add("table", table);
		}
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(LiblouisHyphenatorJnaImplProvider.class.getName()).toString();
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LiblouisHyphenatorJnaImplProvider.class);
	
}
