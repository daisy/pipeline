package org.daisy.dotify.api.tasks;

/**
 * <p>
 * Provides an interface for a TaskSystemFactoryMaker service. The purpose of
 * this interface is to expose an implementation of a TaskSystemFactoryMaker as
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
public interface TaskSystemFactoryMakerService {

	/**
	 * Gets a TaskSystemFactory that supports the specified locale and format.
	 * 
	 * @param locale the target locale
	 * @param outputFormat the output file format
	 * @return returns a task system factory for the specified locale and format
	 * @throws TaskSystemFactoryException if a factory cannot be returned
	 */
	public TaskSystemFactory getFactory(String locale, String outputFormat) throws TaskSystemFactoryException;
	
	/**
	 *  Gets a task system for the specified output format and context
	 *  @param locale the target locale
	 *  @param outputFormat the output file format
	 */
	public TaskSystem newTaskSystem(String locale, String outputFormat) throws TaskSystemFactoryException;
}
