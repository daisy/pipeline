package org.daisy.pipeline.gui.databridge;

import org.daisy.pipeline.gui.ServiceRegistry;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.StatusMessage;

import com.google.common.eventbus.Subscribe;

// listen to changes coming from the pipeline framework
public class EventBusListener {

	ServiceRegistry pipelineServices;
	DataManager dataManager;
	public EventBusListener(ServiceRegistry pipelineServices, DataManager dataManager) {
		this.pipelineServices = pipelineServices;
		this.dataManager = dataManager;
	}
	
    @Subscribe
    public void handleStatus(StatusMessage message) {
    	//System.out.println("##################### GUI EVENT BUS STATUS");
    	JobId jobId =  message.getJobId();
    	Job job = pipelineServices.getJobManager().getJob(jobId).get();
    	dataManager.updateStatus(job, message.getStatus());
    }
	
}
