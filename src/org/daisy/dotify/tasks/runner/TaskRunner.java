package org.daisy.dotify.tasks.runner;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.common.io.TempFileHandler;

/**
 * Utility class to run a list of tasks.
 * @author Joel Håkansson
 *
 */
public class TaskRunner {
	private final Logger logger;
	private final String name;
	private final boolean writeTempFiles;
	private final boolean keepTempFilesOnSuccess;
	private final TempFileWriter tempFileWriter;
	
	/**
	 * Provides a builder for TaskRunner
	 * @author Joel Håkansson
	 *
	 */
	public static class Builder {
		private final String name;
		private boolean writeTempFiles = false;
		private boolean keepTempFilesOnSuccess = false;
		private TempFileWriter tempFileWriter = null;
		/**
		 * Creates a new builder with the default values
		 * @param name the name of the task runner
		 */
		public Builder(String name) {
			this.name = name;
		}
		
		public Builder() {
			this("Task Runner");
		}

		/**
		 * If true, temporary files are written to the temporary folder
		 * @param value the value
		 * @return returns this builder
		 */
		public Builder writeTempFiles(boolean value) {
			this.writeTempFiles = value;
			return this;
		}
		/**
		 * If true, keeps temporary files created by the task runner
		 * @param value the value
		 * @return returns this builder
		 */
		public Builder keepTempFiles(boolean value) {
			this.keepTempFilesOnSuccess = value;
			return this;
		}
		/**
		 * Sets the temporary file writer to use
		 * @param value the writer
		 * @return returns this builder
		 */
		public Builder tempFileWriter(TempFileWriter value) {
			this.tempFileWriter = value;
			return this;
		}
		/**
		 * Creates a new TaskRunner with the current status of the builder
		 * @return a new TaskRunner
		 */
		public TaskRunner build() {
			return new TaskRunner(this);
		}
	}
	
	private TaskRunner(Builder builder) {
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.name = builder.name;
		this.writeTempFiles = builder.writeTempFiles;
		this.keepTempFilesOnSuccess = builder.keepTempFilesOnSuccess;
		this.tempFileWriter = builder.tempFileWriter;
	}
	
	/**
	 * Creates a new TaskRunner builder with the specified name
	 * @param name the name of the task runner
	 * @return returns a new builder
	 */
	public static TaskRunner.Builder withName(String name) {
		return new Builder(name);
	}

	public void runTasks(File input, File output, List<InternalTask> tasks) throws IOException, TaskSystemException {
		Progress progress = new Progress();
		logger.info(name + " started on " + progress.getStart());
		int i = 0;
		NumberFormat nf = NumberFormat.getPercentInstance();
		//FIXME: implement temp file handling as per issue #47
		TempFileWriter tempWriter =
				writeTempFiles?
							tempFileWriter!=null?tempFileWriter:new DefaultTempFileWriter.Builder().build()
						:
							null;
		try (TempFileHandler fj = new TempFileHandler(input, output)) {
			TaskRunnerCore itr = new TaskRunnerCore(fj, tempWriter);
			for (InternalTask task : tasks) {
				itr.runTask(task);
				i++;
				progress.updateProgress(i/(double)tasks.size());
				logger.info(nf.format(progress.getProgress()) + " done. ETA " + progress.getETA());
				//progress(i/tasks.size());
			}
		}
		if (!keepTempFilesOnSuccess && tempWriter!=null) {
			// Process were successful, delete temp files
			tempWriter.deleteTempFiles();
		}
		logger.info(name + " finished in " + Math.round(progress.timeSinceStart()/100d)/10d + " s");
	}

}
