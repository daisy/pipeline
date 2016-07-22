package org.daisy.pipeline.persistence.impl.job;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;

@Entity
@Table(name="input_ports")
public class PersistentInputPort implements Serializable{
	public static final long serialVersionUID=1L;
	@EmbeddedId
	private PK id;

	@ElementCollection
	@CollectionTable
	(
	 name="sources",
	 joinColumns={ @JoinColumn(name="job_id", referencedColumnName="job_id"),@JoinColumn(name="input_name", referencedColumnName="name")  }
	)
	private List<PersistentSource> sources=new ArrayList<PersistentSource>(); 


	public PersistentInputPort(JobId jobId, String name) {
		this.id=new PK(jobId,name);
	}
	

	/**
	 * Constructs a new instance.
	 */
	public PersistentInputPort() {
	}

	/**
	 * Gets the jobId for this instance.
	 *
	 * @return The jobId.
	 */
	public JobId getJobId() {
		return this.id.getJobId();
	}

	/**
	 * Gets the name for this instance.
	 *
	 * @return The name.
	 */
	public String getName() {
		return this.id.getName();
	}


	/**
	 * Gets the id for this instance.
	 *
	 * @return The id.
	 */
	public PK getId() {
		return this.id;
	}

	/**
	 * Sets the id for this instance.
	 *
	 * @param id The id.
	 */
	public void setId(PK id) {
		this.id = id;
	}

	/**
	 * Gets the inputs for this instance.
	 *
	 * @return The inputs.
	 */
	public List<PersistentSource> getSources() {
		return this.sources;
	}

	public void addSource(PersistentSource src){
		this.sources.add(src);
	}

	@Embeddable
	public static class PK implements Serializable{

		public static final long serialVersionUID=1L;
		@Column(name="job_id")
		String jobId;	

		@Column(name="name")
		String name;

		/**
		 * Constructs a new instance.
		 *
		 * @param jobId The jobId for this instance.
		 * @param name The name for this instance.
		 */
		public PK(JobId jobId, String name) {
			this.jobId = jobId.toString();
			this.name = name;
		}

		/**
		 * Constructs a new instance.
		 */
		public PK() {
		}

		@Override
		public int hashCode() {
			return (this.jobId+this.name).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			boolean eq=false;
			if (obj instanceof PK){
				PK other=(PK) obj;
				eq=this.name.equals(other.name)&&this.jobId.equals(other.jobId);
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

		/**
		 * Gets the name for this instance.
		 *
		 * @return The name.
		 */
		public String getName() {
			return this.name;
		}

	}
}

