package org.daisy.dotify.tasks.impl.system.common;

import java.util.ArrayList;
import java.util.List;

import org.daisy.streamline.api.tasks.TaskGroupInformation;

class TaskGroupSpecificationFilter {
	private final List<TaskGroupInformation> convert;
	private final List<TaskGroupInformation> enhance;
	
	private TaskGroupSpecificationFilter(List<TaskGroupInformation> convert, List<TaskGroupInformation> enhance) {
		this.convert = convert;
		this.enhance = enhance;
	}

	static TaskGroupSpecificationFilter filterLocaleGroupByType(List<TaskGroupInformation> candidates) {
		List<TaskGroupInformation> convert = new ArrayList<>();
		List<TaskGroupInformation> enhance = new ArrayList<>();
		if (candidates != null) {
			for (TaskGroupInformation spec : candidates) {

					switch (spec.getActivity()) {
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
		return new TaskGroupSpecificationFilter(convert, enhance);
	}

	List<TaskGroupInformation> getConvert() {
		return convert;
	}

	List<TaskGroupInformation> getEnhance() {
		return enhance;
	}

}