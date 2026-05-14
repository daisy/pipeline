package org.daisy.pipeline.braille.common;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

import org.daisy.pipeline.braille.common.util.Function2;
import org.daisy.pipeline.braille.common.util.Iterables;

public interface Provider<Q,X> {
	
	/**
	 * Get a collection of objects based on a query.
	 * @param query is assumed to be immutable
	 * @return The objects for the query, in order of best match.
	 */
	public Iterable<X> get(Q query);
	
	/* ================== */
	/*       UTILS        */
	/* ================== */
	
	public static abstract class util {
		
		/* ------- */
		/* empty() */
		/* ------- */
		
		public static <Q,X> Provider<Q,X> empty() {
			return new Provider<Q,X>() {
				public Iterable<X> get(Q query) {
					return Optional.<X>absent().asSet();
				}
			};
		}
		
		/* --------- */
		/* memoize() */
		/* --------- */
		
		public interface MemoizingProvider<Q,X> extends Memoizing<Q,Iterable<X>>, Provider<Q,X> {}
		
		/**
		 * provider.get(query) must not mutate query
		 */
		public static <Q,X> MemoizingProvider<Q,X> memoize(final Provider<Q,X> provider) {
			return new Memoize<Q,X>() {
				protected Iterable<X> _get(Q query) {
					return Iterables.memoize(provider.get(query));
				}
			};
		}
		
		public static abstract class Memoize<Q,X> extends Memoizing.util.AbstractMemoizing<Q,Iterable<X>> implements MemoizingProvider<Q,X> {
			protected Memoize() {
				super();
			}
			protected Memoize(Memoize<Q,X> shareCacheWith) {
				super(shareCacheWith);
			}
			protected abstract Iterable<X> _get(Q query);
			public Iterable<X> get(Q query) {
				return apply(query);
			}
			protected final Iterable<X> _apply(Q query) {
				return _get(query);
			}
		}
		
		/* --------------------- */
		/* SimpleMappingProvider */
		/* --------------------- */
		
		public static abstract class SimpleMappingProvider<Q,X> implements Provider<Q,X> {
			private Map<Q,String> map;
			public abstract Q parseKey(String key);
			public abstract X parseValue(String value);
			public SimpleMappingProvider(URL properties) {
				map = readProperties(properties);
			}
			public Iterable<X> get(Q query) {
				String value = map.get(query);
				if (value != null)
					return Optional.fromNullable(parseValue(value)).asSet();
				return Optional.<X>absent().asSet();
			}
			private Map<Q,String> readProperties(URL url) {
				Map<Q,String> map = new HashMap<Q,String>();
				try {
					url.openConnection();
					InputStream reader = url.openStream();
					Properties properties = new Properties();
					properties.loadFromXML(reader);
					for (String key : properties.stringPropertyNames()) {
						Q query = parseKey(key);
						if (!map.containsKey(query))
							map.put(query, properties.getProperty(key)); }
					reader.close();
					return new ImmutableMap.Builder<Q,String>().putAll(map).build(); }
				catch (Exception e) {
					throw new RuntimeException("Could not read properties file " + url, e); }
			}
		}
		
		/* ---------- */
		/* dispatch() */
		/* ---------- */
		
		public static <Q,X> Provider<Q,X> dispatch(final Iterable<? extends Provider<Q,X>> dispatch) {
			return new Dispatch<Q,X>() {
				@SuppressWarnings(
					"unchecked" // safe cast to Iterable<Provider<Q,X>>
				)
				public Iterable<Provider<Q,X>> dispatch() {
					return (Iterable<Provider<Q,X>>)dispatch;
				}
			};
		}
		
		public static abstract class Dispatch<Q,X> implements Provider<Q,X> {
			public abstract Iterable<Provider<Q,X>> dispatch();
			public Iterable<X> get(final Q query) {
				return concat(transform(
					dispatch(),
					new Function<Provider<Q,X>,Iterable<X>>() {
						public Iterable<X> apply(Provider<Q,X> provider) {
							return provider.get(query); }}));
			}
		}
		
		/* ------------ */
		/* varyLocale() */
		/* ------------ */
		
		public static <X> Provider<Locale,X> varyLocale(final Provider<Locale,X> delegate) {
			return new VaryLocale<Locale,X>() {
				public Iterable<X> _get(Locale locale) {
					return delegate.get(locale);
				}
			};
		}
		
		public static <Q,X> Provider<Q,X> varyLocale(final Provider<Q,X> delegate,
		                                             final Function<Q,Locale> getLocale,
		                                             final Function2<Q,Locale,Q> assocLocale) {
			return new VaryLocale<Q,X>() {
				public Iterable<? extends X> _get(Q query) {
					return delegate.get(query);
				}
				public Locale getLocale(Q query) {
					return getLocale.apply(query);
				}
				public Q assocLocale(Q query, Locale locale) {
					return assocLocale.apply(query, locale);
				}
			};
		}
		
		public static abstract class VaryLocale<Q,X> implements Provider<Q,X> {
			public abstract Iterable<? extends X> _get(Q query);
			public abstract Locale getLocale(Q query);
			/**
			 * @param query must not be mutated
			 * @param locale
			 * @return a new query with the locale feature "added"
			 */
			public abstract Q assocLocale(Q query, Locale locale);
			public Locale getLocale(Locale query) {
				return query;
			}
			public Locale assocLocale(Locale query, Locale locale) {
				return locale;
			}
			public final Iterable<X> get(final Q query) {
				return new Iterable<X>() {
					public Iterator<X> iterator() {
						return new Iterator<X>() {
							Iterator<? extends X> next = null;
							int tryNext = 1;
							Locale locale = getLocale(query);
							public boolean hasNext() {
								while (next == null || !next.hasNext()) {
									switch (tryNext) {
									case 1:
										tryNext++;
										next = _get(query).iterator();
										if (locale == null || "".equals(locale.toString()))
											tryNext = 4;
										break;
									case 2:
										tryNext++;
										if (!"".equals(locale.getVariant()))
											next = _get(assocLocale(query, new Locale(locale.getLanguage(), locale.getCountry()))).iterator();
										break;
									case 3:
										tryNext++;
										if (!"".equals(locale.getCountry()))
											next = _get(assocLocale(query, new Locale(locale.getLanguage()))).iterator();
										break;
									case 4:
										tryNext++;
										next = fallback(query).iterator();
										break;
									default:
										return false; }}
								return true;
							}
							public X next() {
								if (!hasNext()) throw new NoSuchElementException();
								return next.next();
							}
							public void remove() {
								throw new UnsupportedOperationException();
							}
						};
					}
				};
			}
			public Iterable<X> fallback(Q query) {
				return Optional.<X>absent().asSet();
			}
		}
	}
}
