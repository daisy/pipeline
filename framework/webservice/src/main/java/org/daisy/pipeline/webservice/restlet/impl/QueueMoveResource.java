package org.daisy.pipeline.webservice.restlet.impl;

import org.daisy.pipeline.job.JobQueue;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.xml.QueueXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.google.common.base.Optional;

public abstract class QueueMoveResource extends AuthenticatedResource {
        JobQueue queue;
        Optional<Job> job;
        @Override
        public void doInit() {
                super.doInit();
                if (!isAuthenticated()) {
                        return;
                }
                String idParam = (String) getRequestAttributes().get("jobId");
                JobManager manager = getJobManager(this.getClient());
                this.job= manager.getJob(JobIdFactory.newIdFromString(idParam));
                this.queue= manager.getExecutionQueue();

        }

        public abstract void move(JobQueue queue,JobId id);

        /**
         * List the jobs, their final priorities and their times
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
                if(!this.job.isPresent()){
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return this.getErrorRepresentation("Job not found");
                }

                setStatus(Status.SUCCESS_OK);
                this.move(this.queue,this.job.get().getId());
                QueueXmlWriter writer = new QueueXmlWriter(queue, getRequest().getRootRef().toString());
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
                                writer.getXmlDocument());
                logResponse(dom);
                return dom;
        }
}