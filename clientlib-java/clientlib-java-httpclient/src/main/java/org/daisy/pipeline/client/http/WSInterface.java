package org.daisy.pipeline.client.http;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.client.models.*;

/**
 * Methods for communicating with the Pipeline 2 API.
 * 
 * @see http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI
 */
public interface WSInterface {

	// ---------- Configuration ----------

	/** Set which Pipeline 2 Web API endpoint to use. For instance: "http://localhost:8181/ws" */
	public void setEndpoint(String endpoint);
	
	/** Return which Pipeline 2 Web API endpoint is currently used */
	public String getEndpoint();
	
	/** Set the credentials to use for the Pipeline 2 Web API */
	public void setCredentials(String username, String secret);
	
	/** Get the username used to authenticate with the Pipeline 2 Web API */
	public String getUsername();
	
	/** Set the key used when invoking /admin/halt to shut down the engine */
	public void setShutDownKey(String key);
	
	/** Get the key meant to be used when invoking /admin/halt */
	public String getShutDownKey();


	// ---------- Engine ----------

	/** Get information about the framework */
	public Alive alive();

	/** Stop the web service */
	public boolean halt();

	/** Get the properties used in the Pipeline 2 engine */
	public List<Property> getProperties();


	// ---------- Scripts ----------

	/** Get a single script */
	public Script getScript(String scriptId);

	/** Get all scripts */
	public List<Script> getScripts();


	// ---------- Data Types ----------

	/** Get a single data type */
	public DataType getDataType(String dataTypeId);

	/** Get all data types */
	public Map<String,String> getDataTypes();


	// ---------- Jobs ----------

	/** Get all jobs */
	public List<Job> getJobs();

	/** Get a single job */
	public Job getJob(String jobId, long msgSeq);

	/** Create a job with files */
	public Job postJob(Job job);

	/** Delete a single job */
	public boolean deleteJob(String jobId);

	/** Get the size used to store each job */
	public JobSizes getSizes();

	/** Get all jobs with the given batchId */
	public List<Job> getBatch(String batchId);

	/** Delete all jobs with the given batchId */
	public boolean deleteBatch(String batchId);

	/** Get the log file for a job */
	public String getJobLog(String jobId);

	/** Get results from a job as an InputStream */
	public InputStream getJobResultAsStream(String jobId, String href);
	
	/** Get results from a job as a File */
	public File getJobResultAsFile(String jobId, String href);

	/** Move job up the queue */
	public JobQueue getQueue();

	/** Move job up the queue */
	public JobQueue moveUpQueue(String jobId);
	
	/** Move job up the queue */
	public JobQueue moveDownQueue(String jobId);


	// ---------- Clients ----------

	/** List all clients */
	public List<Client> getClients();

	/** Get a client */
	public Client getClient(String clientId);

	/** Delete a client */
	public boolean deleteClient(String clientId);

	// TODO: not sure how these work yet:
//	public boolean postClients(TODO);
//	public boolean putClient(Client client);

}
