package org.daisy.dotify.impl.system.common;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.tasks.TaskGroupSpecification;

class QueueInfo {
	private final TaskGroupSpecificationFilter candidates;
	private final List<TaskGroupSpecification> specs;

	QueueInfo(String locale, List<TaskGroupSpecification> inputs, List<TaskGroupSpecification> specs) {
		this.candidates = TaskGroupSpecificationFilter.filterLocaleGroupByType(inputs, locale);
		this.specs = new ArrayList<>(specs);
	}

	TaskGroupSpecificationFilter getCandidates() {
		return candidates;
	}

	List<TaskGroupSpecification> getSpecs() {
		return specs;
	}

}