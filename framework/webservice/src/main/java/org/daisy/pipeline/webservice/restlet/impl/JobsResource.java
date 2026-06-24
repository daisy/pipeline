package org.daisy.pipeline.webservice.restlet.impl;

import java.util.List;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.webservice.CallbackHandler;
import org.daisy.pipeline.webservice.impl.PosterCallback;
import org.daisy.pipeline.webservice.request.JobRequest;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.restlet.JobSubmitter;
import org.daisy.pipeline.webservice.restlet.JobSubmitter.BadRequestException;
import org.daisy.pipeline.webservice.restlet.JobSubmitter.JobRequestAndData;
import org.daisy.pipeline.webservice.xml.JobXmlWriter;
import org.daisy.pipeline.webservice.xml.JobsXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;

public class JobsResource extends AuthenticatedResource {

        private static Logger logger = LoggerFactory.getLogger(JobsResource.class.getName());

        @Get("xml")
        public Representation getResource() {
                logRequest();
                maybeEnableCORS();
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }
                JobManager jobMan = getJobManager(this.getClient());
                JobsXmlWriter writer = new JobsXmlWriter(
                        jobMan.getJobs(),
                        getJobManager(getStorage().getClientStorage().defaultClient()).getExecutionQueue(),
                        getRequest().getRootRef().toString(),
                        getWebSocketRootRef().toString());
                if (getConfiguration().isLocalFS()){
                        writer.withLocalPaths();
                }
                Document doc = writer.getXmlDocument();
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, doc);
                setStatus(Status.SUCCESS_OK);
                logResponse(dom);
                return dom;
        }

        @Post
        public Representation createResource(Representation representation) {
                logRequest();
                maybeEnableCORS();
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }
                try {
                        JobRequestAndData req = JobSubmitter.parseJobRequest(this, representation);
                        Job job = JobSubmitter.submitJob(this, req, getScriptRegistry());

                        // initiate callbacks (deprecation warning has already been given in submitJob)
                        registerCallbacks(job, req.request.getCallbacks());

                        // store the config
                        Document doc = req.request.toXML();
                        getStorage().getJobConfigurationStorage().add(
                                job.getId(),
                                doc != null ? XmlUtils.nodeToString(doc) : req.request.toJSON());

                        // Note that we're not using JobXmlWriter's messagesThreshold argument. It is no use because
                        // filtering of messages on log level already happens in MessageBus and JobProgressAppender.
                        JobXmlWriter writer = new JobXmlWriter(job,
                                                               getRequest().getRootRef().toString(),
                                                               getWebSocketRootRef().toString());
                        Priority jobPriority
                                = getJobManager(getStorage().getClientStorage().defaultClient()).getExecutionQueue()
                                                                                                .getJobPriority(job.getId());
                        if (job.getStatus() == Job.Status.IDLE)
                                writer.withPriority(jobPriority);
                        Document jobXml = writer.withScriptDetails()
                                                .withNotificationsAttribute()
                                                .getXmlDocument();
                        DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, jobXml);
                        setStatus(Status.SUCCESS_CREATED);
                        logResponse(dom);
                        return dom;
                } catch (BadRequestException e) {
                        logger.error("Bad request:", e);
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return getErrorRepresentation(e.getMessage());
                }
        }

        /**
         * Initiate callbacks declared in the job request.
         */
        private void registerCallbacks(Job job, List<JobRequest.Callback> callbacks) {
                for (JobRequest.Callback callback : callbacks) {
                        CallbackHandler handler = getCallbackHandler();
                        if (handler == null)
                                throw new RuntimeException("No push notifier");
                        // Note that the frequency does not have any effect: frequency is hard-coded in
                        // PushNotifier. Because we already have the deprecation message, don't warn about this.
                        handler.addCallback(new PosterCallback(job,
                                                               callback.getType(),
                                                               callback.getFrequency(),
                                                               callback.getHref(),
                                                               getClient(),
                                                               getRequest().getRootRef().toString()));
                }
        }
}
