package org.daisy.pipeline.persistence.impl.webservice;

import java.io.IOException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.daisy.common.zip.ZipUtils;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;

/**
 *  Persists the job configuration. The configuration
 *  string is zipped to avoid wasting space in the db
 *
 * @author Javier Asensio Cubero ( capitan.cambio@gmail.com )
 */
@Entity
public class PersistentJobConfiguration  {
	/**
	 * Builder facility for creating {@link PersistentJobConfiguration} objects 
	 */
	public static class Builder{
		private JobId id;	
		private String configuration;

		/**
		 * Sets the job id to be assigned to this 
		 * builder
		 * @param id
		 */
		public Builder withId(JobId id){
			this.id=id;
			return this;
		}

		/**
		 * Sets the configuration to be assigned to this 
		 * builder
		 * @param configuation
		 */
		public Builder withConfiguration(String configuration){
			this.configuration=configuration;
			return this;
		}

		/**
		 * Builds the instance
		 *
		 * @return
		 */
		public PersistentJobConfiguration build() {
			byte[] zipped;
			try {
				zipped = ZipUtils.deflate(this.configuration);
				return new PersistentJobConfiguration(id.toString(),zipped);
			} catch (IOException e) {
				throw new RuntimeException("Error deflating configuration",e);
			}
		}

	}
	@Id
	private String jobId;

	@Lob
	private byte[] configuration;
	
	/**
	 * Meant for JPA use only
	 */
	public PersistentJobConfiguration() {
	}

	private PersistentJobConfiguration(String jobId,byte[] configuration){
		this.jobId=jobId;
		this.configuration=configuration;
	}

	/**
	 * Returs the job id 
	 *
	 * @return
	 */
	public JobId getId(){
		return JobIdFactory.newIdFromString(this.jobId);
	}

	/**
	 * Returs the configuration as a string 
	 *
	 * @return
	 */
	public String getConfiguration(){
		try {
			return ZipUtils.inflate(this.configuration);
		} catch (IOException e) {
			throw new RuntimeException("Error inflating configuration ",e);
		}
	}
}
