package org.daisy.dotify.tasks.runner;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.ExpandingTask;
import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadOnlyTask;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.common.io.TempFileHandler;

public class TaskRunnerCore {
	private int i;
	private final Logger logger;
	private final TempFileHandler fj;
	private final TempFileWriter tfw;
	
	public TaskRunnerCore(TempFileHandler fj) {
		this(fj, null);
	}

	public TaskRunnerCore(TempFileHandler fj, TempFileWriter tfw) {
		this.i = 0;
		this.fj = fj;
		this.tfw = tfw;
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
	}
	
	public void runTask(InternalTask task) throws InternalTaskException, IOException {
		if (task instanceof ExpandingTask) {
			logger.info("Expanding " + task.getName());
			List<InternalTask> exp = ((ExpandingTask)task).resolve(fj.getInput());
			for (InternalTask t : exp) {
				runTask(t);
			}
		} else if (task instanceof ReadWriteTask) {
			logger.info("Running (r/w) " + task.getName());
			((ReadWriteTask)task).execute(fj.getInput(), fj.getOutput());
			if (tfw!=null) {
				tfw.writeTempFile(fj.getOutput(), task.getName(), i);
			}
			fj.reset();
		} else if (task instanceof ReadOnlyTask) {
			logger.info("Running (r) " + task.getName());
			((ReadOnlyTask)task).execute(fj.getInput());
		} else {
			logger.warning("Unknown task type, skipping.");
		}
		i++;
	}

}