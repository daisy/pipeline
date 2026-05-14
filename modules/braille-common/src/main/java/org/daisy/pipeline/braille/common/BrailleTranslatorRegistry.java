package org.daisy.pipeline.braille.common;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.logCreate;
import org.daisy.pipeline.braille.common.TransformProvider.util.Memoize;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import org.daisy.pipeline.braille.common.util.Strings;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "braille-translator-registry",
	service = { BrailleTranslatorRegistry.class }
)
public class BrailleTranslatorRegistry extends Memoize<BrailleTranslator>
                                       implements BrailleTranslatorProvider<BrailleTranslator> {

	public BrailleTranslatorRegistry() {
		super(true); // memoize (id:...) lookups only
		providers = new ArrayList<>();
		dispatch = dispatch(providers);
		context = logger;
		unmodifiable = false;
	}

	private BrailleTranslatorRegistry(TransformProvider<BrailleTranslator> dispatch,
	                                  HyphenatorRegistry hyphenatorRegistry,
	                                  BrailleTranslatorRegistry from,
	                                  Logger context) {
		super(from);
		this.providers = null;
		this.dispatch = dispatch;
		this.hyphenatorRegistry = hyphenatorRegistry;
		this.context = context;
		this.unmodifiable = true;
	}

	private final List<TransformProvider<BrailleTranslator>> providers;
	private final TransformProvider<BrailleTranslator> dispatch;
	private final Logger context;
	private final boolean unmodifiable;

	@Reference(
		name = "BrailleTranslatorProvider",
		unbind = "-",
		service = BrailleTranslatorProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	/**
	 * @throws UnsupportedOperationException if this object is an unmodifiable view of another
	 * {@link BrailleTranslatorRegistry}.
	 */
	public void addProvider(BrailleTranslatorProvider p) throws UnsupportedOperationException {
		if (unmodifiable)
			throw new UnsupportedOperationException("Unmodifiable");
		providers.add(p);
	}

	@Override
	public Iterable<BrailleTranslator> _get(Query q) {
		return dispatch.get(q);
	}

	@Override
	public BrailleTranslatorRegistry withContext(Logger context) {
		return (BrailleTranslatorRegistry)super.withContext(context);
	}
	
	protected BrailleTranslatorRegistry _withContext(Logger context) {
		if (this.context == context)
			return this;
		return new BrailleTranslatorRegistry(dispatch.withContext(context),
		                                     hyphenatorRegistry.withContext(context),
		                                     this,
		                                     context);
	}

	private HyphenatorRegistry hyphenatorRegistry;

	@Reference(
		name = "HyphenatorRegistry",
		unbind = "-",
		service = HyphenatorRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	/**
	 * @throws UnsupportedOperationException if this object is an unmodifiable view of another
	 * {@link BrailleTranslatorRegistry}.
	 */
	public void bindHyphenatorRegistry(HyphenatorRegistry registry) throws UnsupportedOperationException {
		if (unmodifiable)
			throw new UnsupportedOperationException("Unmodifiable");
		hyphenatorRegistry = registry;
		logger.debug("Binding hyphenator registry: " + registry);
	}

	/**
	 * Select {@link BrailleTranslator}s with {@link Hyphenator} based on a single query.
	 *
	 * Only the {@code hyphenator} and {@code document-locale} features contribute to the hyphenator
	 * selection.
	 *
	 * Like {@code #get(Query)}, returned objects are selectable based on their identifier.
	 */
	public Iterable<BrailleTranslator> getWithHyphenator(Query query) {
		return getWithHyphenator(this, hyphenatorRegistry, query, context);
	}

	private static Iterable<BrailleTranslator> getWithHyphenator(Provider<Query,BrailleTranslator> translatorProvider,
	                                                             Provider<Query,Hyphenator> hyphenatorProvider,
	                                                             Query query,
	                                                             Logger context) {
		if (query.containsKey("id"))
			return translatorProvider.get(query);
		MutableQuery q = mutableQuery(query);

		// select hyphenator
		MutableQuery hyphenatorQuery = mutableQuery();
		String hyphenator = q.containsKey("hyphenator")
			? q.removeOnly("hyphenator").getValue().get()
			: "auto";
		if (!"auto".equals(hyphenator))
			hyphenatorQuery.add("hyphenator", hyphenator);
		String documentLocale; {
			try {
				documentLocale = q.containsKey("document-locale")
					? parseLocale(q.getOnly("document-locale").getValue().get()).toLanguageTag()
					: null; }
			catch (IllegalArgumentException e) {
				logger.error("Invalid locale", e);
				return Collections.emptyList(); }
		}
		if (documentLocale != null)
			hyphenatorQuery.add("document-locale", documentLocale);
		Iterable<Hyphenator> hyphenators; {
			Iterable<Hyphenator> h = hyphenatorProvider.get(hyphenatorQuery.asImmutable());
			if (documentLocale != null && !"auto".equals(hyphenator)) {
				// also search without locale because "hyphenator" feature might be an ID
				hyphenatorQuery.removeAll("document-locale");
				h = Iterables.concat(h, hyphenatorProvider.get(hyphenatorQuery.asImmutable())); }
			hyphenators = h;
		}

		// select translator and bind hyphenator
		Iterable<BrailleTranslator> translators = translatorProvider.get(q);
		Iterable<BrailleTranslator> translatorsWithHyphenator =
			Iterables.concat(
				Iterables.transform(
					translators,
					t -> Iterables.filter(
						Iterables.transform(
							hyphenators,
							h -> {
								try {
									return logCreate(t.withHyphenator(h), context);
								} catch (UnsupportedOperationException e) {
									logger.debug("Could not set hyphenator: " + e.getMessage());
									return null;
								}}),
						Predicates.notNull())));
		return "auto".equals(hyphenator)
			? Iterables.concat(translatorsWithHyphenator, translators)
			: translatorsWithHyphenator;
	}

	/**
	 * Select {@link BrailleTranslator}s based on a query and a CSS style sheet possibly containing
	 * {@code @text-transform} rules.
	 *
	 * Contrary to {@link #get(Query)}, this method is not memoized, and the returned objects may
	 * not be selectable based on their identifier.
	 *
	 * @param baseURI Base URI for resolving relative paths in CSS against.
	 * @param forceMainTranslator if {@code true}, the translator defined in {@code query} is used
	 *                            even if a default translator has been defined in CSS.
	 */
	public Iterable<BrailleTranslator> get(Query query, String style, URI baseURI, boolean forceMainTranslator) {
		if (style != null) {
			Map<String,Query> subQueries = TextTransformParser.getBrailleTranslatorQueries(style, baseURI, query);
			if (subQueries != null && !subQueries.isEmpty()) {
				Query mainQuery = subQueries.remove("auto");
				if (mainQuery == null || forceMainTranslator)
					mainQuery = query;
				if (subQueries.isEmpty())
					return get(mainQuery);
				else {
					Map<String,Supplier<BrailleTranslator>> subTranslators
						= Maps.transformValues(
							subQueries,
							// using getWithHyphenator() because @text-transform rules may contain "hyphenator"
							// descriptor (though it should be deprecated)
							q -> () -> BrailleTranslatorRegistry.this.getWithHyphenator(q).iterator().next());
					return Iterables.transform(
						get(mainQuery),
						t -> logCreate(new CompoundBrailleTranslator(t, subTranslators), context));
				}
			}
		}
		return get(query);
	}

	/**
	 * Select {@link BrailleTranslator}s with {@link Hyphenator} based on a single query and a CSS
	 * style sheet possibly containing {@code @text-transform} and {@code @hyphenation-resource}
	 * rules.
	 *
	 * Contrary to {@link #get(Query)} and similar to {@link #get(Query, String, URI, boolean)},
	 * this method is not memoized, and the returned objects may not be selectable based on their
	 * identifier.
	 */
	public Iterable<BrailleTranslator> getWithHyphenator(Query query, String style, URI baseURI, boolean forceMainTranslator) {
		return getWithHyphenator(
			q -> get(q, style, baseURI, forceMainTranslator),
			q -> hyphenatorRegistry.get(q, style, baseURI),
			query,
			context);
	}

	@Override
	public String toString() {
		return "memoize(dispatch( " + Strings.join(providers, ", ") + " ))";
	}

	private static final Logger logger = LoggerFactory.getLogger(BrailleTranslatorRegistry.class);
}
