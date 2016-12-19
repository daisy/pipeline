package org.daisy.dotify.impl.input.epub;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupInformation;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;

import aQute.bnd.annotation.component.Component;

@Component
public class Epub3InputManagerFactory implements TaskGroupFactory {
	private final Set<TaskGroupInformation> information;

	public Epub3InputManagerFactory() {
		Set<TaskGroupInformation> tmp = new HashSet<>();
		tmp.add(TaskGroupInformation.newConvertBuilder("epub", "html").build());
		information = Collections.unmodifiableSet(tmp);
	}
	
	@Override
	public boolean supportsSpecification(TaskGroupInformation spec) {
		return listAll().contains(spec);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		return new Epub3InputManager();
	}
	@Override
	public Set<TaskGroupInformation> listAll() {
		return information;
	}

}