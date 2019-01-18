package org.daisy.pipeline.job;

import java.io.IOException;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.impl.JobURIUtils;
import org.daisy.pipeline.job.impl.XProcDecorator;
import org.daisy.pipeline.script.BoundXProcScript;


/**
 *
 *
 */
public class JobContextFactory {

        private Client client;


        public JobContextFactory(Client client) {
                this.client=client;
        }

         
        public JobContext newJobContext(boolean mapping,String niceName,JobBatchId batchId,BoundXProcScript boundScript,JobResources collection){
        	try{
        		JobId id = JobIdFactory.newId();
        		AbstractJobContext ctxt = (mapping)?
        			new MappingJobContext(client,id,batchId,niceName,boundScript,collection):
        			new SimpleJobContext(client,id,batchId,niceName,boundScript);
        		return ctxt;
        	}catch (IOException e){
        		throw new RuntimeException("Error while creating job context",e);
        	}
        }

        public JobContext newMappingJobContext(String niceName,JobBatchId batchId,BoundXProcScript boundScript,JobResources collection){
                return newJobContext(true, niceName, batchId, boundScript, collection);
        }


        public JobContext newJobContext(String niceName,JobBatchId batchId,BoundXProcScript boundScript){
                return newJobContext(false, niceName, batchId,boundScript, null);

        }
        
        private static class MappingJobContext extends AbstractJobContext {

        	public MappingJobContext(Client client,JobId id, JobBatchId batchId,String niceName,BoundXProcScript boundScript,JobResources collection) throws IOException{
        		super(client,id,batchId,niceName,boundScript,JobURIUtils.newURIMapper(id));
        		XProcDecorator decorator=XProcDecorator.from(this.getScript(),this.getMapper(),collection);
        		this.setInput(decorator.decorate(this.getInputs()));
        		this.setOutput(decorator.decorate(this.getOutputs()));
        	}

        }
        
        private final class SimpleJobContext extends AbstractJobContext {

        	public SimpleJobContext(Client client,JobId id, JobBatchId batchId,String niceName,BoundXProcScript boundScript) throws IOException {
        		super(client,id,batchId,niceName, boundScript,JobURIUtils.newOutputURIMapper(id));
        		XProcDecorator decorator=XProcDecorator.from(this.getScript(),this.getMapper());
        		this.setInput(decorator.decorate(this.getInputs()));
        		this.setOutput(decorator.decorate(this.getOutputs()));
        	}

        }


}
