package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.EntityManagerFactory;

import org.daisy.common.messaging.MessageAccessor;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.persistence.impl.messaging.PersistentMessageAccessor;

public class PersistentJobMonitor implements JobMonitor {
	PersistentMessageAccessor accessor;

	public PersistentJobMonitor(JobId id, EntityManagerFactory emf) {
		accessor = new PersistentMessageAccessor(id, emf);
	}

	@Override
	public MessageAccessor getMessageAccessor() {
		return accessor;
	}

}
