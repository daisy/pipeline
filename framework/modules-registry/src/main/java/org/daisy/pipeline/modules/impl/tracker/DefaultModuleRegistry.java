package org.daisy.pipeline.modules.impl.tracker;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Entity;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@org.osgi.service.component.annotations.Component(
	name = "module-registry",
	service = { ModuleRegistry.class }
)
public class DefaultModuleRegistry implements ModuleRegistry {

	private static final Logger logger = LoggerFactory
			.getLogger(DefaultModuleRegistry.class);

	private final HashMap<URI, Module> componentsMap = new HashMap<URI, Module>();
	private final HashMap<String, Module> entityMap = new HashMap<String, Module>();
	private final HashSet<Module> modules = new HashSet<Module>();

	public DefaultModuleRegistry() {
	}

	@Activate
	public void init() {
		logger.trace("Module registry up");
	}

	@Deactivate
	public void close() {
	}

	@Override
	public Iterator<Module> iterator() {
		return modules.iterator();
	}

	@Override
	public Module getModuleByComponent(URI uri) {
		return componentsMap.get(uri);
	}

	@Override
	public Module resolveDependency(URI component, Module source) {
		// TODO check cache, otherwise delegate to resolver
		return null;
	}

	@Override
	public Iterable<URI> getComponents() {
		return componentsMap.keySet();
	}

	@Reference(
		name = "Module",
		unbind = "removeModule",
		service = Module.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addModule(Module module) {
		logger.debug("Registring module {}", module.getName());
		modules.add(module);
		for (Component component : module.getComponents()) {
			logger.debug("  - {}", component.getURI());
			componentsMap.put(component.getURI(), module);
		}
		for (Entity entity: module.getEntities()) {
			logger.debug("  - {}", entity.getPublicId());
			entityMap.put(entity.getPublicId(), module);
		}
	}

	public void removeModule(Module module) {
		// FIXME: remove module
	}

	@Override
	public Module getModuleByEntity(String publicId) {
		return entityMap.get(publicId);
	}

	@Override
	public Iterable<String> getEntities() {
		return entityMap.keySet();
	}


}
