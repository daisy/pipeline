package org.daisy.dotify.impl.system.common;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.tasks.TaskGroupSpecification;

class TaskGroupSpecificationFilter {
	private final List<TaskGroupSpecification> convert;
	private final List<TaskGroupSpecification> enhance;
	
	private TaskGroupSpecificationFilter(List<TaskGroupSpecification> convert, List<TaskGroupSpecification> enhance) {
		this.convert = convert;
		this.enhance = enhance;
	}

	static TaskGroupSpecificationFilter filterLocaleGroupByType(List<TaskGroupSpecification> candidates, String locale) {
		List<TaskGroupSpecification> convert = new ArrayList<>();
		List<TaskGroupSpecification> enhance = new ArrayList<>();
		if (candidates != null) {
			for (TaskGroupSpecification spec : candidates) {
				if (locale.equals(spec.getLocale())) {
					switch (spec.getType()) {
						case CONVERT:
							convert.add(spec);
							break;
						case ENHANCE:
							enhance.add(spec);
							break;
						default:
							
					}
				}
			}
		}
		return new TaskGroupSpecificationFilter(convert, enhance);
	}

	List<TaskGroupSpecification> getConvert() {
		return convert;
	}

	List<TaskGroupSpecification> getEnhance() {
		return enhance;
	}

}