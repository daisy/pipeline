package org.daisy.dotify.consumer.tasks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.api.tasks.TaskSystemFactory;
import org.daisy.dotify.api.tasks.TaskSystemFactoryException;
import org.daisy.dotify.api.tasks.TaskSystemFactoryMakerService;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;


/**
 * Entry point for retrieving a TaskSystem implementation. This class will
 * locate all TaskSystemFactory implementations available to the java
 * services API.
 * 
 * @author Joel Håkansson
 *
 */
@Component
public class TaskSystemFactoryMaker implements TaskSystemFactoryMakerService {
	private final List<TaskSystemFactory> filters;
	private final Map<String, TaskSystemFactory> map;
	private final Logger logger;

	public TaskSystemFactoryMaker() {
		logger = Logger.getLogger(TaskSystemFactoryMaker.class.getCanonicalName());
		filters = new CopyOnWriteArrayList<>();
		this.map = Collections.synchronizedMap(new HashMap<String, TaskSystemFactory>());
	}

	/**
	 * <p>
	 * Creates a new TaskSystemFactoryMaker and populates it using the SPI (java
	 * service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the TaskSystemFactoryMakerService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new TaskSystemFactoryMaker
	 */
	public static TaskSystemFactoryMaker newInstance() {
		TaskSystemFactoryMaker ret = new TaskSystemFactoryMaker();
		{
			Iterator<TaskSystemFactory> i = ServiceLoader.load(TaskSystemFactory.class).iterator();
			while (i.hasNext()) {
				TaskSystemFactory factory = i.next();
				factory.setCreatedWithSPI();
				ret.addFactory(factory);
			}
		}
		return ret;
	}
	
	@Reference(type = '*')
	public void addFactory(TaskSystemFactory factory) {
		logger.finer("Adding factory: " + factory);
		filters.add(factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(TaskSystemFactory factory) {
		logger.finer("Removing factory: " + factory);
		// this is to avoid adding items to the cache that were removed while
		// iterating
		synchronized (map) {
			filters.remove(factory);
			map.clear();
		}
	}
	
	private static String toKey(String context, String outputFormat) {
		return context + "(" + outputFormat + ")";
	}
	
	@Override
	public TaskSystemFactory getFactory(String locale, String outputFormat) throws TaskSystemFactoryException {
		TaskSystemFactory template = map.get(toKey(locale, outputFormat));
		if (template==null) {
			for (TaskSystemFactory h : filters) {
				if (h.supportsSpecification(locale, outputFormat)) {
					logger.fine("Found a factory for " + locale + " (" + h.getClass() + ")");
					map.put(toKey(locale, outputFormat), h);
					template = h;
					break;
				}
			}
		}
		if (template==null) {
			throw new TaskSystemFactoryException("Cannot locate a TaskSystemFactory for " + locale + "/" +outputFormat);
		}
		return template;
	}
	
	@Override
	public TaskSystem newTaskSystem(String context, String outputFormat) throws TaskSystemFactoryException {
		return getFactory(context, outputFormat).newTaskSystem(context, outputFormat);
	}
}