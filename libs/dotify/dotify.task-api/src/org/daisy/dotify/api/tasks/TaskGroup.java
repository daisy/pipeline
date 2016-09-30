package org.daisy.dotify.api.tasks;

import java.util.List;
import java.util.Map;


/**
 * a task group is a file/fileset conversion unit. An implementation
 * should provide a useful small conversion step that may be used in a
 * larger context, for example to assemble a task system based on
 * several such implementations. However, a task group MUST NOT
 * depend on other task groups to serve it's purpose.
 * 
 * @author Joel Håkansson
 */
public interface TaskGroup {
	
	/**
	 * Get a descriptive name for the task group
	 * @return returns the name for the task group
	 */
	public String getName();
	
	/**
	 * Gets a list of parameters applicable to this instance
	 * @return returns a list of parameters
	 */
	public List<TaskOption> getOptions();

	/**
	 * Compile the task group using the supplied parameters
	 * @param parameters the parameters to pass to the task group
	 * @return returns a list of InternalTasks
	 * @throws TaskSystemException throws TaskSystemException if something goes wrong when compiling the task group
	 */
	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException;

}
