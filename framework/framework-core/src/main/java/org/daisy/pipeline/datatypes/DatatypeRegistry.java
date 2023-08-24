package org.daisy.pipeline.datatypes;

import java.util.Map;

import com.google.common.base.Optional;
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
	private final Map<String,DatatypeService> registry = Maps.newHashMap();

	/**
	 * Get the datatype from its id.
	 */
	public Optional<DatatypeService> getDatatype(String id) {
		return Optional.fromNullable(this.registry.get(id));
	}

	/**
	 * Get all the datatypes.
	 */
	public Iterable<DatatypeService> getDatatypes() {
		return registry.values();
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
		logger.debug("Registering " + service.toString());
		registry.put(service.getId(), service);
	}

	public void unregister(DatatypeService service) {
		registry.remove(service.getId());
	}
}
