package org.daisy.dotify.api.tasks;

import java.util.Set;

/**
 * <p>Provides an interface for task group factories. The purpose of this
 * interface is to expose an implementation of a task group.
 * A task group factory implementation provides task groups for
 * a any number of supported specifications.</p>
 * 
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 * 
 * @author Joel Håkansson
 */
public interface TaskGroupFactory {
	
	/**
	 * Returns true if this factory can create instances for the specified locale.
	 * @param specification the specification to test
	 * @return true if this factory can create instances for the specified specification, false otherwise
	 */
	public boolean supportsSpecification(TaskGroupSpecification specification);
	
	/**
	 * Returns a new input manager configured for the specified locale.
	 * @param locale the locale for the new input manager
	 * @return returns a new input manager
	 */
	public TaskGroup newTaskGroup(TaskGroupSpecification specification);

	/**
	 * Lists the supported file formats.
	 * @return returns a set of supported formats
	 */
	public Set<TaskGroupSpecification> listSupportedSpecifications();
	
}
