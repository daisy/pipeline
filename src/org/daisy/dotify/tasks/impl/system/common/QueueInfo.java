package org.daisy.dotify.tasks.impl.system.common;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.tasks.TaskGroupInformation;

class QueueInfo {
	private final TaskGroupSpecificationFilter candidates;
	private final List<TaskGroupInformation> specs;

	QueueInfo(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs) {
		this.candidates = TaskGroupSpecificationFilter.filterLocaleGroupByType(inputs);
		this.specs = new ArrayList<>(specs);
	}

	TaskGroupSpecificationFilter getCandidates() {
		return candidates;
	}

	List<TaskGroupInformation> getSpecs() {
		return specs;
	}

}