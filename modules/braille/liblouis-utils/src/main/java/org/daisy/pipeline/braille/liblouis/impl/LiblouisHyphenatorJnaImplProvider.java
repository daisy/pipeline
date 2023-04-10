package org.daisy.pipeline.braille.liblouis.impl;

import java.util.NoSuchElementException;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultFullHyphenator;
import org.daisy.pipeline.braille.common.HyphenatorProvider;
import org.daisy.pipeline.braille.common.Provider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;

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

/**
 * @see <a href="../../../../../../../README.md">Documentation</a>
 */
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
				if (!"liblouis".equals(q.removeOnly("hyphenator").getValue().orElse(null)))
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
			return filter(
				transform(
					tables,
					new Function<LiblouisTableJnaImpl,LiblouisHyphenator>() {
						public LiblouisHyphenator apply(LiblouisTableJnaImpl table) {
							try {
								return new LiblouisHyphenatorImpl(table.getTranslator()); }
							catch (IllegalArgumentException e) {
								return null; }}}),
				Predicates.notNull());
		}
	};
	
	private static class LiblouisHyphenatorImpl extends AbstractHyphenator implements LiblouisHyphenator {
		
		private final LiblouisTable table;
		private final Translator translator;
		
		private LiblouisHyphenatorImpl(Translator translator) {
			this.table = new LiblouisTable(translator.getTable());
			this.translator = translator;

			// The table is not guaranteed to contain a hyphenation (.dic) sub-table. We
			// can find out by hyphenating a test string.
			try {
				translator.hyphenate("foobar");
			} catch (TranslationException e) {
				// Note that Liblouis raises this error but does not log an error
				// message. We assume that the reason of the failure is because
				// `table->hyphenStatesArray == 0' (in other words, because no hyphenation
				// sub-table was encountered).
				throw new IllegalArgumentException("Table does not support hyphenation: " + translator.getTable());
			}
		}
		
		public LiblouisTable asLiblouisTable() {
			return table;
		}
		
		@Override
		public FullHyphenator asFullHyphenator() {
			return fullHyphenator;
		}
		
		private final FullHyphenator fullHyphenator = new DefaultFullHyphenator() {
			protected boolean isCodePointAware() { return true; }
			protected byte[] getHyphenationOpportunities(String textWithoutHyphens) throws RuntimeException {
				try {
					return translator.hyphenate(textWithoutHyphens); }
				catch (TranslationException e) {
					throw new RuntimeException(e); }
			}
		};
		
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
