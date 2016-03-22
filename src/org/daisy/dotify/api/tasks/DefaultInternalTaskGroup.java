package org.daisy.dotify.api.tasks;

import java.util.ArrayList;
import java.util.List;

public class DefaultInternalTaskGroup implements InternalTaskGroup {
	private List<InternalTask> tasks;
	private List<TaskOption> options;
	
	public DefaultInternalTaskGroup() {
		this(new ArrayList<InternalTask>(), new ArrayList<TaskOption>());
	}
	
	public DefaultInternalTaskGroup(List<InternalTask> tasks) {
		this(tasks, new ArrayList<TaskOption>());
	}

	public DefaultInternalTaskGroup(List<InternalTask> tasks, List<TaskOption> options) {
		this.tasks = new ArrayList<>(tasks);
		this.options = new ArrayList<>(options);
	}
	
	public void add(InternalTaskGroup group) {
		if (group.getTasks()!=null) {
			tasks.addAll(group.getTasks());
		}
		if (group.getOptions()!=null) { 
			options.addAll(group.getOptions());
		}
	}
	
	public void addTask(InternalTask task) {
		tasks.add(task);
	}
	
	public void addOption(TaskOption option) {
		options.add(option);
	}

	@Override
	public List<InternalTask> getTasks() {
		return tasks;
	}

	@Override
	public List<TaskOption> getOptions() {
		return options;
	}

}
