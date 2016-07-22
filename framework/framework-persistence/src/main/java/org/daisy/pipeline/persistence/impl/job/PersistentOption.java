package org.daisy.pipeline.persistence.impl.job;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.xml.namespace.QName;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;

@Entity
@Table(name="options")
public class PersistentOption  implements Serializable {
	public static final long serialVersionUID=1L;
	@EmbeddedId
	PK id;
        @Lob
	String value;

	/**
	 * Constructs a new instance.
	 */
	public PersistentOption() {
	}

	/**
	 * Constructs a new instance.
	 */
	public PersistentOption(JobId jobId, QName name,String value) {
		this.id=new PK(jobId,name);
		this.value=value;

	}
	/**
	 * Gets the name for this instance.
	 *
	 * @return The name.
	 */
	public QName getName() {
		return this.id.getNameAsQName();
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
	 * Gets the jobId for this instance.
	 *
	 * @return The jobId.
	 */
	public JobId getJobId() {
		return this.id.getJobId();
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

	@Embeddable
	public static class PK  implements Serializable {
		
		public static final long serialVersionUID=1L;
		@Column(name="job_id")
		String jobId;
		//qnames can be quite long
		@Column(name="name",length=512)
		String name;

		/**
		 * Constructs a new instance.
		 *
		 * @param jobId The jobId for this instance.
		 * @param name The name for this instance.
		 */
		public PK(JobId jobId, QName name) {
			this.jobId = jobId.toString();
			this.name = name.toString();
		}

		/**
		 * Constructs a new instance.
		 */
		public PK() {
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
				
		@Override
		public int hashCode() {
			return (this.jobId+this.name).hashCode();
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
		 * Sets the jobId for this instance.
		 *
		 * @param jobId The jobId.
		 */
		public void setJobId(String jobId) {
			this.jobId = jobId;
		}
		/**
		 * Gets the name for this instance.
		 *
		 * @return The name.
		 */
		public String getName() {
			return this.name;
		}

		public QName getNameAsQName() {
			return QName.valueOf(this.name);
		}
		/**
		 * Sets the name for this instance.
		 *
		 * @param name The name.
		 */
		public void setName(String name) {
			this.name = name;
		}

		/** Sets the name for this instance.
		 *
		 * @param name The name.
		 */
		public void setName(QName name) {
			this.name = name.toString();
		}

	}

}
