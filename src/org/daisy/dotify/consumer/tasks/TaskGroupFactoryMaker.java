package org.daisy.dotify.consumer.tasks;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * Provides a factory maker for input manager factories, that is to say a collection of
 *  
 * @author Joel Håkansson
 */
@Component
public class TaskGroupFactoryMaker implements TaskGroupFactoryMakerService {
	private final List<TaskGroupFactory> filters;
	private final Map<String, TaskGroupFactory> map;
	private final Logger logger;

	public TaskGroupFactoryMaker() {
		logger = Logger.getLogger(TaskGroupFactoryMaker.class.getCanonicalName());
		filters = new CopyOnWriteArrayList<>();
		this.map = Collections.synchronizedMap(new HashMap<String, TaskGroupFactory>());		
	}

	/**
	 * <p>
	 * Creates a new TaskGroupFactoryMaker and populates it using the SPI (java
	 * service provider interface).
	 * </p>
	 * 
	 * <p>
	 * In an OSGi context, an instance should be retrieved using the service
	 * registry. It will be registered under the TaskGroupFactoryMakerService
	 * interface.
	 * </p>
	 * 
	 * @return returns a new TaskGroupFactoryMaker
	 */
	public final static TaskGroupFactoryMaker newInstance() {
		TaskGroupFactoryMaker ret = new TaskGroupFactoryMaker();
		{
			Iterator<TaskGroupFactory> i = ServiceLoader.load(TaskGroupFactory.class).iterator();
			while (i.hasNext()) {
				TaskGroupFactory factory = i.next();
				factory.setCreatedWithSPI();
				ret.addFactory(factory);
			}
		}
		return ret;
	}

	@Reference(type = '*')
	public void addFactory(TaskGroupFactory factory) {
		logger.finer("Adding factory: " + factory);
		filters.add(factory);
	}

	// Unbind reference added automatically from addFactory annotation
	public void removeFactory(TaskGroupFactory factory) {
		logger.finer("Removing factory: " + factory);
		// this is to avoid adding items to the cache that were removed while
		// iterating
		synchronized (map) {
			filters.remove(factory);
			map.clear();
		}
	}

	private static String toKey(TaskGroupSpecification spec) {
		return new StringBuilder().
				append(spec.getLocale()).
				append(" (").
				append(spec.getInputFormat()).
				append(" -> ").
				append(spec.getOutputFormat()).
				append(")").toString();
	}
	
	@Override
	public TaskGroupFactory getFactory(TaskGroupSpecification spec) {
		String specKey = toKey(spec);
		TaskGroupFactory template = map.get(specKey);
		if (template==null) {
			// this is to avoid adding items to the cache that were removed
			// while iterating
			synchronized (map) {
				for (TaskGroupFactory h : filters) {
					if (h.supportsSpecification(spec)) {
						logger.fine("Found a factory for " + specKey + " (" + h.getClass() + ")");
						map.put(specKey, h);
						template = h;
						break;
					}
				}
			}
		}
		if (template==null) {
			throw new IllegalArgumentException("Cannot locate an TaskGroup for " + toKey(spec));
		}
		return template;
	}
	
	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		logger.fine("Attempt to locate an input manager for " + toKey(spec));
		return getFactory(spec).newTaskGroup(spec);
	}
	
	@Override
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		HashSet<TaskGroupSpecification> ret = new HashSet<>();
		for (TaskGroupFactory h : filters) {
			ret.addAll(h.listSupportedSpecifications());
		}
		return ret;
	}

}