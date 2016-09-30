package org.daisy.dotify.tasks.runner;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.InternalTask;

public class RunnerResult {
	private final AnnotatedFile input;
	private final InternalTask task;
	private final boolean success;
	
	public static class Builder {
		private final AnnotatedFile input;
		private final InternalTask task;
		private boolean success = false;
		public Builder(AnnotatedFile input, InternalTask task) {
			this.input = input;
			this.task = task;
		}
		
		public Builder success(boolean value) {
			this.success = value;
			return this;
		}
		
		public RunnerResult build() {
			return new RunnerResult(this);
		}
	}

	private RunnerResult(Builder builder) {
		this.input = builder.input;
		this.task = builder.task;
		this.success = builder.success;
	}

	public AnnotatedFile getInput() {
		return input;
	}

	public InternalTask getTask() {
		return task;
	}

	public boolean isSuccess() {
		return success;
	}

}
