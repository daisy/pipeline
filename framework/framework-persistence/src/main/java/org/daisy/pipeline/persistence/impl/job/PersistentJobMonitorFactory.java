package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.EntityManagerFactory;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.job.JobMonitorFactory;

public class PersistentJobMonitorFactory implements JobMonitorFactory {

	private EntityManagerFactory emf;

	@Override
	public JobMonitor newJobMonitor(JobId id) {
		return new PersistentJobMonitor(id, emf);
	}

	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}

}
