package org.daisy.pipeline.modules.impl;

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

import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
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

	private final HashMap<URI,Module> componentsMap = new HashMap<>();
	private final HashMap<String,Module> entityMap = new HashMap<>();
	private final HashMap<URL,Module> codeSourceLocationMap = new HashMap<>();
	private final HashSet<Module> modules = new HashSet<>();

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
	public Module getModuleByComponent(URI uri, String versionRange) {
		Module module = getModuleByComponent(uri);
		if (module != null && versionRange != null) {
			Component component = module.getComponent(uri);
			if (component == null)
				throw new IllegalStateException("coding error"); // can not happen
			Version version; {
				try {
					version = Version.parseVersion(component.getVersion());
				} catch (IllegalArgumentException e) {
					logger.debug("Version of " + uri + " can not be parsed: " + component.getVersion());
					return null;
				}
			}
			try {
				if (!(new VersionRange(versionRange).includes(version))) {
					logger.debug("Version of " + uri + " (" + version + ") not in requested range (" + versionRange + ")");
					return null;
				}
			} catch (IllegalArgumentException e) {
				logger.debug("Version range can not be parsed: " + versionRange);
				return null;
			}
		}
		return module;
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
		logger.debug("Adding module {}", module.getName());
		modules.add(module);
		for (Component component : module.getComponents()) {
			logger.debug("  - {}", component.getURI());
			componentsMap.put(component.getURI(), module);
		}
		for (Entity entity: module.getEntities()) {
			logger.debug("  - {}", entity.getPublicId());
			entityMap.put(entity.getPublicId(), module);
		}
		URL codeSourceLocation = getCodeSourceLocation(module.getClass());
		if (codeSourceLocationMap.containsKey(codeSourceLocation))
			throw new IllegalStateException("Not more than one module is allowed in one bundle");
		codeSourceLocationMap.put(codeSourceLocation, module);
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

	@Override
	public Module getModuleByClass(Class<?> clazz) {
		return codeSourceLocationMap.get(getCodeSourceLocation(clazz));
	}

	private static URL getCodeSourceLocation(Class<?> clazz) throws IllegalArgumentException {
		URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
		if (!(location.toString().startsWith("file:") || location.toString().startsWith("bundle:")))
			throw new RuntimeException("unexpected code source location: " + location);
		return location;
	}
}
