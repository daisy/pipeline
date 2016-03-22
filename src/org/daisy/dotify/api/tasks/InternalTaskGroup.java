package org.daisy.dotify.api.tasks;

import java.util.List;

public interface InternalTaskGroup {
	
	public List<InternalTask> getTasks();

	/**
	 * Gets a list of parameters applicable to this instance
	 * @return returns a list of parameters
	 */
	public List<TaskOption> getOptions();

}
