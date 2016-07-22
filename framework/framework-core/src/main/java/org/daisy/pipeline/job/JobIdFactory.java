package org.daisy.pipeline.job;



// TODO: Auto-generated Javadoc
/**
 * A factory for creating JobId objects.
 */
public class JobIdFactory {

	/**
	 * New id object.
	 *
	 * @return the job id
	 */
	public static JobId newId(){
		return new JobUUIDGenerator().generateId();
	}
	/**
	 * New batch id object.
	 *
	 * @return the job batch id
	 */
	public static JobBatchId newBatchId(){
		return new  JobBatchId(new JobUUIDGenerator().generateBatchId().toString());
	}

	
	/**
	 * New id using a string object as base.
	 *
	 * @param base the base
	 * @return the job id
	 */
	public static JobId newIdFromString(String base){
		return new JobUUIDGenerator().generateIdFromString(base);
	}

	public static JobBatchId newBatchIdFromString(String base){
		return new JobBatchId(base); 
	}
}
