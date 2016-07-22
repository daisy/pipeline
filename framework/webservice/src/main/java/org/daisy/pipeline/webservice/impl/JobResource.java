package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.webserviceutils.xml.JobXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlWriterFactory;
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
                JobManager jobMan = webservice().getJobManager(this.getClient());
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
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }
                if (!job.isPresent()) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return this.getErrorRepresentation("Job not found");
                }

                setStatus(Status.SUCCESS_OK);
                JobXmlWriter writer = XmlWriterFactory.createXmlWriterForJob(job.get());
        
                writer.withFullResults(true);
                if(this.webservice().getConfiguration().isLocalFS()){
                	writer.withLocalPaths();
                }
                if (msgSeq == -1) {
                        writer = writer.withAllMessages();
                }
                else {
                        writer = writer.withNewMessages(msgSeq);
                }
                Document doc = writer.withScriptDetails().getXmlDocument();
                
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, doc);
                return dom;
        }

        /**
         * Delete resource.
         */
        @Delete
        public void deleteResource() {
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return;
                }

                if (job == null) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);

                } else {

                        JobManager jobMan = webservice().getJobManager(this.getClient());
                        if (jobMan.deleteJob(job.get().getId()).isPresent() ) {
                                webservice().getStorage().
                                        getJobConfigurationStorage().delete(job.get().getId());
                                setStatus(Status.SUCCESS_NO_CONTENT);
                        } else {
                                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        }
                }
        }

}
