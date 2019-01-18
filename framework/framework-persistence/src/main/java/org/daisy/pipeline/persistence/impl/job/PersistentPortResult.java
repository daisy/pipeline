package org.daisy.pipeline.persistence.impl.job;

import java.io.Serializable;
import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.daisy.pipeline.job.Index;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobResult;

@Entity
@Table(name="job_port_results")
public class PersistentPortResult   {

	
	
	@EmbeddedId
	private PK id;

	String portName;
        @Column(length=32672)
	String path;

	String mediaType;

	public PersistentPortResult(JobId jobId, JobResult result,String port) {
		Index idx = null;
		try {
			idx = result.getIdx();
		} catch (Exception e) {
			throw new IllegalArgumentException("The result can not be persisted", e);
		}
		this.id = new PK(jobId, idx);
		this.path=result.getPath().toString();
		this.mediaType=result.getMediaType();
		this.portName=port;
	}

	
	/**
	 * Constructs a new instance.
	 */
	public PersistentPortResult() {
	}

	/**
	 * Gets the portName for this instance.
	 *
	 * @return The portName.
	 */
	public String getPortName()
	{
		return this.portName;
	}

	/**
	 * Sets the portName for this instance.
	 *
	 * @param portName The portName.
	 */
	public void setPortName(String portName)
	{
		this.portName = portName;
	}

	/**
	 * Gets the path for this instance.
	 *
	 * @return The path.
	 */
	public URI getPath()
	{
		return URI.create(this.path);
	}

	/**
	 * Gets the mediaType for this instance.
	 *
	 * @return The mediaType.
	 */
	public String getMediaType() {
		return this.mediaType;
	}

	/**
	 * Sets the mediaType for this instance.
	 *
	 * @param mediaType The mediaType.
	 */
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	/**
	 * Sets the path for this instance.
	 *
	 * @param path The path.
	 */
	public void setPath(URI path)
	{
		this.path = path.toString();
	}

	public String getIdx(){
		return this.id.getIdx();
	}

	public JobResult getJobResult(){
		return new JobResult.Builder().withPath(this.getPath()).withIdx(this.getIdx()).withMediaType(this.getMediaType()).build();
	}
	@Embeddable
	public static class PK implements Serializable{

		public static final long serialVersionUID=1L;
		@Column(name="job_id")
		String jobId;	

		@Column(name="idx")
		String idx;

		/**
		 * Constructs a new instance.
		 *
		 * @param jobId The jobId for this instance.
		 * @param name The name for this instance.
		 */
		public PK(JobId jobId, Index idx) {
			this.jobId = jobId.toString();
			this.idx = idx.toString();
		}

		/**
		 * Constructs a new instance.
		 */
		public PK() {
		}

		@Override
		public int hashCode() {
			return (this.jobId+this.idx).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			boolean eq=false;
			if (obj instanceof PK){
				PK other=(PK) obj;
				eq=this.idx.equals(other.idx)&&this.jobId.equals(other.jobId);
			}
			return eq;
		}

		/**
		 * Gets the jobId for this instance.
		 *
		 * @return The jobId.
		 */
		public JobId getJobId() {
			return JobIdFactory.newIdFromString(this.jobId);
		}

		/**
		 * Sets the jobId for this instance.
		 *
		 * @param jobId The jobId.
		 */
		public void setJobId(JobId jobId) {
			this.jobId = jobId.toString();
		}

		public String getIdx() {
			return this.idx;
		}

	}
}
