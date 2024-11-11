package org.daisy.pipeline.modules.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Lists;

import org.daisy.common.spi.ServiceLoader;
import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Entity;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.daisy.pipeline.modules.ResolutionException;
import org.daisy.pipeline.modules.XSLTPackage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

@org.osgi.service.component.annotations.Component(
	name = "module-registry",
	service = { ModuleRegistry.class }
)
public class DefaultModuleRegistry implements ModuleRegistry {

	private static final Logger logger = LoggerFactory.getLogger(DefaultModuleRegistry.class);

	private final HashMap<URI,Module> componentsMap = new HashMap<>();
	private final HashMap<String,Module> entityMap = new HashMap<>();
	private final HashMap<String,Module> xsltPackageMap = new HashMap<>();
	private final HashMap<URL,Module> codeSourceLocationMap = new HashMap<>();
	private final List<Module> modules = new ArrayList<>();
	private Iterator<Module> nextModules;
	private final LinkedList<Module> modulesBeingInitialized = new LinkedList<>();
	/*
	 * Note that thanks to SaxonConfigurator.setModuleRegistry(), which calls
	 * ModuleRegistry.iterator(), the module registry and all modules will be fully initialized
	 * before the web service goes up.
	*/
	private boolean initialized = false;
	private int detectEndlessRecursion = 0;

	public DefaultModuleRegistry() {
		if (OSGiHelper.inOSGiContext())
			nextModules = OSGiHelper.getModules();
		else
			nextModules = SPIHelper.getModules();
	}

	private Set<URI> components;
	private Set<String> entities;

	/**
	 * Whether some module satisfies {@code module.hasComponent(uri)}.
	 */
	private boolean hasComponent(URI uri) {
		if (components == null) {
			components = new HashSet<>();
			entities = new HashSet<>();
			for (Module m : modules) {
				components.addAll(m.getComponents());
				entities.addAll(m.getEntities());
			}
			for (Module m : modulesBeingInitialized) {
				components.addAll(m.getComponents());
				entities.addAll(m.getEntities());
			}
			if (nextModules.hasNext()) {
				// load modules only after DefaultModuleRegistry has been created,
				// because some module activator methods may use DefaultModuleRegistry
				List<Module> modules = Lists.newArrayList(nextModules);
				for (Module m : modules) {
					components.addAll(m.getComponents());
					entities.addAll(m.getEntities());
				}
				nextModules = modules.iterator();
			}
		}
		return components.contains(uri);
	}

	/**
	 * Whether some module satisfies {@code module.hasEntity(uri)}.
	 */
	private boolean hasEntity(String publicId) {
		if (entities == null)
			hasComponent(null); // initialize entities
		return entities.contains(publicId);
	}

	private void addNextModule() throws NoSuchElementException {
		if (initialized)
			throw new NoSuchElementException();
		do {
			Module module; {
				if (nextModules.hasNext())
					module = nextModules.next();
				else if (!modulesBeingInitialized.isEmpty()) {
					if (detectEndlessRecursion > 2 * modulesBeingInitialized.size())
						// An endless recursion is detected. This can happen either because a
						// dependency is missing, or because there is a circular dependency. We
						// assume that the recursive call comes from getModuleByComponent(),
						// getModuleByEntity(), getModuleByXSLTPackage() or getModuleByClass(). All
						// of these methods first check that the component/entity/package/class in
						// question is provided by one the modules (with unresolved dependencies),
						// before calling addNextModule(). So we can say that we've detected a
						// circular dependency.
						throw new RuntimeException("Circular dependency detected");
					module = modulesBeingInitialized.poll();
				} else {
					initialized = true;
					throw new NoSuchElementException();
				}
			}
			detectEndlessRecursion++;
			modulesBeingInitialized.add(module);
			try {
				// Resolve dependencies of the module. We do it here in ModuleRegistry so that a
				// module does not need to do it during construction. This allows module components
				// to depend on components of other modules. Because modules that make use of
				// ModuleRegistry may recursively call their own resolveDependencies() method, a
				// module may already be partly initialized when module.resolveDependencies() is
				// called.
				module.resolveDependencies();
			} catch (Throwable e) {
				logger.warn("An error happened while resolving dependencies of module " + module, e);
				continue;
			} finally {
				modulesBeingInitialized.remove(module);
			}
			if (modules.contains(module))
				// already added in a recursive call
				return;
			detectEndlessRecursion = 0;
			logger.debug("Adding module {}", module);
			modules.add(module);
			for (URI component : module.getComponents()) {
				try {
					module.getComponent(component);
					logger.debug("  - {}", component);
					componentsMap.put(component, module);
				} catch (ResolutionException e) {
					// ignore components that can not be resolved
				}
			}
			for (String entity: module.getEntities()) {
				try {
					module.getEntity(entity);
					logger.debug("  - {}", entity);
					entityMap.put(entity, module);
				} catch (ResolutionException e) {
					// ignore entities that can not be resolved
				}
			}
			for (XSLTPackage pack : module.getXSLTPackages()) {
				xsltPackageMap.put(pack.getName(), module);
			}
			URL codeSourceLocation = getCodeSourceLocation(module.getClass());
			if (codeSourceLocationMap.containsKey(codeSourceLocation))
				throw new IllegalStateException("Not more than one module is allowed in one bundle");
			codeSourceLocationMap.put(codeSourceLocation, module);
			logger.trace("Added module: "
			             + module.getClass().getName()
			             + "@" + Integer.toHexString(System.identityHashCode(module)));
			return;
		} while (true);
	}

	/**
	 * {@link Module} objects that call this method should be aware that this may call their own
	 * {@link Module#resolveDependencies} method.
	 */
	@Override
	public synchronized Iterator<Module> iterator() {
		if (initialized)
			return modules.iterator();
		return new Iterator<Module>() {
			private int index = 0;
			public boolean hasNext() {
				synchronized (DefaultModuleRegistry.this) {
					if (index < modules.size())
						return true;
					return nextModules.hasNext() || !modulesBeingInitialized.isEmpty();
				}
			}
			public Module next() throws NoSuchElementException {
				synchronized (DefaultModuleRegistry.this) {
					if (index >= modules.size())
						addNextModule();
					return modules.get(index++);
				}
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * {@link Module} objects that call this method should be aware that this may call their own
	 * {@link Module#resolveDependencies} method.
	 */
	@Override
	public synchronized Module getModuleByComponent(URI uri) {
		if (!hasComponent(uri))
			return null;
		do {
			Module module = componentsMap.get(uri);
			if (module != null)
				return module;
			for (Module m : modules)
				if (m.hasComponent(uri))
					// This means that a module provides the component, but
					// the component could not be resolved. It's no use
					// checking other modules.
					try {
						// Just to be sure check whether the component can
						// really not be resolved. This should result in a
						// ResolutionException.
						m.getComponent(uri);
						return m;
					} catch (ResolutionException e) {
						return null;
					}
			try {
				addNextModule();
			} catch (NoSuchElementException e) {
				break;
			}
		} while (true);
		return null;
	}

	/**
	 * {@link Module} objects that call this method should be aware that this may call their own
	 * {@link Module#resolveDependencies} method.
	 */
	@Override
	public synchronized Module getModuleByComponent(URI uri, String versionRange) {
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

	/**
	 * Returns only the components that could be resolved (unlike {@link Module#getComponents}).
	 *
	 * {@link Module} objects that call this method should be aware that this may call their own
	 * {@link Module#resolveDependencies} method.
	 */
	@Override
	public synchronized Iterable<URI> getComponents() {
		if (!initialized)
			while (true)
				try {
					addNextModule();
				} catch (NoSuchElementException e) {
					break;
				}
		return componentsMap.keySet();
	}

	/**
	 * {@link Module} objects that call this method should be aware that this may call their own
	 * {@link Module#resolveDependencies} method.
	 */
	@Override
	public synchronized Module getModuleByEntity(String publicId) {
		if (!hasEntity(publicId))
			return null;
		do {
			Module module = entityMap.get(publicId);
			if (module != null)
				return module;
			for (Module m : modules)
				if (m.hasEntity(publicId))
					// This means that a module provides the entity, but the
					// entity could not be resolved. It's no use checking
					// other modules.
					try {
						// Just to be sure check whether the entity can
						// really not be resolved. This should result in a
						// ResolutionException.
						m.getEntity(publicId);
						return m;
					} catch (ResolutionException e) {
						return null;
					}
			try {
				addNextModule();
			} catch (NoSuchElementException e) {
				break;
			}
		} while (true);
		return null;
	}

	/**
	 * Returns only the entities that could be resolved (unlike {@link Module#getEntities}).
	 *
	 * {@link Module} objects that call this method should be aware that this may call their own
	 * {@link Module#resolveDependencies} method.
	 */
	@Override
	public synchronized Iterable<String> getEntities() {
		if (!initialized)
			while (true)
				try {
					addNextModule();
				} catch (NoSuchElementException e) {
					break;
				}
		return entityMap.keySet();
	}

	/**
	 * {@link Module} objects that call this method should be aware that this may call their own
	 * {@link Module#resolveDependencies} method.
	 */
	@Override
	public synchronized Module getModuleByXSLTPackage(String name) {
		do {
			Module module = xsltPackageMap.get(name);
			if (module != null)
				return module;
			try {
				addNextModule();
			} catch (NoSuchElementException e) {
				break;
			}
		} while (true);
		return null;
	}

	/**
	 * {@link Module} objects that call this method should be aware that this may call their own
	 * {@link Module#resolveDependencies} method.
	 */
	@Override
	public synchronized Module getModuleByClass(Class<?> clazz) {
		URL location = getCodeSourceLocation(clazz);
		do {
			Module module = codeSourceLocationMap.get(location);
			if (module != null)
				return module;
			try {
				addNextModule();
			} catch (NoSuchElementException e) {
				break;
			}
		} while (true);
		return null;
	}

	private static URL getCodeSourceLocation(Class<?> clazz) throws IllegalArgumentException {
		URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
		if (!(location.toString().startsWith("file:") || location.toString().startsWith("bundle:")  || location.toString().startsWith("mvn:")))
			throw new RuntimeException("unexpected code source location: " + location);
		return location;
	}

	// static nested class in order to delay class loading
	private static abstract class OSGiHelper {

		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}

		static Iterator<Module> getModules() {
			return new Iterator<Module>() {
				private BundleContext bc = FrameworkUtil.getBundle(DefaultModuleRegistry.class).getBundleContext();
				private ServiceReference[] refs;
				private int index = 0;  {
					try {
						refs = bc.getServiceReferences(Module.class.getName(), null);
					} catch (InvalidSyntaxException e) {
						throw new IllegalStateException(e); // should not happen
					}
				}
				public boolean hasNext() {
					return refs != null && index < refs.length;
				}
				public Module next() throws NoSuchElementException {
					if (!hasNext())
						throw new NoSuchElementException();
					return (Module)bc.getService(refs[index++]);
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}

	// static nested class in order to delay class loading
	private static abstract class SPIHelper {
		static Iterator<Module> getModules() {
			return ServiceLoader.load(Module.class).iterator();
		}
	}
}
