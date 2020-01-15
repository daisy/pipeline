package org.daisy.pipeline.braille.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import static com.google.common.collect.Iterables.transform;

import org.daisy.pipeline.braille.common.util.Locales;
import org.daisy.pipeline.braille.common.Provider;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.join;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface TransformProvider<T extends Transform> extends Provider<Query,T>, Contextual<Logger,TransformProvider<T>> {
	
	/* ================== */
	/*       UTILS        */
	/* ================== */
	
	public static abstract class util {
		
		/* ------ */
		/* cast() */
		/* ------ */
		
		@SuppressWarnings(
			"unchecked" // safe cast
		)
		public static <T extends Transform> TransformProvider<T> cast(TransformProvider<? extends T> provider) {
			return (TransformProvider<T>)provider;
		}
		
		/* --------- */
		/* memoize() */
		/* --------- */
		
		public interface MemoizingProvider<T extends Transform>
			extends Provider.util.MemoizingProvider<Query,T>, TransformProvider<T> {}
		
		public static <T extends Transform> MemoizingProvider<T> memoize(TransformProvider<T> provider) {
			return new MemoizeFromProvider<T>(provider);
		}
		
		private static abstract class Memoize<T extends Transform>
				extends Provider.util.Memoize<Query,T> implements MemoizingProvider<T> {
			
			private Map<Logger,TransformProvider<T>> providerCache = new HashMap<Logger,TransformProvider<T>>();
			
			public Memoize() {
				providerCache.put(null, this);
			}
			
			protected abstract TransformProvider<T> _withContext(Logger context);
			
			public final TransformProvider<T> withContext(Logger context) {
				if (providerCache.containsKey(context))
					return providerCache.get(context);
				TransformProvider<T> provider = new DerivativeProvider(_withContext(context));
				providerCache.put(context, provider);
				return provider;
			}
			
			private class DerivativeProvider
					extends Provider.util.Memoize<Query,T>
					implements TransformProvider<T> {
				private final TransformProvider<T> provider;
				private DerivativeProvider(TransformProvider<T> provider) {
					this.provider = provider;
				}
				public Iterable<T> _get(Query query) {
					return provider.get(query);
				}
				public TransformProvider<T> withContext(Logger context) {
					return Memoize.this.withContext(context);
				}
			}
		}
		
		private static class MemoizeFromProvider<T extends Transform> extends Memoize<T> {
			private final TransformProvider<T> provider;
			private MemoizeFromProvider(TransformProvider<T> provider) {
				this.provider = provider;
			}
			protected Iterable<T> _get(Query query) {
				return provider.get(query);
			}
			protected TransformProvider<T> _withContext(Logger context) {
				return provider.withContext(context);
			}
			@Override
			public String toString() {
				return "memoize( " + provider + " )";
			}
		}
		
		/* ---------- */
		/* dispatch() */
		/* ---------- */
		
		public static abstract class Dispatch<T extends Transform>
				extends Provider.util.Dispatch<Query,T> implements TransformProvider<T> {
			
			private final Logger context;
			
			public Dispatch(Logger context) {
				this.context = context;
			}
			
			protected abstract Iterable<TransformProvider<T>> _dispatch();
			
			public final Iterable<Provider<Query,T>> dispatch() {
				return transform(
					_dispatch(),
					new Function<TransformProvider<T>,Provider<Query,T>>() {
						public Provider<Query,T> apply(TransformProvider<T> provider) {
							return provider.withContext(context); }});
			}
		}
		
		@SuppressWarnings(
			"unchecked" // safe cast to Iterable<Provider<Q,X>>
		)
		public static <T extends Transform> TransformProvider<T> dispatch(Iterable<? extends TransformProvider<T>> dispatch) {
			return new DispatchFromProviderIterable<T>((Iterable<TransformProvider<T>>)dispatch, null);
		}
		
		private static class DispatchFromProviderIterable<T extends Transform> extends Dispatch<T> {
			private final Iterable<TransformProvider<T>> dispatch;
			private DispatchFromProviderIterable(Iterable<TransformProvider<T>> dispatch, Logger context) {
				super(context);
				this.dispatch = dispatch;
			}
			protected Iterable<TransformProvider<T>> _dispatch() {
				return dispatch;
			}
			public TransformProvider<T> withContext(Logger context) {
				return new DispatchFromProviderIterable<T>(dispatch, context);
			}
			@Override
			public String toString() {
				return "dispatch( " + join(_dispatch(), ", ") + " )";
			}
		}
		
		/* --------- */
		/* partial() */
		/* --------- */
		
		public static <T extends Transform> TransformProvider<T> partial(Query query, TransformProvider<T> provider) {
			return new Partial<T>(query, provider);
		}
		
		private static class Partial<T extends Transform> implements TransformProvider<T> {
			private final Query partialQuery;
			private final TransformProvider<T> provider;
			public Partial(Query partialQuery, TransformProvider<T> provider) {
				this.partialQuery = partialQuery;
				this.provider = provider;
			}
			public final Iterable<T> get(Query query) {
				return provider.get(mutableQuery().addAll(partialQuery).addAll(query));
			}
			public TransformProvider<T> withContext(Logger context) {
				return new Partial<T>(partialQuery, provider.withContext(context));
			}
			@Override
			public String toString() {
				return "partial( " + partialQuery + ", " + provider + " )";
			}
		}
		
		/* ------------------------ */
		/* logCreate(), logSelect() */
		/* ------------------------ */
		
		public static <T extends Transform> T logCreate(T t, Logger context) {
			context.debug("Created " + t);
			return t;
		}
		
		public static <T extends Transform> java.lang.Iterable<T> logSelect(final Query query,
		                                                                    final TransformProvider<T> provider,
		                                                                    final Logger context) {
			return new Iterable<T>() {
				public Iterator<T> iterator() {
					return new AbstractIterator<T>() {
						Iterator<T> i = provider.get(query).iterator();
						boolean first = true;
						public T computeNext() {
							if (!i.hasNext()) {
								if (first)
									context.trace("No match for query " + query);
								return endOfData(); }
							T t = i.next();
							context.debug("Selected " + t + " for query " + query);
							first = false;
							return t;
						}
					};
				}
			};
		}
		
		/* ------------ */
		/* varyLocale() */
		/* ------------ */
		
		public static <T extends Transform> TransformProvider<T> varyLocale(TransformProvider<T> delegate) {
			return new VaryLocale<T>(delegate, null);
		}
		
		private static class VaryLocale<T extends Transform>
				extends Provider.util.VaryLocale<Query,T> implements TransformProvider<T> {
			
			private final TransformProvider<T> delegate;
			private final Logger context;
			private VaryLocale(TransformProvider<T> delegate, Logger context) {
				this.delegate = delegate;
				this.context = context;
			}
			public Iterable<T> _get(Query query) {
				return delegate.withContext(context).get(query);
			}
			public Locale getLocale(Query query) {
				if (query.containsKey("locale"))
					try {
						return parseLocale(query.getOnly("locale").getValue().get()); }
					catch (IllegalArgumentException e) {
						logger.warn("Invalid locale", e);
						return null; }
				else
					return null;
			}
			public Query assocLocale(Query query, Locale locale) {
				MutableQuery q = mutableQuery(query);
				q.removeAll("locale");
				q.add("locale", locale.toLanguageTag());
				return q.asImmutable();
			}
			public TransformProvider<T> withContext(Logger context) {
				return new VaryLocale<T>(delegate, context);
			}
			@Override
			public String toString() {
				return "varyLocale( " + delegate + " )";
			}
		}
		
		private static final Logger logger = LoggerFactory.getLogger(TransformProvider.class);
		
	}
}
