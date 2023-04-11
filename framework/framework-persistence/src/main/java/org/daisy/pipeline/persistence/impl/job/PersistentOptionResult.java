package org.daisy.pipeline.persistence.impl.job;

import java.io.File;
import java.io.Serializable;
import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.namespace.QName;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobResult;

/**
 * Not used (only kept for backward compatibility).
 */
@Entity
@Table(name="job_option_results")
public class PersistentOptionResult{

	
	
	@EmbeddedId
	private PK id;

	String optionName;

        @Column(length=32672)
	String path;

	String mediaType;

	public PersistentOptionResult(JobId jobId, JobResult result,QName option) {
		this.id=new PK(jobId,result.getIdx());
		this.path=result.getPath().toURI().toString();
		this.optionName=option.toString();
		this.mediaType=result.getMediaType();
	}

	
	/**
	 * Constructs a new instance.
	 */
	public PersistentOptionResult() {
	}

	/**
	 * Gets the optionName for this instance.
	 *
	 * @return The optionName.
	 */
	public QName getOptionName()
	{
		return QName.valueOf(this.optionName);
	}

	/**
	 * Sets the optionName for this instance.
	 *
	 * @param optionName The optionName.
	 */
	public void setOptionName(QName optionName)
	{
		this.optionName = optionName.toString();
	}

	/**
	 * Gets the path for this instance.
	 *
	 * @return The path.
	 */
	public File getPath()
	{
		return new File(URI.create(this.path));
	}

	/**
	 * Sets the path for this instance.
	 *
	 * @param path The path.
	 */
	public void setPath(String path)
	{
		this.path = path;
	}

	/**
	 * Gets the mediaType for this instance.
	 *
	 * @return The mediaType.
	 */
	public String getMediaType() {
		return this.mediaType;
	}

	public String getIdx(){
		return this.id.getIdx();
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
		public PK(JobId jobId, String idx) {
			this.jobId = jobId.toString();
			this.idx = idx;
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
