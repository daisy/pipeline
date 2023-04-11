package org.daisy.pipeline.persistence.impl.job;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import javax.xml.namespace.QName;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;

/**
 * Not used (onlt kept for backward compatibility).
 */
@Entity
@Table(name="parameters")
class PersistentParameter  implements Serializable {
	
	public static final long serialVersionUID=1L;
	@EmbeddedId
	PK id;
	String value;


	/**
	 * Constructs a new instance.
	 *
	 * @param value The value for this instance.
	 */
	public PersistentParameter(JobId jobId, String port,QName name,String value) {
		this.id=new PK(jobId, port,name);
		this.value = value;
	}

	/**
	 * Constructs a new instance.
	 */
	public PersistentParameter() {
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
	 * Gets the value for this instance.
	 *
	 * @return The value.
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Sets the value for this instance.
	 *
	 * @param value The value.
	 */
	public void setValue(String value) {
		this.value = value;
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
	public QName getName() {
		return this.id.getQNameObj();
	}

	/**
	 * Gets the name for this instance.
	 *
	 * @return The name.
	 */
	public String getPort() {
		return this.id.getPort();
	}
	@Embeddable
	public static class PK implements Serializable {
		public static final long serialVersionUID=1L;
		@Column(name="job_id")
		String jobId;
		String qname;
		String port;

		/**
		 * Constructs a new instance.
		 */
		public PK() {
		}

		/**
		 * Constructs a new instance.
		 *
		 * @param jobId The jobId for this instance.
		 * @param qname The qname for this instance.
		 * @param name The name for this instance.
		 */
		public PK(JobId jobId, String port,QName name) {
			this.jobId = jobId.toString();
			this.qname = name.toString();
			this.port = port;
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
		public void setJobId(String jobId) {
			this.jobId = jobId;
		}

		/**
		 * Gets the qname for this instance.
		 *
		 * @return The qname.
		 */
		public String getQname() {
			return this.qname;
		}

		/**
		 * Gets the qname for this instance.
		 *
		 * @return The qname.
		 */
		public QName getQNameObj() {
			return QName.valueOf(this.qname);
		}

		/**
		 * Sets the qname for this instance.
		 *
		 * @param qname The qname.
		 */
		public void setQname(String qname) {
			this.qname = qname;
		}

		/**
		 * Gets the name for this instance.
		 *
		 * @return The name.
		 */
		public String getPort() {
			return this.port;
		}

		/**
		 * Sets the name for this instance.
		 *
		 * @param name The name.
		 */
		public void setPort(String name) {
			this.port = name;
		}

	}
}
