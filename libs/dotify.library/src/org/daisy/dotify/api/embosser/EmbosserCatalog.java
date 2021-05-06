package org.daisy.dotify.api.embosser;

import org.daisy.dotify.api.factory.FactoryCatalog;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Provides a catalog of Embosser factories.
 *
 * @author Joel Håkansson
 */
//TODO: use EmbosserService instead of Embosser and enable OSGi support
@Component
public class EmbosserCatalog implements FactoryCatalog<Embosser>, EmbosserCatalogService {
    private final List<EmbosserProvider> providers;
    private final Map<String, EmbosserProvider> map;
    private final Logger logger;

    /**
     * Creates a new empty instance. This method is public because it is required by OSGi.
     * In an SPI context, use newInstance()
     */
    public EmbosserCatalog() {
        logger = Logger.getLogger(this.getClass().getCanonicalName());
        providers = new CopyOnWriteArrayList<>();
        map = Collections.synchronizedMap(new HashMap<String, EmbosserProvider>());
    }

    /**
     * <p>
     * Creates a new EmbosserCatalog and populates it using the SPI
     * (java service provider interface).
     * </p>
     *
     * <p>
     * In an OSGi context, an instance should be retrieved using the service
     * registry. It will be registered under the EmbosserCatalogService
     * interface.
     * </p>
     *
     * @return returns a new EmbosserCatalog
     */
    public static EmbosserCatalog newInstance() {
        EmbosserCatalog ret = new EmbosserCatalog();
        Iterator<EmbosserProvider> i = ServiceLoader.load(EmbosserProvider.class).iterator();
        while (i.hasNext()) {
            EmbosserProvider ep = i.next();
            ep.setCreatedWithSPI();
            ret.addFactory(ep);
        }
        return ret;
    }

    /**
     * Adds a factory (intended for use by the OSGi framework).
     *
     * @param factory the factory to add
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addFactory(EmbosserProvider factory) {
        logger.finer("Adding factory: " + factory);
        providers.add(factory);
    }

    /**
     * Removes a factory (intended for use by the OSGi framework).
     *
     * @param factory the factory to remove
     */
    // Unbind reference added automatically from addFactory annotation
    public void removeFactory(EmbosserProvider factory) {
        // this is to avoid adding items to the cache that were removed while
        // iterating
        synchronized (map) {
            providers.remove(factory);
            map.clear();
        }
    }

    @Override
    public Embosser get(String identifier) {
        if (identifier == null) {
            return null;
        }
        EmbosserProvider template = map.get(identifier);
        if (template == null) {
            // this is to avoid adding items to the cache that were removed
            // while iterating
            synchronized (map) {
                for (EmbosserProvider p : providers) {
                    for (FactoryProperties fp : p.list()) {
                        if (fp.getIdentifier().equals(identifier)) {
                            logger.fine("Found a factory for " + identifier + " (" + p.getClass() + ")");
                            map.put(fp.getIdentifier(), p);
                            template = p;
                            break;
                        }
                    }
                }
            }
        }
        if (template != null) {
            return template.newFactory(identifier);
        } else {
            return null;
        }
    }

    @Override
    public Embosser newEmbosser(String identifier) {
        return get(identifier);
    }

    @Override
    public Collection<EmbosserFactoryProperties> listEmbossers() {
        Collection<EmbosserFactoryProperties> ret = new ArrayList<>();
        for (EmbosserProvider p : providers) {
            ret.addAll(p.list());
        }
        return ret;
    }

    @Override
    public Collection<EmbosserFactoryProperties> listEmbossers(EmbosserFilter filter) {
        Collection<EmbosserFactoryProperties> ret = new ArrayList<>();
        for (EmbosserFactoryProperties fp : listEmbossers()) {
            if (filter.accept(fp)) {
                ret.add(fp);
            }
        }
        return ret;
    }
}
