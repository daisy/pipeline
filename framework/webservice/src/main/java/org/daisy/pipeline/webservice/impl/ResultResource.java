package org.daisy.pipeline.webservice.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.restlet.data.Digest;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * The Class ResultResource.
 */
public class ResultResource extends AuthenticatedResource {
        /** The job. */
        private Optional<Job> job=Optional.absent();
        private static Logger logger = LoggerFactory.getLogger(ResultResource.class
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
                try {
                        JobId id = JobIdFactory.newIdFromString(idParam);
                        job = jobMan.getJob(id);
                } catch (Exception e) {
                        logger.debug("Job Id malformed - Job not found: " + idParam);
                }
        }

        /**
         * Gets the resource.
         *
         * @return the resource
         */
        @Get
        public Representation getResource() {
                logRequest();
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }

                if (!job.isPresent()) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return this.getErrorRepresentation("Job not found");
                }

                if (!(job.get().getStatus().equals(Job.Status.SUCCESS) || job.get().getStatus().equals(Job.Status.FAIL))) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return this.getErrorRepresentation("Job status different to SUCCESS");
                }

                Collection<JobResult> results = job.get().getContext().getResults()
                                .getResults();
                if (results.size() == 0) {
                        setStatus(Status.SERVER_ERROR_INTERNAL);
                        return this.getErrorRepresentation("No results available");
                }
                try {
                        return getZippedRepresentation(results, job.get());
                } catch (Exception e) {
                        setStatus(Status.SERVER_ERROR_INTERNAL);
                        return this.getErrorRepresentation(e);
                }
        }

        public static Representation getZippedRepresentation(Collection<JobResult> results, Job job)
                        throws IOException, NoSuchAlgorithmException {

                InputStream zip = JobResultSet.asZip(results);
                zip = new BufferedInputStream(zip, 8192);
                Integer size = getSize(zip, 32768);
                // DigesterRepresentation doesn't add the header
                Representation rep = new InputRepresentation(zip, MediaType.APPLICATION_ZIP);
                if (size != null) { // if > 32 Mb
                        byte[] bytes = new byte[size];
                        zip.mark(size);
                        zip.read(bytes);
                        rep.setDigest(new Digest(MessageDigest.getInstance("MD5").digest(bytes)));
                        zip.reset();
                }
                Disposition disposition = new Disposition();
                disposition.setFilename(job.getId().toString() + ".zip");
                disposition.setType(Disposition.TYPE_ATTACHMENT);
                if (size != null) // if > 32 Mb
                        disposition.setSize(size);
                rep.setDisposition(disposition);
                return rep;
        }

        // null means greater than the limit
        private static Integer getSize(InputStream stream, int limit) throws IOException {
                Integer size = 0;
                if (!stream.markSupported())
                        throw new RuntimeException();
                stream.mark(limit + 2048);
                byte[] buf = new byte[2048];
                while (size != null && (size += stream.read(buf)) > 0)
                        if (size > limit) size = null;
                stream.reset();
                return size;
        }
}
