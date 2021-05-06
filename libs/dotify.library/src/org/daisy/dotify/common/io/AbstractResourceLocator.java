package org.daisy.dotify.common.io;

import java.net.URL;

/**
 * Provides easy access to package resources. Simply extend AbstractResourceLocator in the package where
 * the resources are located. Your implementation can then be used to locate the resources by
 * specifying a location relative to the implementation.
 *
 * @author Joel Håkansson
 */
public abstract class AbstractResourceLocator implements ResourceLocator {
    private final String basePath;

    /**
     * Creates a new abstract resource locator. Resources are resolved relative
     * to the location of the concrete implementation.
     */
    public AbstractResourceLocator() {
        this(null);
    }

    /**
     * Creates a new abstract resource locator with the specified sub-path prefix, in other
     * words a sub-path from where all resources are resolved. Resources are resolved relative
     * to the location of the concrete implementation.
     *
     * @param basePath the sub-path prefix
     */
    public AbstractResourceLocator(String basePath) {
        if (basePath == null || basePath.equals("")) {
            this.basePath = "";
        } else {
            this.basePath = basePath + "/";
        }
    }

    @Override
    public URL getResource(String subpath) throws ResourceLocatorException {
        String path = basePath + subpath;
        //TODO check the viability of this method
        URL url;
        url = this.getClass().getResource(path);
        if (null == url) {
            String qualifiedPath = this.getClass().getPackage().getName().replace('.', '/') + "/";
            url = this.getClass().getClassLoader().getResource(qualifiedPath + path);
        }
        if (url == null) {
            throw new ResourceLocatorException(
                "Cannot find resource path '" + path + "' relative to " + this.getClass().getCanonicalName()
            );
        }
        return url;
    }

}
