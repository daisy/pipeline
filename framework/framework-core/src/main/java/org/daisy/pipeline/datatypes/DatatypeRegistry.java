package org.daisy.pipeline.datatypes;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "datatype-registry",
	service = { DatatypeRegistry.class }
)
/**
 * Keeps track of the registered datatypes.
 */
public class DatatypeRegistry {

	private static final Logger logger = LoggerFactory.getLogger(DatatypeRegistry.class);
	/**
	 * The number of seconds volatile datatypes stay in registry.
	 */
	private static final long TIMEOUT = 60;

	private final Map<String,DatatypeService> registry = Maps.newHashMap();
	private final Map<String,DatatypeService> volatileRegistry = CacheBuilder.newBuilder()
	                                                                         .expireAfterAccess(TIMEOUT, TimeUnit.SECONDS)
	                                                                         .<String,DatatypeService>build()
	                                                                         .asMap();

	/**
	 * Get the datatype from its id.
	 */
	public Optional<DatatypeService> getDatatype(String id) {
		synchronized (registry) {
			DatatypeService t = registry.get(id);
			if (t != null)
				return Optional.of(t);
		}
		synchronized (volatileRegistry) {
			return Optional.fromNullable(volatileRegistry.get(id));
		}
	}

	/**
	 * Get all the persistent datatypes
	 *
	 * This does not include the volatile datatypes (datatypes that were registred using {@link
	 * #registerVolatile(DatatypeService)}).
	 */
	public Iterable<DatatypeService> getDatatypes() {
		synchronized (registry) {
			return registry.values();
		}
	}

	@Activate
	protected void activate(){
		logger.debug("Activating datatype registry");
	}

	@Reference(
		name = "datatype-services",
		unbind = "-",
		service = DatatypeService.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void register(DatatypeService service) {
		if (service.getId() == null)
			throw new IllegalArgumentException();
		synchronized (registry) {
			logger.debug("Registering " + service.toString());
			registry.put(service.getId(), service);
		}
	}

	public void unregister(DatatypeService service) {
		synchronized (registry) {
			registry.remove(service.getId());
		}
	}

	/**
	 * Register a datatype once its {@link DatatypeService#getId()} method is accessed, and dismiss
	 * it automatically after it has not been accessed for 60 seconds.
	 *
	 * @return The supplied {@link DatatypeService}, but modified so that it does not return {@code null}.
	 */
	public DatatypeService registerVolatile(DatatypeService service) {
		String origId = service.getId();
		service.id = new Supplier<String>() {
			String id = origId;
			@Override
			public String get() {
				synchronized (volatileRegistry) {
					if (id == null)
						// generate an ID that is not yet taken
						synchronized (registry) {
							while (true) {
								id = UUID.randomUUID().toString();
								if (!registry.containsKey(id) && !volatileRegistry.containsKey(id))
									break; }}
					if (!volatileRegistry.containsKey(id)) {
						volatileRegistry.put(id, service);
						logger.debug("Registering volatile " + service.toString());
					}
				}
				return id;
			}
		};
		return service;
	}
}
