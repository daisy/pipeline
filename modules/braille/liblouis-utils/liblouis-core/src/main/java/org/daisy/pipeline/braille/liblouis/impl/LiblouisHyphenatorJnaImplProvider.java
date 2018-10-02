package org.daisy.pipeline.braille.liblouis.impl;

import com.google.common.base.Function;
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
		
		private String transform(String text) {
			Tuple2<String,byte[]> t = extractHyphens(text, SHY, ZWSP);
			byte[] hyphens = doHyphenate(t._1);
			if (t._2 != null)
				for (int i = 0; i < hyphens.length; i++)
					hyphens[i] += t._2[i];
			return insertHyphens(t._1, hyphens, SHY, ZWSP);
		}
		
		private String[] transform(String text[]) {
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
			byte[] autoHyphens = doHyphenate(_text);
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
				return rv; }
		}
		
		private byte[] doHyphenate(String text) {
			try { return translator.hyphenate(text); }
			catch (TranslationException e) {
				throw new RuntimeException(e); }
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LiblouisHyphenatorJnaImplProvider.class);
	
}
