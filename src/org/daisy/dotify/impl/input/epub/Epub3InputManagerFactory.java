package org.daisy.dotify.impl.input.epub;

import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;

public class Epub3InputManagerFactory implements TaskGroupFactory {
	private final Set<TaskGroupSpecification> supportedSpecifications;

	public Epub3InputManagerFactory() {
		this.supportedSpecifications = new HashSet<TaskGroupSpecification>();
		String epub = "epub";
		String obfl = "obfl";
		supportedSpecifications.add(new TaskGroupSpecification(epub, obfl, "sv-SE"));
		supportedSpecifications.add(new TaskGroupSpecification(epub, obfl, "sv"));
		supportedSpecifications.add(new TaskGroupSpecification(epub, obfl, "en"));
		supportedSpecifications.add(new TaskGroupSpecification(epub, obfl, "en-US"));
	}

	@Override
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		return supportedSpecifications;
	}

	@Override
	public boolean supportsSpecification(TaskGroupSpecification spec) {
		return supportedSpecifications.contains(spec);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		if (supportsSpecification(spec)) {
			return new Epub3InputManager();
		}
		return null;
	}

}
