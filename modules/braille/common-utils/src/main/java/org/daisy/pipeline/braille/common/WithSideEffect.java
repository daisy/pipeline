package org.daisy.pipeline.braille.common;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;

public abstract class WithSideEffect<T,W> implements Function<W,T> {
	
	private T value = null;
	private boolean computed = false;
	private boolean absent = true;
	
	public final T apply(W world) throws NoSuchElementException {
		if (!computed) {
			sideEffectsBuilder = new ImmutableList.Builder<Function<? super W,?>>();
			firstWorld = world;
			try {
				value = _apply();
				absent = false; }
			finally {
				sideEffects = sideEffectsBuilder.build();
				sideEffectsBuilder = null;
				firstWorld = null;
				computed = true; }}
		else
			for (Function<? super W,?> sideEffect : sideEffects)
				sideEffect.apply(world);
		if (absent)
			throw new NoSuchElementException();
		return value;
	}
	
	protected abstract T _apply() throws NoSuchElementException;
	
	private List<Function<? super W,?>> sideEffects;
	private ImmutableList.Builder<Function<? super W,?>> sideEffectsBuilder;
	private W firstWorld;
	
	protected final <V> V __apply(final Function<? super W,? extends V> withSideEffect) {
		sideEffectsBuilder.add(withSideEffect);
		return withSideEffect.apply(firstWorld);
	}
	
	/* -- */
	/* of */
	/* -- */
		
	public static <T,W> WithSideEffect<T,W> of(final T value) {
		return new WithSideEffect<T,W>() {
			public T _apply() {
				return value;
			}
		};
	}
	
	/* ------------ */
	/* fromNullable */
	/* ------------ */
		
	public static <T,W> WithSideEffect<T,W> fromNullable(final T value) {
		return new WithSideEffect<T,W>() {
			public T _apply() {
				if (value == null)
					throw new NoSuchElementException();
				return value;
			}
		};
	}
	
	/* ================== */
	/*       UTILS        */
	/* ================== */
	
	public static abstract class util {
		
		/* -------- */
		/* Function */
		/* -------- */
		
		public static abstract class Function<F,T,W> implements com.google.common.base.Function<F,WithSideEffect<T,W>> {
			public abstract T _apply(F from);
			public final WithSideEffect<T,W> apply(final F from) {
				return new WithSideEffect<T,W>() {
					public T _apply() {
						synchronized(Function.this) {
							current = this;
							try {
								return Function.this._apply(from); }
							finally {
								current = null; }
						}
					}
				};
			}
			private WithSideEffect<T,W> current;
			protected final <V> V __apply(final com.google.common.base.Function<? super W,? extends V> withSideEffect) {
				return current.__apply(withSideEffect);
			}
		}
		
		/* -------- */
		/* Iterable */
		/* -------- */
		
		public static interface Iterable<T,W> extends com.google.common.base.Function<W,java.lang.Iterable<T>> {}
		
		/* --------- */
		/* Iterables */
		/* --------- */
		
		public static abstract class Iterables {
			
			/* empty */
			
			public static <T,W> Iterable<T,W> empty() {
				return of(Optional.<WithSideEffect<T,W>>absent().asSet());
			}
			
			/* of */
			
			public static <T,W> Iterable<T,W> of(WithSideEffect<T,W> element) {
				return of(Optional.of(element).asSet());
			}
			
			public static <T,W> Iterable<T,W> of(java.lang.Iterable<WithSideEffect<T,W>> iterable) {
				return new Of<T,W>(iterable);
			}
			
			protected static class Of<T,W> implements Iterable<T,W> {
				protected final java.lang.Iterable<WithSideEffect<T,W>> iterable;
				protected Of(java.lang.Iterable<WithSideEffect<T,W>> iterable) {
					this.iterable = iterable;
				}
				public java.lang.Iterable<T> apply(final W world) {
					return new java.lang.Iterable<T>() {
						public Iterator<T> iterator() {
							return new AbstractIterator<T>() {
								private Iterator<WithSideEffect<T,W>> from = iterable.iterator();
								protected T computeNext() {
									while (true) {
										WithSideEffect<T,W> next;
										try {
											next = from.next(); }
										catch (NoSuchElementException e) {
											return endOfData(); }
										try {
											return next.apply(world); }
										catch (NoSuchElementException e) {
											continue; }}
								}
							};
						}
					};
				}
			}
			
			/* transform */
			
			public static <F,T,W> Iterable<T,W> transform(final Iterable<F,W> from, final com.google.common.base.Function<F,T> function) {
				return new Iterable<T,W>() {
					public java.lang.Iterable<T> apply(W world) {
						return com.google.common.collect.Iterables.transform(from.apply(world), function);
					}
				};
			}
			
			public static <F,T,W> Iterable<T,W> transform(final Iterable<F,W> from, final Function<F,T,W> function) {
				return new Iterable<T,W>() {
					public java.lang.Iterable<T> apply(W world) {
						return of(
							com.google.common.collect.Iterables.transform(from.apply(world), function)
						).apply(world);
					}
				};
			}
			
			public static <F,T,W> Iterable<T,W> transform(final java.lang.Iterable<F> from, final Function<F,T,W> function) {
				return of(
					com.google.common.collect.Iterables.transform(from, function)
				);
			}
			
			/* concat */
			
			public static <T,W> Iterable<T,W> concat(final java.lang.Iterable<? extends Iterable<T,W>> inputs) {
				return new Concat<T,W>() {
					protected Iterator<? extends Iterable<T,W>> iterator(W world) {
						return inputs.iterator();
					}
				};
			}
			
			public static <T,W> Iterable<T,W> concat(final Iterable<? extends Iterable<T,W>,W> inputs) {
				return new Concat<T,W>() {
					protected Iterator<? extends Iterable<T,W>> iterator(W world) {
						return inputs.apply(world).iterator();
					}
				};
			}
			
			protected static abstract class Concat<T,W> implements Iterable<T,W> {
				protected abstract Iterator<? extends Iterable<T,W>> iterator(W world);
				public java.lang.Iterable<T> apply(final W world) {
					return new java.lang.Iterable<T>() {
						public Iterator<T> iterator() {
							return new AbstractIterator<T>() {
								Iterator<? extends Iterable<T,W>> iterableIterator = Concat.this.iterator(world);
								Iterator<T> current;
								protected T computeNext() {
									while (current == null || !current.hasNext()) {
										if (!iterableIterator.hasNext())
											return endOfData();
										current = iterableIterator.next().apply(world).iterator(); }
									return current.next();
								}
							};
						}
					};
				}
			}
		}
	}
}
