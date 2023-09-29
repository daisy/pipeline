package org.daisy.common.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;

import com.google.common.collect.AbstractIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLoader<S> implements Iterable<S> {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceLoader.class);
	
	private static final Map<Class<?>,ServiceLoader<?>> cache = new HashMap<Class<?>,ServiceLoader<?>>();
	
	private static ClassLoader lastContextClassLoader = null;
	
	public static <S> ServiceLoader<S> load(Class<S> serviceType) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader != lastContextClassLoader) {
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
		lastContextClassLoader = classLoader;
		ServiceLoader<S> loader;
		if (cache.containsKey(serviceType)) {
			loader = (ServiceLoader<S>)cache.get(serviceType);
		} else {
			loader = new ServiceLoader<S>(serviceType, classLoader);
			cache.put(serviceType, loader);
		}
		return loader;
	}
	
	private final Class<S> serviceType;
	private Iterable<S> serviceLoader;
	private final ClassLoader classLoader;
	
	private ServiceLoader(Class<S> serviceType, ClassLoader classLoader) {
		this.serviceType = serviceType;
		this.classLoader = classLoader;
	}
	
	public Iterator<S> iterator() {
		return new AbstractIterator<S>() {
			Iterator<S> serviceIterator;
			public S computeNext() {
				ClassLoader restoreClassLoader = Thread.currentThread().getContextClassLoader();
				if (classLoader != restoreClassLoader)
					Thread.currentThread().setContextClassLoader(classLoader);
				try {
					if (serviceIterator == null) {
						try {
							if (serviceLoader == null)
								serviceLoader = memoize(serviceType, java.util.ServiceLoader.load(serviceType));
							serviceIterator = serviceLoader.iterator();
						} catch (Throwable e) {
							logger.error("Failed to load service providers", e);
							return endOfData();
						}
					}
					while (serviceIterator.hasNext()) {
						try {
							return serviceIterator.next();
						} catch (Throwable e) {
							if (e instanceof ServiceConfigurationError && e.getCause() instanceof ActivationException) {
								logger.info(e.getMessage() + ": " + e.getCause().getMessage());
								if (e.getCause().getCause() != null)
									logger.trace("Cause:", e.getCause().getCause());
							} else {
								logger.error("Failed to instantiate provider of service '" + serviceType.getCanonicalName() + "'", e);
							}
						}
					}
					return endOfData();
				} finally {
					if (classLoader != restoreClassLoader)
						Thread.currentThread().setContextClassLoader(restoreClassLoader);
				}
			}
		};
	}
	
	/*
	 * No two Iterables returned by this function contain multiple instances of the same class
	 */
	private static <S> Iterable<S> memoize(Class<S> serviceType, final Iterable<S> iterable) {
		return new Memoize<S>(serviceType) {
			protected Iterator<S> _iterator() {
				return iterable.iterator();
			}
		};
	}
	
	private static abstract class Memoize<S> implements Iterable<S> {
		/* set of previously returned objects */
		private static final Map<Class<?>,Object> singletons = new HashMap<Class<?>,Object>();
		private final Class<S> serviceType;
		private final List<S> list = new ArrayList<S>();
		protected abstract Iterator<S> _iterator();
		private Iterator<S> _iterator;
		protected Memoize(Class<S> serviceType) {
			this.serviceType = serviceType;
		}
		public final Iterator<S> iterator() {
			return new Iterator<S>() {
				private int index = 0;
				public boolean hasNext() {
					synchronized(list) {
						if (index < list.size())
							return true;
						if (_iterator == null)
							_iterator = _iterator();
						return _iterator.hasNext(); // this does not try to instantiate any objects yet
					}
				}
				// Note that it could happen that this call recursively calls itself, in a different
				// Iterator object but with the same underlying Iterable
				// (java.util.ServiceLoader). java.util.ServiceLoader can cope with that just fine,
				// and we are taking the necessary precautions to handle it here as well.
				public S next() throws NoSuchElementException {
					synchronized(list) {
						if (index < list.size())
							return list.get(index++);
						if (_iterator == null)
							_iterator = _iterator();
						S next = _iterator.next();
						synchronized(singletons) {
							if (singletons.containsKey(next.getClass())) {
								logger.trace("Object of type " + next.getClass() + " already loaded. Returning same object.");
								next = (S)singletons.get(next.getClass());
							} else
								singletons.put(next.getClass(), next);
						}
						list.add(next);
						return list.get(index++); // not returning next because next items may have
						                          // been added to the list in the meantime
					}
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
}
