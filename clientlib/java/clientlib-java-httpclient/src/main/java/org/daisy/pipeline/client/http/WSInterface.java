package org.daisy.pipeline.client.http;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.client.models.*;

/**
 * Methods for communicating with the Pipeline 2 API.
 * 
 * @see <a href="http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI">http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI</a>
 */
public interface WSInterface {

	// ---------- Configuration ----------

	/**
	 * Set which Pipeline 2 Web API endpoint to use. For instance: "http://localhost:8181/ws"
	 * 
	 * @param endpoint the endpoint to use
	 */
	public void setEndpoint(String endpoint);
	
	/**
	 * Return which Pipeline 2 Web API endpoint is currently used
	 * 
	 * @return the endpoint used
	 */
	public String getEndpoint();
	
	/**
	 * Set the credentials to use for the Pipeline 2 Web API
	 * 
	 * @param username the username to use
	 * @param secret the secret to use
	 */
	public void setCredentials(String username, String secret);
	
	/**
	 * Get the username used to authenticate with the Pipeline 2 Web API
	 * 
	 * @return the username
	 */
	public String getUsername();
	
	/**
	 * Set the key used when invoking /admin/halt to shut down the engine
	 * 
	 * @param key the shutdown key to use
	 */
	public void setShutDownKey(String key);
	
	/**
	 * Get the key meant to be used when invoking /admin/halt
	 * 
	 * @return the shutdown key in use
	 */
	public String getShutDownKey();


	// ---------- Engine ----------

	/**
	 * Get information about the engine
	 * 
	 * @return information about the engine
	 */
	public Alive alive();

	/**
	 * Stop the web service
	 * 
	 * @return whether or not halting the engine succeeded
	 */
	public boolean halt();

	/**
	 * Get the properties used in the Pipeline 2 engine
	 * 
	 * @return the list of properties
	 */
	public List<Property> getProperties();


	// ---------- Scripts ----------

	/**
	 * Get a single script
	 * 
	 * @param scriptId the ID of the script
	 * @return the script
	 */
	public Script getScript(String scriptId);

	/**
	 * Get all scripts
	 * 
	 * @return the list of scripts
	 */
	public List<Script> getScripts();


	// ---------- Data Types ----------

	/**
	 * Get a single data type
	 * 
	 * @param dataTypeId the ID of the datatype
	 * @return the datatype
	 */
	public DataType getDataType(String dataTypeId);

	/**
	 * Get all data types
	 * 
	 * @return a map of all datatypes
	 */
	public Map<String,String> getDataTypes();


	// ---------- Jobs ----------

	/**
	 * Get all jobs
	 * 
	 * @return the list of all jobs
	 */
	public List<Job> getJobs();

	/**
	 * Get a single job
	 * 
	 * @param jobId the job ID
	 * @param msgSeq the number of the first message to include
	 * @return the job
	 */
	public Job getJob(String jobId, long msgSeq);

	/**
	 * Submit a job to the engine
	 * 
	 * @param job the job to post
	 * @return the posted job
	 */
	public Job postJob(Job job);

	/**
	 * Delete a single job
	 * 
	 * @param jobId the job ID
	 * @return whether or not the job was deleted
	 */
	public boolean deleteJob(String jobId);

	/**
	 * Get the size used to store each job
	 * 
	 * @return the job sizes
	 */
	public JobSizes getSizes();

	/**
	 * Get all jobs with the given batchId
	 * 
	 * @param batchId the batch ID
	 * @return the list of jobs for the given batch ID
	 */
	public List<Job> getBatch(String batchId);

	/**
	 * Delete all jobs with the given batchId
	 * 
	 * @param batchId the batch ID
	 * @return whether or not all jobs were successfully deleted
	 */
	public boolean deleteBatch(String batchId);

	/**
	 * Get the log file for a job
	 * 
	 * @param jobId the job ID
	 * @return the log
	 */
	public String getJobLog(String jobId);

	/**
	 * Get results from a job as an InputStream
	 * 
	 * @param jobId the job ID
	 * @param href the relative path to the desired job result file
	 * @return the file as an InputStream
	 */
	public InputStream getJobResultAsStream(String jobId, String href);
	
	/**
	 * Get results from a job as a File
	 * 
	 * @param jobId the job ID
	 * @param href the relative path to the desired job result file
	 * @return the file as a File
	 */
	public File getJobResultAsFile(String jobId, String href);

	/**
	 * Get the job queue
	 * 
	 * @return the job queue
	 */
	public JobQueue getQueue();

	/**
	 * Move job up the queue
	 * 
	 * @param jobId the job ID
	 * @return the job queue
	 */
	public JobQueue moveUpQueue(String jobId);
	
	/**
	 * Move job down the queue
	 * 
	 * @param jobId the job ID
	 * @return the job queue
	 */
	public JobQueue moveDownQueue(String jobId);


	// ---------- Clients ----------

	/**
	 * List all clients
	 * 
	 * @return the list of clients
	 */
	public List<Client> getClients();

	/**
	 * Get a client
	 * 
	 * @param clientId the client ID
	 * @return the client
	 */
	public Client getClient(String clientId);

	/**
	 * Delete a client
	 * 
	 * @param clientId the client ID
	 * @return whether or not the client were successfully deleted
	 */
	public boolean deleteClient(String clientId);

	// TODO: not sure how these work yet:
//	public boolean postClients(TODO);
//	public boolean putClient(Client client);

}
