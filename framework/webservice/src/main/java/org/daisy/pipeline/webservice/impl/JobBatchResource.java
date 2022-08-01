package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.webservice.xml.JobsXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

import org.w3c.dom.Document;

public class JobBatchResource extends JobsResource{
        JobBatchId batchId;
        @Override
        public void doInit() {
                super.doInit();
                String idParam = (String) getRequestAttributes().get("id");
                this.batchId=JobIdFactory.newBatchIdFromString(idParam);
        }

        /**
         * Gets the resource.
         *
         * @return the resource
         */
        @Get("xml")
        public Representation getResource() {
                logRequest();
                maybeEnableCORS();
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }
                JobManager jobMan = webservice().getJobManager(this.getClient(),this.batchId);
                JobsXmlWriter writer = new JobsXmlWriter(
                        jobMan.getJobs(),
                        webservice().getJobManager(webservice().getStorage().getClientStorage().defaultClient())
                                    .getExecutionQueue(),
                        getRequest().getRootRef().toString(),
                        getWebSocketRootRef().toString());
                if(this.webservice().getConfiguration().isLocalFS()){
                	writer.withLocalPaths();
                }
                Document doc = writer.getXmlDocument();
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, doc);
                setStatus(Status.SUCCESS_OK);
                logResponse(dom);
                return dom;
        }
        @Delete
        public void deleteResource() {
                logRequest();
                maybeEnableCORS();
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return;
                }

                if (batchId == null) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return;

                } 
                JobManager jobMan = webservice().getJobManager(this.getClient(),this.batchId);
                for ( Job j: jobMan.deleteAll()) {
                        webservice().getStorage().
                                getJobConfigurationStorage().delete(j.getId());
                } 
                setStatus(Status.SUCCESS_NO_CONTENT);
        }
}
