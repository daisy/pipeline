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

	/**
	 * <p>Informs the implementation that it was discovered and instantiated using
	 * information collected from a file within the <tt>META-INF/services</tt> directory.
	 * In other words, it was created using SPI (service provider interfaces).</p>
	 * 
	 * <p>This information, in turn, enables the implementation to use the same mechanism
	 * to set dependencies as needed.</p>
	 * 
	 * <p>If this information is <strong>not</strong> given, an implementation
	 * should avoid using SPIs and instead use
	 * <a href="http://wiki.osgi.org/wiki/Declarative_Services">declarative services</a>
	 * for dependency injection as specified by OSGi. Note that this also applies to
	 * several newInstance() methods in the Java API.</p>
	 * 
	 * <p>The class that created an instance with SPI must call this method before
	 * putting it to use.</p>
	 */
	public void setCreatedWithSPI();
}
