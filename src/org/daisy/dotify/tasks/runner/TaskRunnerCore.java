package org.daisy.dotify.tasks.runner;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.api.tasks.ExpandingTask;
import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadOnlyTask;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.common.io.TempFileHandler;

public class TaskRunnerCore implements Closeable {
	private final Logger logger;
	private final TempFileHandler fj;
	private final TempFileWriter tfw;
	private AnnotatedFile current;
	
	public TaskRunnerCore(File input, File output) throws IOException {
		this(input, output, null);
	}
	
	/**
	 * @deprecated use TaskRunnerCore(File input, File output)
	 * @param fj temp file handler
	 */
	public TaskRunnerCore(TempFileHandler fj) {
		this(fj, null);
	}
	
	public TaskRunnerCore(File input, File output, TempFileWriter tfw) throws IOException {
		this.fj = new TempFileHandler(input, output);
		this.tfw = tfw;
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.current = DefaultAnnotatedFile.with(fj.getInput()).extension(input).build();
	}

	/**
	 * 
	 * @param fj temp file handler
	 * @param tfw temp file writer
	 * @deprecated use TaskRunnerCore(File input, File output, TempFileWriter tfw)
	 */
	public TaskRunnerCore(TempFileHandler fj, TempFileWriter tfw) {
		this.fj = fj;
		this.tfw = tfw;
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.current = DefaultAnnotatedFile.with(fj.getInput()).build();
	}
	
	public List<RunnerResult> runTask(InternalTask task) throws InternalTaskException, IOException {
		List<RunnerResult> ret = new ArrayList<>();
		RunnerResult.Builder r = new RunnerResult.Builder(current, task);
		if (task instanceof ExpandingTask) {
			logger.info("Expanding " + task.getName());
			List<InternalTask> exp = ((ExpandingTask)task).resolve(current);
			ret.add(r.success(true).build());
			for (InternalTask t : exp) {
				ret.addAll(runTask(t));
			}
		} else if (task instanceof ReadWriteTask) {
			logger.info("Running (r/w) " + task.getName());
			current = ((ReadWriteTask)task).execute(current, fj.getOutput());
			ret.add(r.success(true).build());
			if (tfw!=null) {
				tfw.writeTempFile(fj.getOutput(), task.getName());
			}
			fj.reset();
		} else if (task instanceof ReadOnlyTask) {
			logger.info("Running (r) " + task.getName());
			((ReadOnlyTask)task).execute(current);
			ret.add(r.success(true).build());
		} else {
			logger.warning("Unknown task type, skipping.");
			ret.add(r.success(false).build());
		}
		return ret;
	}

	@Override
	public void close() throws IOException {
		fj.close();
	}

}