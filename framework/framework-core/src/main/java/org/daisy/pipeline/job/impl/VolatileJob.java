package org.daisy.pipeline.job.impl;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.script.Script;

/**
 * Job that automatically deletes all associated files when the object is dismissed.
 */
public class VolatileJob extends AbstractJob {

	private final AtomicBoolean autoCleanUp;
	private final AtomicInteger resultCount;
	private static boolean shutdownHookAdded = false;

	/**
	 * @param managed Whether the Job will be managed by a {@link JobManager}.
	 */
	public VolatileJob(AbstractJobContext ctxt, Priority priority, XProcEngine xprocEngine, boolean managed) {
		super(ctxt, priority, xprocEngine, managed);
		autoCleanUp = new AtomicBoolean(true);
		resultCount = new AtomicInteger(0);

		// Note that even with the following code, the automatic cleanup does not always
		// happen. This is acceptable because users of the Job class are expected to call close()
		// before discarding a job object.
		if (!shutdownHookAdded) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						// We rely on finalization to clean up files, but finalization is not
						// guaranteed to be done. Running System.runFinalization() makes that more
						// likely:
						System.gc();
						System.runFinalization(); }});
			shutdownHookAdded = true;
		}
	}

	@Override
	public void managedRun() {
		try {
			super.managedRun();
		} finally {
			if (autoCleanUp.get())
				tryDeleteContextDir();
		}
	}

	@Override
	public void managedClose() {
		// make sure finalize() methods don't attempt to clean anything up because
		// super.managedClose() already does the clean up
		autoCleanUp.set(false);
		super.managedClose();
	}

	@Override
	protected JobResultSet.Builder newResultSetBuilder(Script script) {
		return new JobResultSet.Builder(script) {
			@Override
			public JobResultSet.Builder addResult(String port, String idx, File path, String mediaType) {
				addResult(port, newResult(idx, path, mediaType));
				return this;
			}
		};
	}

	private JobResult newResult(String idx, File path, String mediaType) {
		resultCount.incrementAndGet();
		return new JobResult(idx, path, mediaType) {
				@Override
				protected void finalize() {
					if (autoCleanUp.get()) {
						if (resultCount.decrementAndGet() == 0) {
							tryDeleteOutputDir();
							// set to -1
							resultCount.decrementAndGet();
						}
					}
				}
			};
	}

	@Override
	protected void finalize() {
		if (autoCleanUp.get()) {
			tryDeleteLogFile();
			if (resultCount.get() == 0)
				// no JobResult objects were created
				tryDeleteOutputDir();
			if (status == Status.IDLE)
				// run() was not executed yet so context directory is still there
				tryDeleteContextDir();
		}
	}

	private void tryDeleteContextDir() {
		logger.info(String.format("Deleting context files for job %s", getId()));
		JobURIUtils.deleteJobContextDir(getId().toString());
	}

	private void tryDeleteOutputDir() {
		logger.info(String.format("Deleting output files for job %s", getId()));
		JobURIUtils.deleteJobOutputDir(getId().toString());
	}

	private void tryDeleteLogFile() {
		logger.info(String.format("Deleting log file for job %s", getId()));
		JobURIUtils.deleteLogFile(getId().toString());
	}
}
