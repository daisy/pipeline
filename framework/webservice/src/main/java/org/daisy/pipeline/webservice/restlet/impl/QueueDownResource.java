package org.daisy.pipeline.webservice.restlet.impl;



import org.daisy.pipeline.job.JobQueue;
import org.daisy.pipeline.job.JobId;


public class QueueDownResource extends QueueMoveResource {

        @Override
        public void move(JobQueue queue, JobId id) {
                queue.moveDown(id);
        }

}
