package org.daisy.common.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.AbstractIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLoader<S> implements Iterable<S> {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceLoader.class);
	
	private static final Map<Class<?>,ServiceLoader<?>> cache = new HashMap<Class<?>,ServiceLoader<?>>();
	
	private static ClassLoader lastContextClassLoader = null;
	
	public static <S> ServiceLoader<S> load(Class<S> serviceType) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		if (ccl != lastContextClassLoader) {
			cache.clear();
			synchronized (Memoize.singletons) {
				for (Object o : Memoize.singletons.values())
					if (o instanceof ServiceWithProperties)
						try {
							((ServiceWithProperties)o).spi_deactivate();
						} catch (AbstractMethodError e) {}
				Memoize.singletons.clear();
			}
		}
		lastContextClassLoader = ccl;
		ServiceLoader<S> loader;
		if (cache.containsKey(serviceType)) {
			loader = (ServiceLoader<S>)cache.get(serviceType);
		} else {
			loader = new ServiceLoader<S>(serviceType);
			cache.put(serviceType, loader);
		}
		return loader;
	}
	
	private Class<S> serviceType;
	private Iterable<S> serviceLoader;
	
	private ServiceLoader(Class<S> serviceType) {
		this.serviceType = serviceType;
	}
	
	public Iterator<S> iterator() {
		return new AbstractIterator<S>() {
			Iterator<S> serviceIterator;
			public S computeNext() {
				if (serviceIterator == null) {
					try {
						if (serviceLoader == null) {
							serviceLoader = memoize(java.util.ServiceLoader.load(serviceType));
						}
						serviceIterator = serviceLoader.iterator();
					} catch (Throwable e) {
						logger.error("Failed to instantiate services", e);
						return endOfData();
					}
				}
				while (serviceIterator.hasNext()) {
					try {
						return serviceIterator.next();
					} catch (Throwable e) {
						logger.error("Failed to instantiate service", e);
					}
				}
				return endOfData();
			}
		};
	}
	
	/*
	 * No two Iterables returned by this function contain multiple instances of the same class
	 */
	private static <S> Iterable<S> memoize(final Iterable<S> iterable) {
		return new Memoize<S>() {
			protected Iterator<S> _iterator() {
				return iterable.iterator();
			}
		};
	}
	
	private static abstract class Memoize<S> implements Iterable<S> {
		/* set of previously returned objects */
		private static final Map<Class<?>,Object> singletons = new HashMap<Class<?>,Object>();
		private final ArrayList<S> list = new ArrayList<S>();
		protected abstract Iterator<S> _iterator();
		private Iterator<S> _iterator;
		public final Iterator<S> iterator() {
			return new Iterator<S>() {
				private int index = 0;
				public boolean hasNext() {
					synchronized(list) {
						if (index < list.size())
							return true;
						if (_iterator == null)
							_iterator = _iterator();
						return _iterator.hasNext();
					}
				}
				public S next() throws NoSuchElementException {
					synchronized(list) {
						if (index < list.size())
							return list.get(index++);
						if (_iterator == null)
							_iterator = _iterator();
						S next = _iterator.next();
						synchronized(singletons) {
							if (singletons.containsKey(next.getClass()))
								next = (S)singletons.get(next.getClass());
							else
								singletons.put(next.getClass(), next);
						}
						list.add(next);
						index++;
						return next;
					}
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
}
