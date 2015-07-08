package org.daisy.dotify.api.cr;

import java.util.Set;

/**
 * <p>
 * Provides an interface for a TaskGroupFactoryMaker service. The purpose of
 * this interface is to expose an implementation of a TaskGroupFactoryMaker as
 * an OSGi service.
 * </p>
 * 
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 * 
 * @author Joel Håkansson
 * 
 */
public interface TaskGroupFactoryMakerService {

	/**
	 * Gets a TaskGroupFactory that supports the specification
	 * 
	 * @param specification the specification
	 * @return returns a task group manager factory with the desired specification
	 */
	public TaskGroupFactory getFactory(TaskGroupSpecification specification);

	/**
	 * Creates a new input manager with the specified options.
	 * @param specification the specification
	 * @return returns a new task group
	 * @throws IllegalArgumentException if the specified configuration isn't supported
	 */
	public TaskGroup newTaskGroup(TaskGroupSpecification specification);

	/**
	 * Gets a list of supported specifications.
	 * @return returns a list of supported specifications
	 */
	public Set<TaskGroupSpecification> listSupportedSpecifications();

}
