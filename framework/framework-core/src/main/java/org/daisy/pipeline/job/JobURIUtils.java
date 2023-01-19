package org.daisy.pipeline.job;

import java.io.File;

public class JobURIUtils {

	/**
	 * Ensure that the "org.daisy.pipeline.data" property is set. If not set, a temporary
	 * directory is used to store the job context, result and log files, which means the jobs
	 * can not be persisted across runs.
	 *
	 * Called from PersistentJobStorage.
	 *
	 * @throws IllegalStateException if the property is not set.
	 */
	public static void assertFrameworkDataDirPersisted() throws IllegalStateException {
		org.daisy.pipeline.job.impl.JobURIUtils.assertFrameworkDataDirPersisted();
	}

	public static File getLogFile(String jobId) {
		return org.daisy.pipeline.job.impl.JobURIUtils.getLogFile(jobId);
	}
}
