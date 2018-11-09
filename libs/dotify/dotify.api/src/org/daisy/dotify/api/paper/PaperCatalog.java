package org.daisy.dotify.api.paper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provides a catalog of Paper factories.
 * @author Joel Håkansson
 */
@Component
public class PaperCatalog implements PaperCatalogService {
	private static final Logger logger = Logger.getLogger(PaperCatalog.class.getCanonicalName());
	private final Map<String, Paper> map;

	/**
	 * Creates a new empty instance. This method is public because it is required by OSGi.
	 * In an SPI context, use newInstance()
	 */
	public PaperCatalog() {
		map = Collections.synchronizedMap(new HashMap<String, Paper>(UserPapersCollection.getInstance().getMap()));
	}

	/**
	 * <p>
	 * Creates a new PaperCatalog and populates it using the SPI
	 * (java service provider interface).
	 * </p>
	 *
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the PaperCatalogService
	 * interface.
	 * </p>
	 *
	 * @return returns a new PaperCatalogCatalog
	 */
	public static PaperCatalog newInstance() {
		PaperCatalog ret = new PaperCatalog();
		Iterator<PaperProvider> i = ServiceLoader.load(PaperProvider.class).iterator();
		while (i.hasNext()) {
			PaperProvider provider = i.next();
			ret.addFactory(provider);
		}
		return ret;
	}

	/**
	 * Adds a factory (intended for use by the OSGi framework)
	 * @param factory the factory to add
	 */
	@Reference(cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC)
	public void addFactory(PaperProvider factory) {
		for (Paper paper : factory.list()) {
			map.put(paper.getIdentifier(), paper);
		}
	}

	/**
	 * Removes a factory (intended for use by the OSGi framework)
	 * @param factory the factory to remove
	 */
	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(PaperProvider factory) {
		synchronized (map) {
			for (Paper paper : factory.list()) {
				map.remove(paper.getIdentifier());
			}
		}
	}

	@Override
	public Paper get(String identifier) {
		return map.get(identifier);
	}

	@Override
	public Collection<Paper> list() {
		return map.values();
	}

	@Override
	public Collection<Paper> list(PaperFilter filter) {
		return map.values().stream()
				.filter(v->filter.accept(v))
				.collect(Collectors.toList());
	}

	@Override
	public boolean supportsUserPapers() {
		return true;
	}

	@Override
	public boolean addNewSheetPaper(String name, String desc, Length width, Length height) {
		try {
			SheetPaper ret = UserPapersCollection.getInstance().addNewSheetPaper(name, desc, width, height);
			map.put(ret.getIdentifier(), ret);
			return true;
		} catch (IOException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Failed to add new sheet paper: " + name, e);
			}
			return false;
		}
	}

	@Override
	public boolean addNewTractorPaper(String name, String desc, Length across, Length along) {
		try {
			TractorPaper ret = UserPapersCollection.getInstance().addNewTractorPaper(name, desc, across, along);
			map.put(ret.getIdentifier(), ret);
			return true;
		} catch (IOException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Failed to add new tractor paper: " + name, e);
			}
			return false;
		}
	}

	@Override
	public boolean addNewRollPaper(String name, String desc, Length across) {
		try {
			RollPaper ret = UserPapersCollection.getInstance().addNewRollPaper(name, desc, across);
			map.put(ret.getIdentifier(), ret);
			return true;
		} catch (IOException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Failed to add new roll paper: " + name, e);
			}
			return false;
		}		
	}

	@Override
	public boolean isRemovable(Paper paper) {
		return UserPapersCollection.getInstance().get(paper.getIdentifier())!=null;
	}

	@Override
	public boolean remove(Paper p) {
		try {
			UserPapersCollection.getInstance().remove(p);
			map.remove(p.getIdentifier());
			return true;
		} catch (IOException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Failed to remove paper: " + p.getIdentifier(), e);
			}
			return false;
		}
	}
/*TODO: is needed?
	@Override
	public Collection<Paper> listRemovable() {
		return CustomPaperCollection.getInstance().list();
	}*/
}
