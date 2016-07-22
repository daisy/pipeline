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
import org.daisy.pipeline.xmlcatalog.XmlCatalogParser;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultModuleRegistry implements ModuleRegistry {

	private final class ModuleTracker implements BundleTrackerCustomizer {
		@Override
		public Object addingBundle(final Bundle bundle,
				final BundleEvent event) {
			Bundle result = null;
			URL url = bundle.getResource("META-INF/catalog.xml");
			if (url != null) {
				logger.trace("tracking '{}' <{}>",
						bundle.getSymbolicName(), url);

				Module module;
				try {
					module = new OSGIModuleBuilder().withBundle(bundle).withCatalog(mParser.parse(url.toURI())).build();
				} catch (URISyntaxException e) {
					logger.error("Error getting catalog uri from "+url+"",e);
					throw new RuntimeException("Error getting catalog uri",e);

				}

				// System.out.println(module.getName());
				addModule(module);
				result = bundle;

			}

			// Finally
			return result;
		}

		@Override
		public void modifiedBundle(Bundle bundle,
				BundleEvent event, Object object) {
			// TODO reset module
		}

		@Override
		public void removedBundle(Bundle bundle, BundleEvent event,
				Object object) {
			logger.trace("removing bundle '{}' [{}] ",
					bundle.getSymbolicName(), event);
			// FIXME remove module
			// bundles.remove(bundle.getSymbolicName());
		}
	}

	private static final Logger logger = LoggerFactory
			.getLogger(DefaultModuleRegistry.class);

	private final HashMap<URI, Module> componentsMap = new HashMap<URI, Module>();
	private final HashMap<String, Module> entityMap = new HashMap<String, Module>();
	private final HashSet<Module> modules = new HashSet<Module>();
	private XmlCatalogParser mParser;

	private BundleTracker tracker;

	public DefaultModuleRegistry() {
	}

	public void init(BundleContext context) {
		logger.trace("Activating module registry");
		tracker = new BundleTracker(context, Bundle.ACTIVE,
				new ModuleTracker());
		tracker.open();
		//TODO open the tracker in a separate thread ?
		// new Thread() {
		// @Override
		// public void run() {
		// tracker.open();
		// }
		// }.start();
		logger.trace("Module registry up");
	}

	public void close() {
		tracker.close();
	}

	public void setParser(XmlCatalogParser parser) {
		mParser = parser;
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

	@Override
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

	@Override
	public Module getModuleByEntity(String publicId) {
		return entityMap.get(publicId);
	}

	@Override
	public Iterable<String> getEntities() {
		return entityMap.keySet();
	}


}
