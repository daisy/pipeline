package org.daisy.pipeline.braille.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.daisy.braille.css.LanguageRange;
import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.logCreate;
import org.daisy.pipeline.braille.common.TransformProvider.util.Memoize;
import org.daisy.pipeline.braille.common.util.Files;
import org.daisy.pipeline.braille.common.util.Strings;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "hyphenator-registry",
	service = { HyphenatorRegistry.class }
)
public class HyphenatorRegistry extends Memoize<Hyphenator> implements HyphenatorProvider<Hyphenator> {

	public HyphenatorRegistry() {
		super(true); // memoize (id:...) lookups only
		providers = new ArrayList<>();
		dispatch = dispatch(providers);
		context = logger;
		unmodifiable = false;
	}

	private HyphenatorRegistry(TransformProvider<Hyphenator> dispatch,
	                           HyphenatorRegistry from,
	                           Logger context) {
		super(from);
		this.providers = null;
		this.dispatch = dispatch;
		this.context = context;
		this.unmodifiable = true;
	}

	private final List<TransformProvider<Hyphenator>> providers;
	private final TransformProvider<Hyphenator> dispatch;
	private final Logger context;
	private final boolean unmodifiable;

	@Reference(
		name = "HyphenatorProvider",
		unbind = "-",
		service = HyphenatorProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	/**
	 * @throws UnsupportedOperationException if this object is an unmodifiable view of another
	 * {@link BrailleTranslatorRegistry}.
	 */
	public void addProvider(HyphenatorProvider p) {
		if (unmodifiable)
			throw new UnsupportedOperationException("Unmodifiable");
		providers.add(p);
	}

	public Iterable<Hyphenator> _get(Query q) {
		if (q.containsKey("exception-words")) {
			MutableQuery baseQuery = mutableQuery(q);
			String exceptionsFilePath = baseQuery.removeOnly("exception-words").getValue().get();
			FileReader exceptionsFile;
			try {
				URI u = URLs.asURI(exceptionsFilePath);
				if ("volatile-file".equals(u.getScheme()))
					// Note that file is compiled every time regardless of whether the scheme is
					// "file" or "volatile-file" (see below).
					try {
						u = new URI("file", u.getSchemeSpecificPart(), u.getFragment());
					} catch (Exception e) {
						// should not happen
						throw new IllegalStateException(e);
					}
				File f = Files.asFile(u);
				if (!f.exists())
					throw new FileNotFoundException("File does not exist: " + f);
				exceptionsFile = new FileReader(f);
			} catch (Exception e) {
				context.debug("'exception-words' could not be resolved to a file'", e);
				return Collections.emptyList();
			}
			return Iterables.transform(
				_get(baseQuery),
				// Note that the word list is compiled again every time unless a `(id:...)' query is given.
				h -> {
					try {
						return logCreate(new HyphenatorWithExceptions(h, exceptionsFile), context);
					} catch (IOException e) {
						throw new NoSuchElementException(); // should not happen
					}
				});
		}
		if (q.containsKey("document-locale")) {
			MutableQuery fallbackQuery = mutableQuery(q);
			fallbackQuery.removeAll("document-locale");
			fallbackQuery.addAll(FALLBACK_QUERY);
			return Iterables.concat(
				dispatch.get(q),
				dispatch.get(fallbackQuery));
		} else
			return dispatch.get(q);
	}

	private final static Query FALLBACK_QUERY = query("(document-locale:und)");

	@Override
	public HyphenatorRegistry withContext(Logger context) {
		return (HyphenatorRegistry)super.withContext(context);
	}

	protected HyphenatorRegistry _withContext(Logger context) {
		if (this.context == context)
			return this;
		return new HyphenatorRegistry(dispatch.withContext(context), this, context);
	}

	/**
	 * Select {@link Hyphenator}s based on a query and a CSS style sheet possibly containing
	 * {@code @hyphenation-resource} rules.
	 *
	 * Contrary to {@link #get(Query)}, this method is not memoized, and the returned objects may
	 * not be selectable based on their identifier.
	 *
	 * @param baseURI Base URI for resolving relative paths in CSS against.
	 */
	public Iterable<Hyphenator> get(Query query, String style, URI baseURI) {
		if (style != null) {
			Map<LanguageRange,Query> subQueries = HyphenationResourceParser.getHyphenatorQueries(style, baseURI, query);
			if (subQueries != null && !subQueries.isEmpty()) {
				Map<LanguageRange,Supplier<Hyphenator>> subHyphenators
					= Maps.transformValues(
						subQueries,
						q -> () -> HyphenatorRegistry.this.get(q).iterator().next());
				Iterable<Hyphenator> hyphenators = Iterables.transform(
					get(query),
					t -> logCreate(new CompoundHyphenator(subHyphenators, t), context));
			    if (query.isEmpty())
					hyphenators = Iterables.concat(
					    hyphenators,
						LazyValue.from(() -> logCreate(new CompoundHyphenator(subHyphenators, null), context)));
				return hyphenators;
			}
		}
		return get(query);
	}

	@Override
	public String toString() {
		return "memoize(dispatch( " + Strings.join(providers, ", ") + " ))";
	}

	private static final Logger logger = LoggerFactory.getLogger(HyphenatorRegistry.class);
}
