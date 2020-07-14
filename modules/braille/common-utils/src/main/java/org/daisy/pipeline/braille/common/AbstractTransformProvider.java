package org.daisy.pipeline.braille.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;

import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.util.Function0;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTransformProvider<T extends Transform> implements TransformProvider<T> {
	
	private Map<Query,Iterable<T>> transformCache = new HashMap<Query,Iterable<T>>();
	
	private Map<Logger,TransformProvider<T>> providerCache = new HashMap<Logger,TransformProvider<T>>();
	
	protected abstract Iterable<T> _get(Query query);
	
	// WARNING: using "(id:<ID>)" in a query where <ID> is the result of a
	// call to AbstractTransform#getIdentifier() will only work when that
	// transformer came from the exact same provider and its cache is not
	// cleared in the meantime.
	private final java.lang.Iterable<T> get(Query query, Logger context) {
		MutableQuery q = mutableQuery(query);
		if (q.containsKey("id")) {
			Feature f = q.removeOnly("id");
			if (q.isEmpty())
				return Optional.fromNullable(fromId(f.getValue().get())).asSet(); }
		Iterable<T> i;
		if (transformCache.containsKey(query))
			i = transformCache.get(query);
		else {
			// memoize() doesn't make sense
			i = util.Iterables.memoize(_get(query));
			transformCache.put(query, i); }
		return rememberId(i.apply(context));
	}
	
	public java.lang.Iterable<T> get(Query query) {
		return get(query, null);
	}
	
	public final TransformProvider<T> withContext(Logger context) {
		if (providerCache.containsKey(context))
			return providerCache.get(context);
		TransformProvider<T> provider = new DerivativeProvider(context);
		providerCache.put(context, provider);
		return provider;
	}
	
	public void invalidateCache() {
		transformCache.clear();
	}
	
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(this);
	}
	
	@Override
	public String toString() {
		return toStringHelper().add("context", null).toString();
	}
	
	private class DerivativeProvider implements TransformProvider<T> {
		private final Logger context;
		private DerivativeProvider(final Logger context) {
			this.context = context;
		}
		public java.lang.Iterable<T> get(Query query) {
			return AbstractTransformProvider.this.get(query, context);
		}
		public TransformProvider<T> withContext(Logger context) {
			return AbstractTransformProvider.this.withContext(context);
		}
		@Override
		public String toString() {
			return AbstractTransformProvider.this.toStringHelper().add("context", context).toString();
		}
	}
	
	private Map<String,T> fromId = new HashMap<String,T>();
	
	protected T fromId(String id) {
		return fromId.get(id);
	}
	
	private java.lang.Iterable<T> rememberId(final java.lang.Iterable<T> iterable) {
		return new java.lang.Iterable<T>() {
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					Iterator<T> i = iterable.iterator();
					public boolean hasNext() {
						return i.hasNext();
					}
					public T next() {
						T t = i.next();
						fromId.put(t.getIdentifier(), t);
						return t;
					}
					public void remove() {
						i.remove();
					}
				};
			}
		};
	}
	
	protected Function0<Void> provideTemporarily(T t) {
		// assumes t is not in cache yet and is unique to this call
		fromId.put(t.getIdentifier(), t);
		return () -> {
			fromId.remove(t.getIdentifier());
			// also remove from transformCache
			transformCache.remove(mutableQuery().add("id", t.getIdentifier()));
			return null;
		};
	}
	
	/* -------- */
	/* Iterable */
	/* -------- */
	
	protected interface Iterable<T> extends WithSideEffect.util.Iterable<T,Logger>,
	                                        java.lang.Iterable<WithSideEffect<T,Logger>> {}
	
	/* ================== */
	/*       UTILS        */
	/* ================== */
	
	public static abstract class util {
		
		/* ------------------------ */
		/* logCreate(), logSelect() */
		/* ------------------------ */
		
		public static <T extends Transform> WithSideEffect<T,Logger> logCreate(final T t) {
			return new WithSideEffect<T,Logger>() {
				public T _apply() {
					__apply(debug("Created " + t));
					return t; }};
		}
		
		public static <T extends Transform> Iterable<T> logSelect(final Query query,
		                                                          final TransformProvider<T> provider) {
			// not using provider.withContext() because memoizing only makes sense if sub-providers
			// have no side-effects and provided transformers have no context
			return logSelect(query, provider.withContext(null).get(query));
		}
		
		public static <T extends Transform> Iterable<T> logSelect(final Query query,
		                                                          final java.lang.Iterable<T> iterable) {
			return Iterables.of(
				new java.lang.Iterable<WithSideEffect<T,Logger>>() {
					public Iterator<WithSideEffect<T,Logger>> iterator() {
						return new Iterator<WithSideEffect<T,Logger>>() {
							Iterator<T> i = iterable.iterator();
							boolean first = true;
							public boolean hasNext() {
								if (i == null)
									return true;
								return i.hasNext();
							}
							public WithSideEffect<T,Logger> next() {
								final T t;
								if (first) {
									first = false;
									try { t = i.next(); }
									catch (final NoSuchElementException e) {
										return new WithSideEffect<T,Logger>() {
											public T _apply() {
												__apply(debug("No match for query " + query));
												throw e;
											}
										};
									}
								} else
									t = i.next();
								return new WithSideEffect<T,Logger>() {
									public T _apply() {
										__apply(info("Selected " + t + " for query " + query));
										return t;
									}
								};
							}
							public void remove() {
								throw new UnsupportedOperationException();
							}
						};
					}
				}
			);
		}
		
		private static final Logger fallbackLogger = LoggerFactory.getLogger(AbstractTransformProvider.class);
		
		public static com.google.common.base.Function<Logger,Void> debug(final String message) {
			return new com.google.common.base.Function<Logger,Void>() {
				public Void apply(Logger logger) {
					if (logger != null)
						logger.debug(message);
					else
						fallbackLogger.debug(message);
					return null;
				}
			};
		}
		
		public static com.google.common.base.Function<Logger,Void> info(final String message) {
			return new com.google.common.base.Function<Logger,Void>() {
				public Void apply(Logger logger) {
					if (logger != null)
						logger.info(message);
					else
						fallbackLogger.debug(message);
					return null;
				}
			};
		}
		
		public static com.google.common.base.Function<Logger,Void> warn(final String message) {
			return new com.google.common.base.Function<Logger,Void>() {
				public Void apply(Logger logger) {
					if (logger != null)
						logger.warn(message);
					else
						fallbackLogger.debug(message);
					return null;
				}
			};
		}
		
		/* -------- */
		/* Function */
		/* -------- */
		
		public static abstract class Function<F,T> extends WithSideEffect.util.Function<F,T,Logger> {}
		
		/* --------- */
		/* Iterables */
		/* --------- */
		
		public static abstract class Iterables {
			
			/* memoize() */
			
			public static <T> Iterable<T> memoize(Iterable<T> iterable) {
				return of(org.daisy.pipeline.braille.common.util.Iterables.memoize(iterable));
			}
			
			/* empty() */
			
			public static <T> Iterable<T> empty() {
				return of(Optional.<WithSideEffect<T,Logger>>absent().asSet());
			}
			
			/* fromNullable() */
			
			public static <T> Iterable<T> fromNullable(T element) {
				return of(WithSideEffect.<T,Logger>fromNullable(element));
			}
			
			/* of() */
			
			public static <T> Iterable<T> of(T element) {
				return of(WithSideEffect.<T,Logger>of(element));
			}
			
			public static <T> Iterable<T> of(WithSideEffect<T,Logger> element) {
				return of(Optional.of(element).asSet());
			}
			
			public static <T> Iterable<T> of(final java.lang.Iterable<WithSideEffect<T,Logger>> iterable) {
				return new Of<T>(iterable);
			}
			
			private static class Of<T> extends WithSideEffect.util.Iterables.Of<T,Logger> implements Iterable<T> {
				protected Of(java.lang.Iterable<WithSideEffect<T,Logger>> iterable) {
					super(iterable);
				}
				public Iterator<WithSideEffect<T,Logger>> iterator() {
					return iterable.iterator();
				}
			}
			
			/* transform() */
			
			public static <F,T> Iterable<T> transform(Iterable<F> from,
			                                          final com.google.common.base.Function<F,T> function) {
				return transform(
					from,
					new Function<F,T>() {
						public T _apply(F from) {
							return function.apply(from);
						}
					}
				);
			}
			
			public static <F,T> Iterable<T> transform(Iterable<F> from, final Function<F,T> function) {
				return of(
					com.google.common.collect.Iterables.transform(
						from,
						new Function<WithSideEffect<F,Logger>,T>() {
							public T _apply(WithSideEffect<F,Logger> from) {
								return __apply(function.apply(__apply(from)));
							}
						}
					)
				);
			}
			
			public static <F,T> Iterable<T> transform(final java.lang.Iterable<F> from, final Function<F,T> function) {
				return of(
					com.google.common.collect.Iterables.transform(from, function)
				);
			}
			
			/* concat() */
			
			public static <T> Iterable<T> concat(Iterable<T> a, T b) {
				return of(
					com.google.common.collect.Iterables.concat(a, of(b))
				);
			}
			
			public static <T> Iterable<T> concat(Iterable<T> a, WithSideEffect<T,Logger> b) {
				return of(
					com.google.common.collect.Iterables.concat(a, of(b))
				);
			}
			
			public static <T> Iterable<T> concat(Iterable<T> a, Iterable<T> b) {
				return of(
					com.google.common.collect.Iterables.concat(a, b)
				);
			}
			
			public static <T> Iterable<T> concat(final java.lang.Iterable<? extends Iterable<T>> inputs) {
				return of(
					com.google.common.collect.Iterables.concat(inputs)
				);
			}
			
			public static <T> Iterable<T> concat(final Iterable<? extends Iterable<T>> inputs) {
				return new Concat<T>() {
					protected Iterator<? extends Iterable<T>> iterator(Logger context) {
						return inputs.apply(context).iterator();
					}
					public Iterator<WithSideEffect<T,Logger>> iterator() {
						return new AbstractIterator<WithSideEffect<T,Logger>>() {
							Iterator<? extends WithSideEffect<? extends Iterable<T>,Logger>> iterableIterator = inputs.iterator();
							Iterator<WithSideEffect<T,Logger>> current;
							WithSideEffect<T,Logger> nextEvaluate;
							boolean evaluated = true;
							protected WithSideEffect<T,Logger> computeNext() {
								if (!evaluated)
									throw new RuntimeException("Previous element must be evaluated first");
								if (current != null && current.hasNext())
									return current.next();
								else if (!iterableIterator.hasNext())
									return endOfData();
								nextEvaluate = new WithSideEffect<T,Logger>() {
									public T _apply() throws Throwable {
										if (nextEvaluate == this)
											evaluated = true;
										while (current == null || !current.hasNext()) {
											try {
												current = __apply(iterableIterator.next()).iterator();
												break; }
											catch (WithSideEffect.Exception e) {
												continue; }}
										try {
											return __apply(current.next()); }
										catch (WithSideEffect.Exception e) {
											throw e.getCause(); }
									}
								};
								evaluated = false;
								return nextEvaluate;
							}
						};
					}
				};
			}
			
			protected static abstract class Concat<T> extends WithSideEffect.util.Iterables.Concat<T,Logger>
			                                          implements Iterable<T> {}
			
			/* intersection() */
			
			public static <T> Iterable<T> intersection(Iterable<T> a, Iterable<T> b) {
				return of(
					new java.lang.Iterable<WithSideEffect<T,Logger>>() {
						public Iterator<WithSideEffect<T,Logger>> iterator() {
							return new AbstractIterator<WithSideEffect<T,Logger>>() {
								Iterator<WithSideEffect<T,Logger>> itrA = a.iterator();
								Iterator<WithSideEffect<T,Logger>> itrB = b.iterator();
								Set<T> returned = new HashSet<>();
								Set<T> setB = new HashSet<>();
								protected WithSideEffect<T,Logger> computeNext() {
									if (!itrA.hasNext())
										return endOfData();
									return new WithSideEffect<T,Logger>() {
										public T _apply() throws Throwable {
											while (itrA.hasNext()) {
												T nextA; {
													try {
														nextA = __apply(itrA.next()); }
													catch (WithSideEffect.Exception e) {
														continue; }}
												if (returned.contains(nextA))
													continue;
												if (setB.contains(nextA)) {
													returned.add(nextA);
													return nextA; }
												while (itrB.hasNext())
													try {
														T nextB = __apply(itrB.next());
														setB.add(nextB);
														if (Objects.equal(nextA, nextB)) {
															returned.add(nextA);
															return nextA; }}
													catch (WithSideEffect.Exception e) {}
											}
											throw new NoSuchElementException();
										}
									};
								}
							};
						}
					}
				);
			}
		}
	}
}
