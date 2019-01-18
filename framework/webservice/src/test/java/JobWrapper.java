import java.util.List;

import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.Results;

public class JobWrapper {
	
	private Job job;
	
	/**
	 * @param job
	 */
	public JobWrapper(Job job) {
		this.job = job;
	}
	
	public Results getResults(){
		List<Object> elements = job.getNicenameOrBatchIdOrScript();
		return (Results)elements.get(elements.size() - 1);
	}
}
