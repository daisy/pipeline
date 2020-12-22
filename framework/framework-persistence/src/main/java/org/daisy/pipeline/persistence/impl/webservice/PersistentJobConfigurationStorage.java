package org.daisy.pipeline.persistence.impl.webservice;

import org.daisy.pipeline.clients.JobConfigurationStorage;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.persistence.impl.Database;

/**
 * This class implements a job storage
 * relying on JPA
 *
 * @author
 */
public class PersistentJobConfigurationStorage implements
		JobConfigurationStorage {

	private Database database;

	/**
	 * @param database
	 */
	public PersistentJobConfigurationStorage(Database database) {
		this.database = database;
	}

	@Override
	public boolean add(JobId id, String configuration) {
		if(this.find(id)!=null){
			return false;
		}
		PersistentJobConfiguration conf= new PersistentJobConfiguration.Builder()
			.withId(id).withConfiguration(configuration).build();
		this.database.addObject(conf);
		return true;
	}

	@Override
	public String get(JobId id) {
		PersistentJobConfiguration cnf=
			this.find(id);	
		if(cnf!=null){
			return cnf.getConfiguration();
		}else{
			return "";
		}
	}

	@Override
	public boolean delete(JobId id) {
		PersistentJobConfiguration cnf=
			this.find(id);	
		return cnf!=null && this.database.deleteObject(cnf);
	}

	private PersistentJobConfiguration find(JobId id){
		return 
			database.getEntityManager().find(PersistentJobConfiguration.class,id.toString());
		}
	
}
