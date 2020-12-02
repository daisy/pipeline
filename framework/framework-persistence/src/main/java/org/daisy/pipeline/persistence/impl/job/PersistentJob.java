package org.daisy.pipeline.persistence.impl.job;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.persistence.impl.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name="jobs")
@NamedQuery ( name="Job.getAll", query="select j from PersistentJob j")
@Access(value=AccessType.FIELD)
public class PersistentJob  extends AbstractJob implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(PersistentJob.class);
	public static final long serialVersionUID=1L;

	/* A job is just an executable context + status 
	 * Due to the limitations of jpa we cant persist superclass attributes
	 * unless the superclass is annotated. 
	 * So here we follow a bean approach
	 * The id is proxified.
	 */
	@Id
	@Column(name="job_id")
	String sJobId;

	//the status changed will be watched by changeStatus
	//and this very object is in charge of updating itself
	@Transient
	Database db=null;

	PersistentJob(Database db, AbstractJobContext ctxt) {
		this(db, ctxt, null);
	}

	PersistentJob(Database db, AbstractJobContext ctxt, Priority priority) {
		super(new PersistentJobContext(ctxt), priority);
		this.db=db;
		this.sJobId=ctxt.getId().toString();
		this.db.addObject(this);
		changeStatus(Status.IDLE);
	}

	/**
	 * Constructs a new instance.
	 */
	private PersistentJob() {
		super(null,null);
	}

	@Enumerated(EnumType.ORDINAL)
	@Access(value=AccessType.PROPERTY)
	@Override
	public Status getStatus() {
		return super.getStatus();
	}

	// public for unit test
	@Override
	public void setStatus(Status status) {
		super.setStatus(status);
	}

	@Enumerated(EnumType.ORDINAL)
	@Access(value=AccessType.PROPERTY)
	@Override
	public Priority getPriority() {
		return super.getPriority();
	}

	@SuppressWarnings("unused") // used by jpa
	private void setPriority(Priority priority) {
		this.priority = priority;
	}

	public static final String MODEL_JOB_CONTEXT = "context";

	@OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	@Access(value=AccessType.PROPERTY)
	@Override
	public PersistentJobContext getContext() {
		return (PersistentJobContext)super.getContext();
	}

	@SuppressWarnings("unused") // used by jpa
	private void setContext(PersistentJobContext pCtxt) {
		super.ctxt= pCtxt;
	}


	public static List<AbstractJob> getAllJobs(Database db){
		TypedQuery<AbstractJob> query =
			      db.getEntityManager().createNamedQuery("Job.getAll", AbstractJob.class);
		List<AbstractJob> jobs=query.getResultList();
		return jobs;
		

	}

	//this will watch for changes in the status and update the db
	@Override
	protected synchronized void onStatusChanged(Job.Status to) {
		logger.info("Changing Status:"+to);	
		if(this.db!=null){
			logger.debug("Updating object");	
			db.updateObject(this);
                        //this should be the proper way to invalidate the object 
                        //in the cache, it simply won't work
                        //db.getCache().evict(PersistentJob.class,this.getId());

		}else{
			logger.warn("Object not updated as the Database is null");
		}
	}

	void setDatabase(Database db){
		this.db=db;
	}
}
