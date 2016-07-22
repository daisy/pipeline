package org.daisy.pipeline.nonpersistent.impl.job;

import org.daisy.common.messaging.MessageAccessor;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.job.JobMonitorFactory;
import org.daisy.pipeline.nonpersistent.impl.messaging.VolatileMessageAccessor;

public final class VolatileJobMonitorFactory implements JobMonitorFactory{




	@Override
	public JobMonitor newJobMonitor(JobId id) {
		return new VolatileJobMonitor(id);
	}

	private final static class VolatileJobMonitor implements JobMonitor {
		private JobId id;

		/**
		 * @param id
		 */
		public VolatileJobMonitor(JobId id) {
			this.id = id;
		}

		@Override
		public MessageAccessor getMessageAccessor() {
			return new VolatileMessageAccessor(this.id);
		}
	}
	
}
