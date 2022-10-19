package org.daisy.pipeline.webservice.restlet.impl;

import java.util.Collection;

import org.daisy.common.priority.Priority;
import org.daisy.common.priority.Prioritizable;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.xml.JobXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;

import com.google.common.base.Optional;

/**
 * The Class JobResource.
 */
public class JobResource extends AuthenticatedResource {
        /** The job. */
        private Optional<Job> job;
        private int msgSeq = -1;

        /** The logger. */
        private static Logger logger = LoggerFactory.getLogger(JobResource.class
                        .getName());

        /*
         * (non-Javadoc)
         *
         * @see org.restlet.resource.Resource#doInit()
         */
        @Override
        public void doInit() {
                super.doInit();
                if (!isAuthenticated()) {
                        return;
                }
                JobManager jobMan = getJobManager(this.getClient());
                String idParam = (String) getRequestAttributes().get("id");
                String msgSeqParam = getQuery().getFirstValue("msgSeq");

                if (msgSeqParam != null) {
                        msgSeq = Integer.parseInt(msgSeqParam);
                }
                try {
                        JobId id = JobIdFactory.newIdFromString(idParam);
                        job = jobMan.getJob(id);
                } catch (Exception e) {
                        logger.error(e.getMessage());
                        job = Optional.absent();
                }
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
                if (!job.isPresent()) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return this.getErrorRepresentation("Job not found");
                }

                setStatus(Status.SUCCESS_OK);
                JobXmlWriter writer = new JobXmlWriter(job.get(), getRequest().getRootRef().toString());
        
                writer.withFullResults(true);
                if (getConfiguration().isLocalFS()) {
                	writer.withLocalPaths();
                }
                if (msgSeq == -1) {
                        writer = writer.withAllMessages();
                }
                else {
                        writer = writer.withNewMessages(msgSeq);
                }
                if (job.get().getStatus() == Job.Status.IDLE) {
                        writer.withPriority(getJobPriority(job.get()));
                        int pos = getPositionInQueue(job.get());
                        writer.withQueuePosition(pos);
                }

                Document doc = writer.withScriptDetails().getXmlDocument();
                
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, doc);
                logResponse(dom);
                return dom;
        }

        private int getPositionInQueue(Job job) {
                int pos = getJobManager(getStorage().getClientStorage().defaultClient())
                          .getExecutionQueue().getPositionInQueue(job.getId());
                if (pos < 0) // should not happen because we've checked above that the job is idle, meaning it is in the queue
                        return pos;
                else
                        // As this is targeted for end-usures the position starts at 1
                        return pos + 1;
        }

        private Priority getJobPriority(Job job) {
                return getJobManager(getStorage().getClientStorage().defaultClient())
                       .getExecutionQueue().getJobPriority(job.getId());
        }

        /**
         * Delete resource.
         */
        @Delete
        public void deleteResource() {
                logRequest();
                maybeEnableCORS();
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return;
                }

                if (job == null || !job.isPresent()) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);

                } else {

                        JobManager jobMan = getJobManager(this.getClient());
                        if (jobMan.deleteJob(job.get().getId()).isPresent()) {
                                getStorage().getJobConfigurationStorage().delete(job.get().getId());
                                setStatus(Status.SUCCESS_NO_CONTENT);
                        } else {
                                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        }
                }
        }
}
