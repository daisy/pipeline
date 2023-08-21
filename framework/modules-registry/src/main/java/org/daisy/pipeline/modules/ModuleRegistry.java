package org.daisy.pipeline.modules;

import java.net.URI;

/**
 * ModuleRegistry offers the functionality to enregister and query for the modules loaded.
 */
public interface ModuleRegistry extends Iterable<Module> {

	/**
	 * Gets the module which has a component identified by the unique systemId.
	 */
	public Module getModuleByComponent(URI uri);

	/**
	 * Gets the module which has a component identified by the unique systemId and a version
	 * matching the given version range.
	 *
	 * @param versionRange the version range, expressed as a mathematical interval notation, using the
	 *                     <a href="https://docs.osgi.org/specification/osgi.core/8.0.0/framework.module.html#i3189032"
	 *                     >OSGi syntax</a>.
	 */
	public Module getModuleByComponent(URI uri, String versionRange);

	/**
	 * Gets the module which has declared the entity with the given public id.
	 */
	public Module getModuleByEntity(String publicId);

	/**
	 * Gets the module which has an XSLT package identified by the given name.
	 */
	public Module getModuleByXSLTPackage(String name);

	/**
	 * Gets the module which has the specified class among its resources, or {@code null} if no such
	 * module can be found.
	 */
	public Module getModuleByClass(Class<?> clazz);

	/**
	 * Returns the list of available components.
	 */
	public Iterable<URI> getComponents();

	/**
	 * Returns the list of available entities.
	 */
	public Iterable<String> getEntities();

}
