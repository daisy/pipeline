package org.daisy.pipeline.modules.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.daisy.common.spi.ServiceLoader;
import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Entity;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
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
	private final Iterator<Module> nextModules;
	private final LinkedList<Module> modulesBeingInitialized = new LinkedList<>();
	private boolean initialized = false;
	private int detectEndlessRecursion = 0;

	public DefaultModuleRegistry() {
		if (OSGiHelper.inOSGiContext())
			nextModules = OSGiHelper.getModules();
		else
			nextModules = SPIHelper.getModules();
	}

	private void addNextModule() throws NoSuchElementException {
		if (initialized)
			throw new NoSuchElementException();
		do {
			Module module; {
				if (nextModules.hasNext())
					module = nextModules.next();
				else if (!modulesBeingInitialized.isEmpty()) {
					if (detectEndlessRecursion > modulesBeingInitialized.size())
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
				// Initialize the module. We do it here in ModuleRegistry so that a module does not
				// need to initialize itself during construction. This allows module components to
				// depend on components of other modules.
				// Because modules that make of ModuleRegistry may recursively call their own init()
				// method, a module may already be partly initialized when module.init() is called.
				module.init();
			} catch (Throwable e) {
				logger.warn("An error happened while initializing module " + module, e);
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
			for (Component component : module.getComponents()) {
				logger.debug("  - {}", component.getURI());
				componentsMap.put(component.getURI(), module);
			}
			for (Entity entity: module.getEntities()) {
				logger.debug("  - {}", entity.getPublicId());
				entityMap.put(entity.getPublicId(), module);
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
	 * {@link Module#init} method.
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
	 * {@link Module#init} method.
	 */
	@Override
	public synchronized Module getModuleByComponent(URI uri) {
		do {
			Module module = componentsMap.get(uri);
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
	 * {@link Module#init} method.
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
	 * {@link Module} objects that call this method should be aware that this may call their own
	 * {@link Module#init} method.
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
	 * {@link Module#init} method.
	 */
	@Override
	public synchronized Module getModuleByEntity(String publicId) {
		do {
			Module module = entityMap.get(publicId);
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
	 * {@link Module#init} method.
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
	 * {@link Module#init} method.
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
	 * {@link Module#init} method.
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
		if (!(location.toString().startsWith("file:") || location.toString().startsWith("bundle:")))
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
