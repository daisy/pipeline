package org.daisy.pipeline.job;

import org.daisy.pipeline.job.Job;

public class StatusMessage {
	private JobId jobId;
	private Job.Status status;
	public static class Builder{
		private JobId jobId;
		private Job.Status status;

		public Builder withJobId(JobId id){
			this.jobId=id;
			return this;
		}

		public Builder withStatus(Job.Status status){
			this.status=status;
			return this;
		}
		
		public StatusMessage build(){
			return new StatusMessage(this.jobId,this.status);
		}
		
	}
	/**
	 * Constructs a new instance.
	 
		
	}
	/**
	 * Constructs a new instance.
	 */
	private StatusMessage(JobId id,Job.Status status) {
		this.jobId=id;
		this.status=status;
	}

	/**
	 * Gets the jobId for this instance.
	 *
	 * @return The jobId.
	 */
	public JobId getJobId() {
		return this.jobId;
	}

	/**
	 * Gets the status for this instance.
	 *
	 * @return The status.
	 */
	public Job.Status getStatus() {
		return this.status;
	}
}
